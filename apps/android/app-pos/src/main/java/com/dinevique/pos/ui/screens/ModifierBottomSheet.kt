package com.dinevique.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.dinevique.pos.ui.models.MenuItemUiModel
import com.dinevique.pos.ui.models.ModifierOptionUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierBottomSheet(
    item: MenuItemUiModel,
    selectedModifiersMap: Map<String, List<ModifierOptionUiModel>>,
    isValid: Boolean,
    onToggleOption: (groupId: String, option: ModifierOptionUiModel) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Calculate live item total
    val additionalCents = selectedModifiersMap.values.flatten().sumOf { it.priceDeltaCents }
    val totalCents = item.basePriceCents + additionalCents
    val formattedTotal = String.format("$%d.%02d", totalCents / 100, totalCents % 100)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrandSlate,
        scrimColor = Color(0xCC000000),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = BrandWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    if (item.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
                Text(
                    text = item.formattedPrice(),
                    color = BrandAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF2C3948))

            // Modifier Groups Scrollable Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item.modifierGroups.forEach { group ->
                    val selectedInGroup = selectedModifiersMap[group.groupId] ?: emptyList()
                    val isSatisfied = !group.isRequired || selectedInGroup.size >= group.minSelection

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertizontally
                        ) {
                            Text(
                                text = group.name,
                                color = BrandWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            val badgeText = if (group.isRequired) {
                                if (group.minSelection == 1 && group.maxSelection == 1) "Required • Select 1"
                                else "Required • Select ${group.minSelection}"
                            } else {
                                "Optional • Max ${group.maxSelection}"
                            }
                            Text(
                                text = badgeText,
                                color = if (isSatisfied) Color(0xFF94A3B8) else BrandAmber,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Options List
                        group.options.forEach { option ->
                            val isSelected = selectedInGroup.any { it.optionId == option.optionId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF263342) else Color(0xFF18222E))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrandAmber else Color(0xFF2C3948),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onToggleOption(group.groupId, option) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertizontally) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onToggleOption(group.groupId, option) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = BrandAmber,
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option.name,
                                        color = if (isSelected) BrandWhite else Color(0xFFCBD5E1),
                                        fontSize = 14.sp
                                    )
                                }

                                if (option.priceDeltaCents > 0) {
                                    Text(
                                        text = option.formattedPriceDelta(),
                                        color = BrandAmber,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF2C3948))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer Total & Confirm CTA Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Column {
                    Text(text = "ITEM TOTAL", color = Color.Gray, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(text = formattedTotal, color = BrandWhite, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }

                Button(
                    onClick = onConfirm,
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandAmber,
                        contentColor = BrandInk,
                        disabledContainerColor = Color(0xFF333333),
                        disabledContentColor = Color(0xFF777777)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(52.dp).width(200.dp)
                ) {
                    Text(
                        text = "ADD TO ORDER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
