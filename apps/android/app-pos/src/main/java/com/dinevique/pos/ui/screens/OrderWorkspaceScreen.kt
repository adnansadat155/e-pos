package com.dinevique.pos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinevique.pos.ui.*
import com.dinevique.pos.ui.engine.CourseCategory
import com.dinevique.pos.ui.models.CourseHoldState
import com.dinevique.pos.ui.models.DraftOrderItem
import com.dinevique.pos.ui.models.MenuItemUiModel
import com.dinevique.pos.ui.viewmodel.OrderWorkspaceViewModel

@Composable
fun OrderWorkspaceScreen(
    viewModel: OrderWorkspaceViewModel,
    onBackToFloorPlan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandOnyx)
    ) {
        // Left Pane: Active Check (40% width / 420dp)
        ActiveCheckPane(
            uiState = uiState,
            onBackToFloorPlan = onBackToFloorPlan,
            onSelectSeat = { viewModel.selectSeat(it) },
            onToggleHold = { viewModel.toggleItemCourseHold(it) },
            onHoldAll = { viewModel.holdAllItems() },
            onFireAll = { viewModel.fireAllItems() },
            onFireCourse = { viewModel.fireCourse(it) },
            onOpenSplitCheck = { viewModel.openSplitCheckModal() },
            onRemoveItem = { viewModel.removeItem(it) },
            onFireOrder = {
                viewModel.fireOrder()
                onBackToFloorPlan()
            },
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
                .background(BrandSlate)
                .border(width = 1.dp, color = Color(0xFF2C3948))
        )

        // Right Pane: Catalog Grid (60% width)
        CatalogPane(
            uiState = uiState,
            onSelectCategory = { viewModel.selectCategory(it) },
            onMenuItemClick = { viewModel.onMenuItemClicked(it) },
            onMenuItemLongClick = { viewModel.togglePass86(it) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }

    // Modifier Bottom Sheet Modal
    if (uiState.isModifierSheetOpen && uiState.pendingItem != null) {
        ModifierBottomSheet(
            item = uiState.pendingItem!!,
            selectedModifiersMap = uiState.selectedModifiersMap,
            isValid = uiState.isModifierSelectionValid,
            onToggleOption = { grpId, opt -> viewModel.toggleModifierOption(grpId, opt) },
            onConfirm = { viewModel.confirmPendingModifiers() },
            onDismiss = { viewModel.dismissModifierSheet() }
        )
    }

    // Split Check Modal
    if (uiState.isSplitCheckModalOpen) {
        SplitCheckModal(
            draftCheck = uiState.activeCheck,
            onDismiss = { viewModel.dismissSplitCheckModal() },
            onPaymentCompleted = { checkUuid, tender ->
                // Payment logged
            }
        )
    }
}

@Composable
fun ActiveCheckPane(
    uiState: com.dinevique.pos.ui.viewmodel.OrderWorkspaceUiState,
    onBackToFloorPlan: () -> Unit,
    onSelectSeat: (Int) -> Unit,
    onToggleHold: (String) -> Unit,
    onHoldAll: () -> Unit,
    onFireAll: () -> Unit,
    onFireCourse: (CourseCategory) -> Unit,
    onOpenSplitCheck: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onFireOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val check = uiState.activeCheck

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            // Header: Table, Server, Back CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                IconButton(onClick = onBackToFloorPlan) {
                    Text(text = "←", color = BrandWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = check.tableLabel,
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${check.serverName} • ${check.guestCount} Guests",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    color = Color(0xFF263342),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "OPEN",
                        color = BrandAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seat Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items((1..check.guestCount).toList()) { seat ->
                    val isSelected = seat == uiState.currentSelectedSeat
                    Surface(
                        color = if (isSelected) BrandAmber else Color(0xFF263342),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectSeat(seat) }
                    ) {
                        Text(
                            text = "Seat $seat",
                            color = if (isSelected) BrandInk else BrandWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coursing Quick Bar (Hold All / Fire Courses)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xFF382618),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onHoldAll)
                ) {
                    Text(
                        text = "HOLD ALL",
                        color = BrandAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF143825),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onFireAll)
                ) {
                    Text(
                        text = "FIRE ALL",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF202A36),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onFireCourse(CourseCategory.ENTREE) }
                ) {
                    Text(
                        text = "FIRE ENTREES",
                        color = BrandWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFF2C3948))
            Spacer(modifier = Modifier.height(8.dp))

            // Order Items Scrollable List
            if (check.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items added yet.\nSelect items from the right menu.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(check.items, key = { it.itemInstanceUuid }) { item ->
                        CheckItemRow(
                            item = item,
                            onToggleHold = { onToggleHold(item.itemInstanceUuid) },
                            onRemove = { onRemoveItem(item.itemInstanceUuid) }
                        )
                    }
                }
            }
        }

        // Summary Calculations & Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Divider(color = Color(0xFF2C3948))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Subtotal", color = Color.LightGray, fontSize = 13.sp)
                Text(text = check.formattedSubtotal(), color = BrandWhite, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Tax (${check.taxRatePercent}%)", color = Color.LightGray, fontSize = 13.sp)
                Text(text = check.formattedTax(), color = BrandWhite, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total", color = BrandWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = check.formattedTotal(), color = BrandAmber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Split Check & Fire Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSplitCheck,
                    enabled = check.items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF263342),
                        contentColor = BrandWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(text = "SPLIT CHECK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onFireOrder,
                    enabled = check.items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandAmber,
                        contentColor = BrandInk,
                        disabledContainerColor = Color(0xFF333333),
                        disabledContentColor = Color(0xFF777777)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(50.dp)
                ) {
                    Text(
                        text = "FIRE ORDER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CheckItemRow(
    item: DraftOrderItem,
    onToggleHold: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = Color(0xFF141E28),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color(0xFF233547), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertizontally) {
                    Text(
                        text = "${item.quantity}x",
                        color = BrandAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.menuItem.name,
                        color = BrandWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = item.formattedItemTotal(),
                    color = BrandWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // Modifier Chips
            if (item.selectedModifiers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.padding(start = 24.dp)) {
                    Text(
                        text = item.selectedModifiers.joinToString(" • ") { it.optionName },
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // Seat Badge & Course Hold Action
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Surface(
                    color = Color(0xFF263342),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Seat ${item.seatNumber}",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertizontally) {
                    val isHold = item.courseHold == CourseHoldState.HOLD
                    Surface(
                        color = if (isHold) Color(0xFF4A3419) else Color(0xFF193B2B),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onToggleHold)
                    ) {
                        Text(
                            text = if (isHold) "HOLD" else "FIRE",
                            color = if (isHold) BrandAmber else Color(0xFF4ADE80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "✕",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(onClick = onRemove)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogPane(
    uiState: com.dinevique.pos.ui.viewmodel.OrderWorkspaceUiState,
    onSelectCategory: (String) -> Unit,
    onMenuItemClick: (MenuItemUiModel) -> Unit,
    onMenuItemLongClick: (MenuItemUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Category Navigation Tabs Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.categories) { (id, title) ->
                val isSelected = id == uiState.selectedCategoryId
                Surface(
                    color = if (isSelected) BrandAmber else BrandSlate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(id) }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandAmber else Color(0xFF2C3948),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) BrandInk else BrandWhite,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items Grid
        val filteredItems = uiState.menuItems.filter { it.categoryId == uiState.selectedCategoryId }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems, key = { it.itemId }) { item ->
                val is86d = uiState.unavailable86ItemIds.contains(item.itemId)
                ProductCard(
                    item = item,
                    is86d = is86d,
                    onClick = { onMenuItemClick(item) },
                    onLongClick = { onMenuItemLongClick(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    item: MenuItemUiModel,
    is86d: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        color = if (is86d) Color(0xFF171717) else BrandSlate,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = 1.dp,
                color = if (is86d) Color(0xFF8B263E) else Color(0xFF2C3948),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Prep Station Badge & 86 Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Surface(
                    color = Color(0xFF263342),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.prepStation,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (is86d) {
                    Surface(
                        color = Color(0xFF7F1D1D),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "86'D",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (item.modifierGroups.isNotEmpty()) {
                    Text(
                        text = "${item.modifierGroups.size} choices",
                        color = BrandAmber,
                        fontSize = 11.sp
                    )
                }
            }

            // Middle: Name
            Text(
                text = item.name,
                color = if (is86d) Color.Gray else BrandWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                textDecoration = if (is86d) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2
            )

            // Bottom: Price
            Text(
                text = item.formattedPrice(),
                color = if (is86d) Color.Gray else BrandAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
