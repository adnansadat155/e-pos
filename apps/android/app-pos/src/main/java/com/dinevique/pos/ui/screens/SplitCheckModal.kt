package com.dinevique.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinevique.pos.ui.BrandAmber
import com.dinevique.pos.ui.BrandInk
import com.dinevique.pos.ui.BrandSlate
import com.dinevique.pos.ui.BrandWhite
import com.dinevique.pos.ui.engine.SplitCheckEngine
import com.dinevique.pos.ui.engine.SplitSubCheck
import com.dinevique.pos.ui.engine.TenderType
import com.dinevique.pos.ui.models.DraftCheck

enum class SplitMode {
    EVEN_N_WAY,
    BY_SEAT
}

@Composable
fun SplitCheckModal(
    draftCheck: DraftCheck,
    onDismiss: () -> Unit,
    onPaymentCompleted: (checkUuid: String, tender: TenderType) -> Unit
) {
    var splitMode by remember { mutableStateOf(SplitMode.EVEN_N_WAY) }
    var partyCount by remember { mutableIntStateOf(2) }

    val subChecks = remember(splitMode, partyCount, draftCheck) {
        if (splitMode == SplitMode.EVEN_N_WAY) {
            SplitCheckEngine.splitEvenlyNWay(draftCheck, partyCount)
        } else {
            SplitCheckEngine.splitBySeat(draftCheck)
        }
    }

    var paidSubChecks by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSlate,
        modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Text(
                    text = "SPLIT CHECK • ${draftCheck.tableLabel}",
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Total: ${draftCheck.formattedTotal()}",
                    color = BrandAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Split Mode Switcher & Party Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertizontally
                ) {
                    // Mode Tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = if (splitMode == SplitMode.EVEN_N_WAY) BrandAmber else Color(0xFF263342),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { splitMode = SplitMode.EVEN_N_WAY }
                        ) {
                            Text(
                                text = "Split Evenly",
                                color = if (splitMode == SplitMode.EVEN_N_WAY) BrandInk else BrandWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            color = if (splitMode == SplitMode.BY_SEAT) BrandAmber else Color(0xFF263342),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { splitMode = SplitMode.BY_SEAT }
                        ) {
                            Text(
                                text = "Split by Seat",
                                color = if (splitMode == SplitMode.BY_SEAT) BrandInk else BrandWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Stepper for N parties if even split
                    if (splitMode == SplitMode.EVEN_N_WAY) {
                        Row(verticalAlignment = Alignment.CenterVertizontally) {
                            Text(text = "Parties: ", color = Color.LightGray, fontSize = 13.sp)
                            IconButton(
                                onClick = { if (partyCount > 2) partyCount-- },
                                enabled = partyCount > 2
                            ) {
                                Text(text = "-", color = BrandWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "$partyCount",
                                color = BrandWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { if (partyCount < 20) partyCount++ },
                                enabled = partyCount < 20
                            ) {
                                Text(text = "+", color = BrandWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF2C3948))

                // Scrollable Sub-Checks List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(subChecks, key = { it.subCheckUuid }) { check ->
                        val isPaid = paidSubChecks.contains(check.subCheckUuid)
                        SplitCheckCard(
                            check = check,
                            isPaid = isPaid,
                            onPay = { tender ->
                                paidSubChecks = paidSubChecks + check.subCheckUuid
                                onPaymentCompleted(check.subCheckUuid, tender)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "DONE", color = BrandAmber, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SplitCheckCard(
    check: SplitSubCheck,
    isPaid: Boolean,
    onPay: (TenderType) -> Unit
) {
    Surface(
        color = Color(0xFF141E28),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isPaid) Color(0xFF22C55E) else Color(0xFF2C3948),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Column {
                    Text(
                        text = check.checkLabel,
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Subtotal: ${check.formattedSubtotal()} • Tax: ${check.formattedTax()}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = check.formattedTotal(),
                    color = BrandAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isPaid) {
                Surface(
                    color = Color(0xFF143825),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PAID IN FULL",
                        color = Color(0xFF86EFAC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPay(TenderType.CARD) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAmber,
                            contentColor = BrandInk
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = "CARD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onPay(TenderType.CASH) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF263342),
                            contentColor = BrandWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = "CASH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onPay(TenderType.OFFLINE_CARD) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF382D1D),
                            contentColor = BrandAmber
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(text = "OFFLINE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
