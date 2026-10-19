package com.dinevique.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.dinevique.pos.ui.*
import com.dinevique.pos.ui.models.TableShape
import com.dinevique.pos.ui.models.TableStatus
import com.dinevique.pos.ui.models.TableUiModel
import com.dinevique.pos.ui.viewmodel.FloorPlanViewModel

val StatusAvailableBg = Color(0xFF141E28)
val StatusAvailableBorder = Color(0xFF233547)

val StatusSeatedBg = Color(0xFF1C1A14)
val StatusSeatedBorder = Color(0xFFD4820A) // Brand Amber

val StatusCheckDroppedBg = Color(0xFF261D10)
val StatusCheckDroppedBorder = Color(0xFFB8860B) // Warning Ochre

val StatusDirtyBg = Color(0xFF1F1A1C)
val StatusDirtyBorder = Color(0xFF8B263E) // Muted Crimson

@Composable
fun FloorPlanScreen(
    viewModel: FloorPlanViewModel,
    onTableSelectedForOrder: (TableUiModel) -> Unit,
    onLockRequested: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showActionDialog by remember { mutableStateOf(false) }
    var actionTargetTable by remember { mutableStateOf<TableUiModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandOnyx)
    ) {
        // Top System / Navigation Bar
        FloorPlanTopBar(
            selectedSection = uiState.selectedSection,
            sections = uiState.sections,
            onSectionSelected = { viewModel.selectSection(it) },
            onLockRequested = onLockRequested
        )

        // Main Floor Canvas Grid
        val sectionTables = uiState.tables.filter { it.section == uiState.selectedSection }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sectionTables, key = { it.tableId }) { table ->
                TableCard(
                    table = table,
                    currentEpochMs = uiState.currentEpochMs,
                    onClick = {
                        actionTargetTable = table
                        showActionDialog = true
                    }
                )
            }
        }
    }

    // Table Action Modal
    if (showActionDialog && actionTargetTable != null) {
        val currentTable = uiState.tables.find { it.tableId == actionTargetTable!!.tableId } ?: actionTargetTable!!
        TableActionDialog(
            table = currentTable,
            onDismiss = {
                showActionDialog = false
                actionTargetTable = null
            },
            onOpenOrder = {
                showActionDialog = false
                onTableSelectedForOrder(currentTable)
            },
            onSeatParty = { guests ->
                viewModel.seatGuests(currentTable.tableId, guests)
                showActionDialog = false
                onTableSelectedForOrder(currentTable.copy(status = TableStatus.SEATED, currentGuests = guests))
            },
            onMarkDirty = {
                viewModel.markTableDirty(currentTable.tableId)
                showActionDialog = false
            },
            onMarkAvailable = {
                viewModel.markTableAvailable(currentTable.tableId)
                showActionDialog = false
            }
        )
    }
}

@Composable
fun FloorPlanTopBar(
    selectedSection: String,
    sections: List<String>,
    onSectionSelected: (String) -> Unit,
    onLockRequested: () -> Unit
) {
    Surface(
        color = BrandSlate,
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertizontally,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Station ID
            Row(verticalAlignment = Alignment.CenterVertizontally) {
                Text(
                    text = "DINEVIQUE",
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = BrandAmber,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "TERMINAL 01 • MASTER",
                        color = BrandInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Section Filter Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sections.forEach { section ->
                    val isSelected = section == selectedSection
                    val cleanTitle = section.replace("_", " ")
                    Surface(
                        color = if (isSelected) BrandAmber else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSectionSelected(section) }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BrandAmber else Color(0xFF33465D),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = cleanTitle,
                            color = if (isSelected) BrandInk else BrandWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // User Info & Rapid Lock CTA
            Row(verticalAlignment = Alignment.CenterVertizontally) {
                Text(
                    text = "Server Elena",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onLockRequested,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C3948),
                        contentColor = BrandWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "LOCK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TableCard(
    table: TableUiModel,
    currentEpochMs: Long,
    onClick: () -> Unit
) {
    val (bgColor, borderColor, badgeColor, statusLabel) = when (table.status) {
        TableStatus.AVAILABLE -> Quadruple(StatusAvailableBg, StatusAvailableBorder, Color(0xFF22C55E), "AVAILABLE")
        TableStatus.SEATED -> Quadruple(StatusSeatedBg, StatusSeatedBorder, BrandAmber, "SEATED • ${table.formattedElapsedTime(currentEpochMs)}")
        TableStatus.CHECK_DROPPED -> Quadruple(StatusCheckDroppedBg, StatusCheckDroppedBorder, Color(0xFFEAB308), "DROPPED • ${table.formattedTotal()}")
        TableStatus.DIRTY -> Quadruple(StatusDirtyBg, StatusDirtyBorder, Color(0xFFEF4444), "DIRTY • BUS")
    }

    val shape = when (table.shape) {
        TableShape.ROUND -> RoundedCornerShape(24.dp)
        TableShape.BOOTH -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        TableShape.RECTANGLE -> RoundedCornerShape(12.dp)
    }

    Surface(
        color = bgColor,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .border(width = 2.dp, color = borderColor, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Table Label & Capacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Text(
                    text = table.tableLabel,
                    color = BrandWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = if (table.status == TableStatus.SEATED) "${table.currentGuests}/${table.capacity}p" else "${table.capacity} seats",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            // Middle: Server or Details
            if (table.serverName != null) {
                Text(
                    text = table.serverName,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            } else {
                Text(
                    text = "Ready to seat",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }

            // Footer Badge: Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusLabel,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TableActionDialog(
    table: TableUiModel,
    onDismiss: () -> Unit,
    onOpenOrder: () -> Unit,
    onSeatParty: (guests: Int) -> Unit,
    onMarkDirty: () -> Unit,
    onMarkAvailable: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSlate,
        title = {
            Text(
                text = "${table.tableLabel} • ${table.status.name}",
                color = BrandWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Capacity: ${table.capacity} guests • Section: ${table.section}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                when (table.status) {
                    TableStatus.AVAILABLE -> {
                        Text(
                            text = "Choose party size to seat guests:",
                            color = BrandWhite,
                            fontSize = 14.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..minOf(table.capacity, 6)).forEach { count ->
                                Button(
                                    onClick = { onSeatParty(count) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2C3948),
                                        contentColor = BrandWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "$count")
                                }
                            }
                        }
                    }
                    TableStatus.SEATED, TableStatus.CHECK_DROPPED -> {
                        Button(
                            onClick = onOpenOrder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandAmber,
                                contentColor = BrandInk
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "OPEN ORDER WORKSPACE", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onMarkDirty,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B242A),
                                contentColor = Color(0xFFFCA5A5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "MARK AS VACATED / DIRTY")
                        }
                    }
                    TableStatus.DIRTY -> {
                        Button(
                            onClick = onMarkAvailable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF166534),
                                contentColor = Color(0xFFBBF7D0)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "MARK CLEANED & AVAILABLE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE", color = BrandAmber)
            }
        }
    )
}
