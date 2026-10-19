package com.dinevique.pos.ui.models

/**
 * Table Floor Plan Data Models
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1 & REQ-01-POS FR-POS-003
 */

enum class TableStatus {
    AVAILABLE,      // Ready for seating (Slate fill)
    SEATED,         // Actively occupied with open order (Brand Amber accent & elapsed timer)
    CHECK_DROPPED,  // Bill presented to guests, awaiting tender (Warning Ochre)
    DIRTY           // Table vacated, needs busing (Muted Dark)
}

enum class TableShape {
    RECTANGLE,
    ROUND,
    BOOTH
}

data class TableUiModel(
    val tableId: String,
    val tableLabel: String,
    val section: String = "MAIN_DINING",
    val capacity: Int = 4,
    val currentGuests: Int = 0,
    val shape: TableShape = TableShape.RECTANGLE,
    val status: TableStatus = TableStatus.AVAILABLE,
    val serverName: String? = null,
    val seatedAtEpochMs: Long? = null,
    val activeCheckUuid: String? = null,
    val activeCheckTotalCents: Long = 0
) {
    /**
     * Calculates elapsed time since seated formatted as mm:ss
     */
    fun formattedElapsedTime(currentEpochMs: Long = System.currentTimeMillis()): String {
        val seated = seatedAtEpochMs ?: return "00:00"
        val diffMs = (currentEpochMs - seated).coerceAtLeast(0)
        val minutes = (diffMs / 1000) / 60
        val seconds = (diffMs / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Formatted active bill total formatted as $XX.XX
     */
    fun formattedTotal(): String {
        val dollars = activeCheckTotalCents / 100
        val cents = activeCheckTotalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}
