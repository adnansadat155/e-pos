package com.dinevique.core.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Ktor WebSocket Client for Handheld & KDB devices connecting to Master Terminal
 */
class LanWebSocketClient {
    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var activeSession: DefaultClientWebSocketSession? = null
    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    suspend fun connect(host: String, port: Int = 8080) {
        client.webSocket(host = host, port = port, path = "/ws/lan") {
            activeSession = this
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    _incomingMessages.tryEmit(frame.readText())
                }
            }
        }
    }

    suspend fun sendEvent(json: String) {
        activeSession?.send(Frame.Text(json))
    }

    fun close() {
        client.close()
    }
}
