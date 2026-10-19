package com.dinevique.handheld

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HandheldOrderScreen(
    viewModel: HandheldOrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.currentOrder

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandOnyx
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Header & Categories
            Column {
                HandheldTopBar(
                    tableLabel = order.tableLabel,
                    serverName = order.serverName,
                    isLanConnected = order.isLanConnected,
                    tapCount = uiState.tapCount
                )

                // Category Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { (id, title) ->
                        val isSelected = id == uiState.selectedCategory
                        Surface(
                            color = if (isSelected) BrandAmber else Color(0xFF1E2A38),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectCategory(id) }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) BrandAmber else Color(0xFF2C3948),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) BrandInk else BrandWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Divider(color = Color(0xFF2C3948))
            }

            // Middle Section: Fast Menu Item List (Thumb-Optimized)
            val filteredItems = uiState.menuItems.filter {
                uiState.selectedCategory == "POPULAR" || it.categoryId == uiState.selectedCategory
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.itemId }) { item ->
                    HandheldItemRow(
                        item = item,
                        onClick = { viewModel.onItemTapped(item) }
                    )
                }
            }

            // Bottom Section: Sticky Active Summary & 3-Tap Fire CTA
            Surface(
                color = Color(0xFF1E2A38),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertizontally
                    ) {
                        Text(
                            text = "${order.items.size} ITEMS IN CHECK",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = order.formattedTotal(),
                            color = BrandAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persistent 3-Tap CTA Button (56dp min touch target)
                    Button(
                        onClick = { viewModel.fireOrderToMaster() },
                        enabled = order.items.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAmber,
                            contentColor = BrandInk,
                            disabledContainerColor = Color(0xFF333333),
                            disabledContentColor = Color(0xFF777777)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertizontally,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SEND TO KITCHEN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = BrandInk,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LAN PROXY",
                                    color = BrandAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Required Modifier Selection Bottom Dialog
    if (uiState.isModifierModalOpen && uiState.pendingModifierItem != null) {
        val pending = uiState.pendingModifierItem!!
        val group = pending.requiredModifierGroup!!

        AlertDialog(
            onDismissRequest = { viewModel.dismissModifierModal() },
            containerColor = Color(0xFF1E2A38),
            title = {
                Text(
                    text = "${pending.name} • ${group.name}",
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tap desired option (Tap 2):",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    group.options.forEach { opt ->
                        Surface(
                            color = Color(0xFF263342),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.onModifierSelected(opt) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertizontally
                            ) {
                                Text(text = opt.name, color = BrandWhite, fontWeight = FontWeight.Medium)
                                if (opt.priceDeltaCents > 0) {
                                    val formatted = String.format("+$%d.%02d", opt.priceDeltaCents / 100, opt.priceDeltaCents % 100)
                                    Text(text = formatted, color = BrandAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun HandheldTopBar(
    tableLabel: String,
    serverName: String,
    isLanConnected: Boolean,
    tapCount: Int
) {
    Surface(
        color = Color(0xFF141E28),
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertizontally
        ) {
            Column {
                Text(
                    text = tableLabel,
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = serverName,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertizontally) {
                // LAN Status Pill
                Surface(
                    color = if (isLanConnected) Color(0xFF143825) else Color(0xFF381414),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLanConnected) Color(0xFF22C55E) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLanConnected) "MASTER LAN" else "OFFLINE",
                            color = if (isLanConnected) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HandheldItemRow(
    item: HandheldMenuItem,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E2A38),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(width = 1.dp, color = Color(0xFF2C3948), shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertizontally
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = BrandWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (item.requiredModifierGroup != null) {
                    Text(
                        text = "Requires: ${item.requiredModifierGroup.name}",
                        color = BrandAmber,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = item.formattedPrice(),
                color = BrandAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
