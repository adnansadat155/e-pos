package com.dinevique.pos.hardware

import com.dinevique.pos.ui.models.DraftOrderItem
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * 9-Pin Dot Matrix Kitchen Impact Printer Builder
 * Supports dynamic two-color ribbon switching (Epson TM-U220 / Star SP700)
 * Source: docs/SOURCES/SYSTEM_PRD.md §5.1, §6.3 & REQ-01-POS FR-POS-005
 */
class KitchenImpactBuilder(private val charset: Charset = Charsets.US_ASCII) {

    private val stream = ByteArrayOutputStream()

    companion object {
        val ESC_INIT = byteArrayOf(0x1B, 0x40)
        val RIBBON_BLACK = byteArrayOf(0x1B, 0x72, 0x00)
        val RIBBON_RED = byteArrayOf(0x1B, 0x72, 0x01)
        val FONT_DOUBLE = byteArrayOf(0x1D, 0x21, 0x11)
        val FONT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val BUZZER_CHIME = byteArrayOf(0x1B, 0x1E)
        val CUT_FULL = byteArrayOf(0x1D, 0x56, 0x00)
        val LINE_FEED = byteArrayOf(0x0A)
        const val IMPACT_WIDTH = 40
    }

    init {
        stream.write(ESC_INIT)
        stream.write(RIBBON_BLACK)
    }

    fun ribbonBlack(): KitchenImpactBuilder {
        stream.write(RIBBON_BLACK)
        return this
    }

    fun ribbonRed(): KitchenImpactBuilder {
        stream.write(RIBBON_RED)
        return this
    }

    fun doubleSize(enable: Boolean): KitchenImpactBuilder {
        stream.write(if (enable) FONT_DOUBLE else FONT_NORMAL)
        return this
    }

    fun bold(enable: Boolean): KitchenImpactBuilder {
        stream.write(if (enable) BOLD_ON else BOLD_OFF)
        return this
    }

    fun chime(): KitchenImpactBuilder {
        stream.write(BUZZER_CHIME)
        return this
    }

    fun line(text: String = ""): KitchenImpactBuilder {
        if (text.isNotEmpty()) {
            stream.write(text.toByteArray(charset))
        }
        stream.write(LINE_FEED)
        return this
    }

    fun divider(): KitchenImpactBuilder {
        return line("=".repeat(IMPACT_WIDTH))
    }

    fun cut(): KitchenImpactBuilder {
        line()
        line()
        line()
        stream.write(CUT_FULL)
        return this
    }

    fun toByteArray(): ByteArray = stream.toByteArray()

    /**
     * Builds a kitchen prep ticket with red emphasis on allergies, rush tags, and exclusions
     */
    fun buildKitchenTicket(
        ticketNumber: String,
        tableLabel: String,
        serverName: String,
        station: String,
        items: List<DraftOrderItem>,
        isRush: Boolean = false,
        allergyAlert: String? = null
    ): ByteArray {
        chime()

        if (isRush) {
            ribbonRed()
            doubleSize(true)
            bold(true)
            line("*** RUSH ORDER ***")
            doubleSize(false)
            bold(false)
            ribbonBlack()
        }

        // Header
        doubleSize(true)
        bold(true)
        line("$ticketNumber  $tableLabel")
        doubleSize(false)
        bold(false)
        line("STATION: $station • SERVER: $serverName")
        divider()

        // Allergy Warning in Red
        if (allergyAlert != null) {
            ribbonRed()
            bold(true)
            line("ALLERGY ALERT: $allergyAlert")
            bold(false)
            ribbonBlack()
            divider()
        }

        // Items
        items.forEach { item ->
            bold(true)
            line("${item.quantity}x ${item.menuItem.name}")
            bold(false)

            item.selectedModifiers.forEach { mod ->
                // Check if modifier is an exclusion or temp
                if (mod.optionName.contains("No ", ignoreCase = true) || mod.optionName.contains("Rare", ignoreCase = true)) {
                    ribbonRed()
                    line("   * ${mod.optionName}")
                    ribbonBlack()
                } else {
                    line("   + ${mod.optionName}")
                }
            }

            if (item.specialInstructions.isNotEmpty()) {
                ribbonRed()
                line("   NOTE: ${item.specialInstructions}")
                ribbonBlack()
            }
            line()
        }

        divider()
        cut()

        return toByteArray()
    }
}
