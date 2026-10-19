package com.dinevique.handheld

import java.util.UUID

/**
 * Dinevique Handheld Domain Models (Sunmi V2 / Mobile Waitstaff)
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.3, §6.2
 */

data class HandheldMenuItem(
    val itemId: String,
    val name: String,
    val categoryId: String,
    val basePriceCents: Long,
    val requiredModifierGroup: HandheldModifierGroup? = null
) {
    fun formattedPrice(): String {
        val dollars = basePriceCents / 100
        val cents = basePriceCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

data class HandheldModifierGroup(
    val groupId: String,
    val name: String,
    val options: List<HandheldModifierOption>
)

data class HandheldModifierOption(
    val optionId: String,
    val name: String,
    val priceDeltaCents: Long = 0
)

data class HandheldDraftItem(
    val itemInstanceUuid: String = UUID.randomUUID().toString(),
    val menuItem: HandheldMenuItem,
    val selectedModifier: HandheldModifierOption? = null,
    val quantity: Int = 1
) {
    val totalCents: Long
        get() = (menuItem.basePriceCents + (selectedModifier?.priceDeltaCents ?: 0L)) * quantity

    fun formattedTotal(): String {
        val dollars = totalCents / 100
        val cents = totalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

data class HandheldDraftOrder(
    val clientOrderUuid: String = UUID.randomUUID().toString(),
    val tableLabel: String = "Table 7",
    val serverName: String = "Elena R.",
    val items: List<HandheldDraftItem> = emptyList(),
    val isLanConnected: Boolean = true
) {
    val subtotalCents: Long
        get() = items.sumOf { it.totalCents }

    val taxCents: Long
        get() = Math.round(subtotalCents * 0.0825)

    val totalCents: Long
        get() = subtotalCents + taxCents

    fun formattedTotal(): String {
        val dollars = totalCents / 100
        val cents = totalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}
