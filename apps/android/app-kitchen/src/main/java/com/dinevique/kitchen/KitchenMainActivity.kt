package com.dinevique.kitchen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.viewmodel.compose.viewModel

val BrandOnyx = Color(0xFF0D0D0D)
val BrandSlate = Color(0xFF1E2A38)
val BrandAmber = Color(0xFFD4820A)
val BrandInk = Color(0xFF1A1A1A)
val BrandWhite = Color(0xFFFFFFFF)

class KitchenMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: KitchenViewModel = viewModel()
            KitchenBoardScreen(viewModel)
        }
    }
}

@Composable
fun KitchenBoardScreen(viewModel: KitchenViewModel) {
    val tickets by viewModel.tickets.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val connectionStatus by viewModel.connectionState.collectAsState()
    val currentEpochMs by viewModel.currentEpochMs.collectAsState()

    val stations = listOf("ALL", "GRILL", "FRY", "SALAD", "BAR", "EXPO")
    val filteredTickets = tickets.filter { selectedStation == "ALL" || it.station == selectedStation }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandOnyx
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertizontally, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DINEVIQUE KITCHEN",
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 2.sp
                    )
                    Surface(
                        color = if (connectionStatus.contains("CONNECTED")) Color(0xFF166534) else BrandAmber,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = connectionStatus,
                            color = if (connectionStatus.contains("CONNECTED")) Color(0xFFBBF7D0) else BrandInk,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Station Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    stations.forEach { station ->
                        val isSelected = station == selectedStation
                        Surface(
                            color = if (isSelected) BrandAmber else BrandSlate,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectStation(station) }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) BrandAmber else Color(0xFF2C3948),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Text(
                                text = station,
                                color = if (isSelected) BrandInk else BrandWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "ACTIVE: ${filteredTickets.size}",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Divider(color = Color(0xFF2C3948))
            Spacer(modifier = Modifier.height(16.dp))

            // Ticket Cards Row
            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All clear. Waiting for tickets...",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTickets, key = { it.orderUuid }) { ticket ->
                        TicketCardItem(
                            ticket = ticket,
                            currentEpochMs = currentEpochMs,
                            onBump = { viewModel.bumpTicket(ticket) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCardItem(
    ticket: UiTicket,
    currentEpochMs: Long,
    onBump: () -> Unit
) {
    val (borderColor, badgeBg, badgeText, ctaLabel) = when (ticket.statusLabel) {
        "NEW_TICKET" -> Quad(Color(0xFFEF4444), Color(0xFF7F1D1D), "NEW", "START PREP")
        "IN_PROGRESS" -> Quad(BrandAmber, Color(0xFF4A3419), "COOKING", "MARK READY")
        "READY" -> Quad(Color(0xFF22C55E), Color(0xFF14532D), "PLATED", "BUMP")
        else -> Quad(BrandSlate, Color(0xFF2C3948), "QUEUED", "BUMP")
    }

    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = BrandSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header: Ticket #, Table, Elapsed Timer & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertizontally
                ) {
                    Text(
                        text = ticket.ticketNumber,
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = borderColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertizontally
                ) {
                    Text(
                        text = "${ticket.tableLabel} • ${ticket.station}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "⏱ ${ticket.formattedElapsed(currentEpochMs)}",
                        color = if (borderColor == Color(0xFFEF4444)) Color(0xFFFCA5A5) else BrandAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFF2C3948))
                Spacer(modifier = Modifier.height(14.dp))

                // Items List
                ticket.items.forEach { item ->
                    Text(
                        text = "• $item",
                        color = BrandWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // High-Contrast Bump Action Button (64dp minimum touch target for kitchen operations)
            Button(
                onClick = onBump,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandAmber,
                    contentColor = BrandInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ctaLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
