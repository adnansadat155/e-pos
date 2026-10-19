package com.dinevique.core.network

import com.dinevique.core.network.models.LanEventEnvelope
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Embedded Ktor HTTP & WebSocket Server (Android Foreground Service)
 * Acts as the in-store orchestrator routing traffic between POS, KDB, and Handhelds.
 * Source: docs/SOURCES/SYSTEM_PRD.md §3.2
 */
class KtorLanServer(
    private val port: Int = 8080
) {
    private var engine: NettyApplicationEngine? = null
    private val activeSessions = ConcurrentHashMap.newKeySet<WebSocketSession>()
    private val json = Json { ignoreUnknownKeys = true }

    private val _incomingEvents = MutableSharedFlow<LanEventEnvelope>(extraBufferCapacity = 128)
    val incomingEvents: SharedFlow<LanEventEnvelope> = _incomingEvents

    fun start() {
        if (engine != null) return

        engine = embeddedServer(Netty, port = port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(30)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                get("/health") {
                    call.respondText(
                        """{"status":"UP","node":"MASTER_TERMINAL_EDGE","connectedClients":${activeSessions.size}}""",
                        ContentType.Application.Json
                    )
                }

                webSocket("/ws/lan") {
                    activeSessions.add(this)
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                try {
                                    val envelope = json.decodeFromString<LanEventEnvelope>(text)
                                    _incomingEvents.tryEmit(envelope)

                                    // If heartbeat, reply with ACK immediately
                                    if (envelope.eventType == "HEARTBEAT") {
                                        send(Frame.Text("""{"eventType":"HEARTBEAT_ACK","timestampEpochMs":${System.currentTimeMillis()}}"""))
                                    }
                                } catch (e: Exception) {
                                    // Non-envelope frame or raw payload
                                }
                            }
                        }
                    } finally {
                        activeSessions.remove(this)
                    }
                }
            }
        }.start(wait = false)
    }

    suspend fun broadcast(envelope: LanEventEnvelope) {
        val payloadStr = json.encodeToString(LanEventEnvelope.serializer(), envelope)
        broadcastRaw(payloadStr)
    }

    suspend fun broadcastRaw(jsonPayload: String) {
        val deadSessions = mutableListOf<WebSocketSession>()
        for (session in activeSessions) {
            try {
                session.send(Frame.Text(jsonPayload))
            } catch (e: Exception) {
                deadSessions.add(session)
            }
        }
        activeSessions.removeAll(deadSessions)
    }

    fun getConnectedCount(): Int = activeSessions.size

    fun stop() {
        engine?.stop(1000, 2000)
        engine = null
        activeSessions.clear()
    }
}
