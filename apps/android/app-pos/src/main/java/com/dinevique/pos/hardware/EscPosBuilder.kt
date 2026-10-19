package com.dinevique.pos.hardware

import com.dinevique.pos.ui.models.DraftCheck
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * ESC/POS Thermal Receipt Bytecode Builder (80mm / 48 Columns)
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1, §6.3 & REQ-01-POS FR-POS-005
 */
class EscPosBuilder(private val charset: Charset = Charsets.UTF_8) {

    private val stream = ByteArrayOutputStream()

    companion object {
        // ESC/POS Command Byte Constants
        val ESC_INIT = byteArrayOf(0x1B, 0x40)
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val FONT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)
        val FONT_DOUBLE = byteArrayOf(0x1D, 0x21, 0x11)
        val CUT_PARTIAL = byteArrayOf(0x1D, 0x56, 0x42, 0x03)
        val LINE_FEED = byteArrayOf(0x0A)
        const val RECEIPT_WIDTH = 48
    }

    init {
        stream.write(ESC_INIT)
    }

    fun alignCenter(): EscPosBuilder {
        stream.write(ALIGN_CENTER)
        return this
    }

    fun alignLeft(): EscPosBuilder {
        stream.write(ALIGN_LEFT)
        return this
    }

    fun alignRight(): EscPosBuilder {
        stream.write(ALIGN_RIGHT)
        return this
    }

    fun bold(enable: Boolean): EscPosBuilder {
        stream.write(if (enable) BOLD_ON else BOLD_OFF)
        return this
    }

    fun doubleSize(enable: Boolean): EscPosBuilder {
        stream.write(if (enable) FONT_DOUBLE else FONT_NORMAL)
        return this
    }

    fun text(text: String): EscPosBuilder {
        stream.write(text.toByteArray(charset))
        return this
    }

    fun line(text: String = ""): EscPosBuilder {
        if (text.isNotEmpty()) {
            stream.write(text.toByteArray(charset))
        }
        stream.write(LINE_FEED)
        return this
    }

    fun divider(char: Char = '-'): EscPosBuilder {
        val s = String(CharArray(RECEIPT_WIDTH) { char })
        return line(s)
    }

    /**
     * Formats left and right text aligned on opposite ends of a 48-column line
     */
    fun lineTwoColumns(left: String, right: String): EscPosBuilder {
        val maxLeftLen = RECEIPT_WIDTH - right.length - 1
        val safeLeft = if (left.length > maxLeftLen) left.substring(0, maxLeftLen) else left
        val spaces = RECEIPT_WIDTH - safeLeft.length - right.length
        val s = safeLeft + " ".repeat(spaces.coerceAtLeast(1)) + right
        return line(s)
    }

    fun cut(): EscPosBuilder {
        line()
        line()
        line()
        stream.write(CUT_PARTIAL)
        return this
    }

    fun toByteArray(): ByteArray = stream.toByteArray()

    /**
     * Builds a standard complete customer receipt from a DraftCheck model
     */
    fun buildCustomerReceipt(
        check: DraftCheck,
        tenderType: String = "CARD",
        amountPaidFormatted: String? = null,
        changeDueFormatted: String? = null,
        reroutedFrom: String? = null
    ): ByteArray {
        alignCenter()
        if (reroutedFrom != null) {
            bold(true)
            line("*** REROUTED FROM $reroutedFrom ***")
            bold(false)
            divider()
        }

        doubleSize(true)
        bold(true)
        line("DINEVIQUE")
        doubleSize(false)
        line("The Art of Smarter Dining")
        line("100 Fine Dining Boulevard, Suite 500")
        line("Tel: (555) 019-8472 • Tax ID: 84-9281740")
        divider()

        alignLeft()
        lineTwoColumns(check.tableLabel, "Server: ${check.serverName}")
        lineTwoColumns("Guests: ${check.guestCount}", "Order: ${check.clientOrderUuid.take(8)}")
        divider()

        // Line Items
        check.items.forEach { item ->
            lineTwoColumns("${item.quantity}x ${item.menuItem.name}", item.formattedItemTotal())
            item.selectedModifiers.forEach { mod ->
                val modPrice = if (mod.priceDeltaCents > 0) String.format("+$%d.%02d", mod.priceDeltaCents / 100, mod.priceDeltaCents % 100) else ""
                lineTwoColumns("   + ${mod.optionName}", modPrice)
            }
        }

        divider()
        lineTwoColumns("Subtotal", check.formattedSubtotal())
        lineTwoColumns("Sales Tax (${check.taxRatePercent}%)", check.formattedTax())
        bold(true)
        doubleSize(true)
        lineTwoColumns("TOTAL", check.formattedTotal())
        doubleSize(false)
        bold(false)
        divider()

        // Tender & Settlement
        lineTwoColumns("Tender Type", tenderType)
        if (amountPaidFormatted != null) {
            lineTwoColumns("Amount Paid", amountPaidFormatted)
        }
        if (changeDueFormatted != null) {
            lineTwoColumns("Change Due", changeDueFormatted)
        }
        divider()

        alignCenter()
        line("Thank you for dining with us.")
        line("Visit dinevique.com for private events")
        cut()

        return toByteArray()
    }
}
