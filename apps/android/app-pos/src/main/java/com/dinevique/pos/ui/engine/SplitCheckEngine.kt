package com.dinevique.pos.ui.engine

import com.dinevique.pos.ui.models.DraftCheck
import com.dinevique.pos.ui.models.DraftOrderItem
import java.util.UUID

/**
 * Split Check Engine & Mathematical Penny-Perfect Rounding
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1, §6.2 & REQ-01-POS FR-POS-004
 */

enum class TenderType {
    CASH,
    CARD,
    OFFLINE_CARD,
    GIFT_CARD
}

data class SplitSubCheck(
    val subCheckUuid: String = UUID.randomUUID().toString(),
    val checkLabel: String,
    val seatNumber: Int? = null,
    val items: List<DraftOrderItem> = emptyList(),
    val subtotalCents: Long,
    val taxCents: Long,
    val totalCents: Long,
    val isPaid: Boolean = false,
    val paidTenderType: TenderType? = null,
    val paidAmountCents: Long = 0L
) {
    fun formattedTotal(): String {
        val dollars = totalCents / 100
        val cents = totalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }

    fun formattedSubtotal(): String {
        val dollars = subtotalCents / 100
        val cents = subtotalCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }

    fun formattedTax(): String {
        val dollars = taxCents / 100
        val cents = taxCents % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

object SplitCheckEngine {

    /**
     * Splits a check evenly across N parties (2 <= N <= 20) with mathematical penny-perfect rounding.
     * Guaranteed zero-cents discrepancy: sum(subchecks.totalCents) == originalCheck.totalCents.
     */
    fun splitEvenlyNWay(draftCheck: DraftCheck, n: Int): List<SplitSubCheck> {
        require(n in 2..20) { "Party count must be between 2 and 20" }

        val totalCents = draftCheck.totalCents
        val taxCents = draftCheck.taxCents

        // Base division and remainders in cents
        val baseTotal = totalCents / n
        val remainderTotal = (totalCents % n).toInt()

        val baseTax = taxCents / n
        val remainderTax = (taxCents % n).toInt()

        val subChecks = ArrayList<SplitSubCheck>(n)

        for (i in 0 until n) {
            // Allocate +1 cent to the first remainderTotal checks
            val partyTotal = if (i < remainderTotal) baseTotal + 1 else baseTotal
            val partyTax = if (i < remainderTax) baseTax + 1 else baseTax
            val partySubtotal = partyTotal - partyTax

            subChecks.add(
                SplitSubCheck(
                    checkLabel = "Check ${i + 1} of $n",
                    items = draftCheck.items, // Shared reference for even split
                    subtotalCents = partySubtotal,
                    taxCents = partyTax,
                    totalCents = partyTotal
                )
            )
        }

        return subChecks
    }

    /**
     * Splits a check by individual seat numbers, assigning items to their respective seats.
     */
    fun splitBySeat(draftCheck: DraftCheck): List<SplitSubCheck> {
        val seats = draftCheck.items.map { it.seatNumber }.distinct().sorted()
        if (seats.isEmpty()) return emptyList()

        return seats.map { seat ->
            val seatItems = draftCheck.items.filter { it.seatNumber == seat }
            val seatSubtotal = seatItems.sumOf { it.itemTotalCents }
            val seatTax = Math.round(seatSubtotal * (draftCheck.taxRatePercent / 100.0))
            val seatTotal = seatSubtotal + seatTax

            SplitSubCheck(
                checkLabel = "Seat $seat Check",
                seatNumber = seat,
                items = seatItems,
                subtotalCents = seatSubtotal,
                taxCents = seatTax,
                totalCents = seatTotal
            )
        }
    }
}
