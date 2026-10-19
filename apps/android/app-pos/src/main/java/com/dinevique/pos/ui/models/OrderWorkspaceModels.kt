package com.dinevique.pos.ui.models

import java.util.UUID

/**
 * POS Order Workspace Models
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1, §6.2
 */

enum class CourseHoldState {
    HOLD,
    FIRE
}

data class ModifierOptionUiModel(
    val optionId: String,
    val name: String,
    val priceDeltaCents: Long = 0,
    val isAvailable: Boolean = true
) {
    fun formattedPriceDelta(): String {
        if (priceDeltaCents == 0L) return ""
        val dollars = priceDeltaCents / 100
        val cents = priceDeltaCents % 100
        return String.format("+$$%d.%02d", dollars, cents)
    }
}

data class ModifierGroupUiModel(
    val groupId: String,
    val name: String,
    val minSelection: Int = 0,
    val maxSelection: Int = 1,
    val isRequired: Boolean = false,
    val options: List<ModifierOptionUiModel> = emptyList()
)

data class MenuItemUiModel(
    val itemId: String,
    val categoryId: String,
    val name: String,
    val description: String = "",
    val basePriceCents: Long,
    val prepStation: String = "EXPO",
    val isAvailable: Boolean = true,
    val modifierGroups: List<ModifierGroupUiModel> = emptyList()
) {
    fun formattedPrice(): String {
        val dollars = basePriceCents / 100
        val cents = basePriceCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

data class SelectedModifierItem(
    val groupId: String,
    val groupName: String,
    val optionId: String,
    val optionName: String,
    val priceDeltaCents: Long
)

data class DraftOrderItem(
    val itemInstanceUuid: String = UUID.randomUUID().toString(),
    val menuItem: MenuItemUiModel,
    val quantity: Int = 1,
    val seatNumber: Int = 1,
    val courseHold: CourseHoldState = CourseHoldState.FIRE,
    val selectedModifiers: List<SelectedModifierItem> = emptyList(),
    val specialInstructions: String = ""
) {
    val unitPriceCents: Long
        get() = menuItem.basePriceCents + selectedModifiers.sumOf { it.priceDeltaCents }

    val itemTotalCents: Long
        get() = unitPriceCents * quantity

    fun formattedItemTotal(): String {
        val dollars = itemTotalCents / 100
        val cents = itemTotalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

data class DraftCheck(
    val clientOrderUuid: String = UUID.randomUUID().toString(),
    val tableId: String,
    val tableLabel: String,
    val serverName: String = "Server Elena",
    val guestCount: Int = 2,
    val items: List<DraftOrderItem> = emptyList(),
    val taxRatePercent: Double = 8.25
) {
    val subtotalCents: Long
        get() = items.sumOf { it.itemTotalCents }

    val taxCents: Long
        get() = Math.round(subtotalCents * (taxRatePercent / 100.0))

    val totalCents: Long
        get() = subtotalCents + taxCents

    fun formattedSubtotal(): String = formatCurrency(subtotalCents)
    fun formattedTax(): String = formatCurrency(taxCents)
    fun formattedTotal(): String = formatCurrency(totalCents)

    companion object {
        fun formatCurrency(cents: Long): String {
            val dollars = cents / 100
            val c = cents % 100
            return String.format("$%d.%02d", dollars, c)
        }
    }
}
