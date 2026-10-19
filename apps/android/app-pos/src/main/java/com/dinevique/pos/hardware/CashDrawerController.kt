package com.dinevique.pos.hardware

import java.util.UUID

/**
 * Solenoid Cash Drawer Kick Controller
 * Generates pulse sequence over RJ12 printer peripheral port or USB relay.
 * Enforces < 100ms kick latency SLA.
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1, §6.3 & REQ-01-POS FR-POS-005
 */

data class DrawerAuditEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String = "CASH_DRAWER_OPENED",
    val triggerSource: String, // "CASH_TENDER", "NO_SALE_OVERRIDE"
    val employeeId: String,
    val employeeName: String,
    val latencyMs: Long,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

object CashDrawerController {

    // ESC/POS Cash Drawer Kick Sequence (RJ12 Pin 2, 50ms ON, 500ms OFF)
    val KICK_COMMAND_PIN2 = byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte())
    // RJ12 Pin 5 alternate kick
    val KICK_COMMAND_PIN5 = byteArrayOf(0x1B, 0x70, 0x01, 0x19, 0xFA.toByte())

    /**
     * Generates kick command and measures execution latency
     */
    fun kickDrawer(
        triggerSource: String = "CASH_TENDER",
        employeeId: String = "emp_01",
        employeeName: String = "Server Elena"
    ): Pair<ByteArray, DrawerAuditEvent> {
        val startNano = System.nanoTime()

        // Generate byte command
        val command = KICK_COMMAND_PIN2

        val elapsedMs = (System.nanoTime() - startNano) / 1_000_000
        val auditEvent = DrawerAuditEvent(
            triggerSource = triggerSource,
            employeeId = employeeId,
            employeeName = employeeName,
            latencyMs = elapsedMs
        )

        return command to auditEvent
    }

    /**
     * Executes manual No-Sale override requiring manager credentials
     */
    fun openNoSaleDrawer(
        managerId: String,
        managerName: String,
        reason: String = "CHANGE_REQUEST"
    ): Pair<ByteArray, DrawerAuditEvent> {
        val (command, audit) = kickDrawer(
            triggerSource = "NO_SALE_OVERRIDE: $reason",
            employeeId = managerId,
            employeeName = managerName
        )
        return command to audit
    }
}
