package com.dinevique.pos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dinevique.pos.payments.ContactlessAuthResult
import com.dinevique.pos.payments.TapToPayEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

// Dinevique Brand Tokens
private val BrandOnyx = Color(0xFF0D0D0D)
private val BrandSlate = Color(0xFF1E2A38)
private val BrandAmber = Color(0xFFD4820A)
private val BrandInk = Color(0xFF1A1A1A)
private val StatusSuccess = Color(0xFF2D7D46)
private val CardBorderColor = Color(0xFF262626)

enum class PaymentStage {
    TIP_SELECTION,
    AWAITING_TAP,
    READING_CARD,
    APPROVED
}

@Composable
fun HandheldPaymentDialog(
    subtotalCents: Long,
    taxCents: Long,
    tableLabel: String = "Table 12",
    onDismissRequest: () -> Unit,
    onPaymentComplete: (ContactlessAuthResult.Success) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val tapToPayEngine = remember { TapToPayEngine() }

    var selectedTipPercent by remember { mutableStateOf<Int?>(18) }
    var stage by remember { mutableStateOf(PaymentStage.TIP_SELECTION) }
    var authResult by remember { mutableStateOf<ContactlessAuthResult.Success?>(null) }

    val tipCents = remember(selectedTipPercent, subtotalCents) {
        if (selectedTipPercent != null) {
            ((subtotalCents * selectedTipPercent!!) / 100.0).roundToLong()
        } else {
            0L
        }
    }

    val totalCents = subtotalCents + taxCents + tipCents

    Dialog(onDismissRequest = {
        if (stage != PaymentStage.READING_CARD) {
            onDismissRequest()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BrandOnyx,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tableside Checkout",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tableLabel,
                        color = BrandAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(BrandSlate, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total display
                Text(
                    text = "$${String.format("%.2f", totalCents / 100.0)}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Subtotal: $${String.format("%.2f", subtotalCents / 100.0)} • Tax: $${String.format("%.2f", taxCents / 100.0)}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (stage) {
                    PaymentStage.TIP_SELECTION -> {
                        Text(
                            text = "Select Gratuity",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tip chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 18, 20).forEach { percent ->
                                val isSelected = selectedTipPercent == percent
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BrandAmber else BrandSlate)
                                        .clickable { selectedTipPercent = percent }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$percent%",
                                            color = if (isSelected) BrandInk else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", (subtotalCents * percent / 100.0) / 100.0)}",
                                            color = if (isSelected) BrandInk else Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTipPercent == 0) BrandAmber else BrandSlate)
                                    .clickable { selectedTipPercent = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Tip",
                                    color = if (selectedTipPercent == 0) BrandInk else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Action Button (Brand Amber fill paired with Brand Ink text)
                        Button(
                            onClick = { stage = PaymentStage.AWAITING_TAP },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandAmber,
                                contentColor = BrandInk
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Proceed to Tap ($${String.format("%.2f", totalCents / 100.0)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    PaymentStage.AWAITING_TAP -> {
                        // Contactless Wave Animation container
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(BrandSlate)
                                .border(2.dp, BrandAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NFC",
                                color = BrandAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tap or Insert Card",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hold contactless card or phone near reader",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Simulate card tap button for field testing
                        Button(
                            onClick = {
                                stage = PaymentStage.READING_CARD
                                coroutineScope.launch {
                                    delay(400) // Simulate NFC read latency
                                    val result = tapToPayEngine.processContactlessTap(
                                        rawPan = "4111111111114242",
                                        expiry = "12/28",
                                        amountCents = subtotalCents + taxCents,
                                        tipCents = tipCents,
                                        isMobileDevice = false
                                    )
                                    if (result is ContactlessAuthResult.Success) {
                                        authResult = result
                                        stage = PaymentStage.APPROVED
                                        delay(800)
                                        onPaymentComplete(result)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandSlate,
                                contentColor = BrandAmber
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Simulate Contactless Card Tap", fontSize = 12.sp)
                        }
                    }

                    PaymentStage.READING_CARD -> {
                        CircularProgressIndicator(
                            color = BrandAmber,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authorizing Payment...",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Do not remove card",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    PaymentStage.APPROVED -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(StatusSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Payment Approved",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        authResult?.let { res ->
                            Text(
                                text = "${res.cardData.cardBrand.displayName} ${res.cardData.cardLast4} • Auth: ${res.authCode}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
