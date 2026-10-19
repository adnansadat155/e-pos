package com.dinevique.pos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dinevique.pos.ui.models.TableUiModel
import com.dinevique.pos.ui.screens.FloorPlanScreen
import com.dinevique.pos.ui.screens.OrderWorkspaceScreen
import com.dinevique.pos.ui.viewmodel.FloorPlanViewModel
import com.dinevique.pos.ui.viewmodel.OrderWorkspaceViewModel

// Dinevique Brand Color Tokens
val BrandOnyx = Color(0xFF0D0D0D)
val BrandAmber = Color(0xFFD4820A)
val BrandSlate = Color(0xFF1E2A38)
val BrandInk = Color(0xFF1A1A1A)
val BrandWhite = Color(0xFFFFFFFF)

enum class PosNavDestination {
    PIN_LOCK,
    FLOOR_PLAN,
    ORDER_WORKSPACE
}

class PosMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DineviquePosTheme {
                PosAppNavigator()
            }
        }
    }
}

@Composable
fun DineviquePosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BrandOnyx,
            surface = BrandSlate,
            primary = BrandAmber,
            onPrimary = BrandInk
        ),
        content = content
    )
}

@Composable
fun PosAppNavigator() {
    var currentScreen by remember { mutableStateOf(PosNavDestination.PIN_LOCK) }
    var activeTableForOrder by remember { mutableStateOf<TableUiModel?>(null) }
    val floorPlanViewModel: FloorPlanViewModel = viewModel()

    when (currentScreen) {
        PosNavDestination.PIN_LOCK -> {
            PinLockScreen(
                onPinSuccess = {
                    currentScreen = PosNavDestination.FLOOR_PLAN
                }
            )
        }
        PosNavDestination.FLOOR_PLAN -> {
            FloorPlanScreen(
                viewModel = floorPlanViewModel,
                onTableSelectedForOrder = { table ->
                    activeTableForOrder = table
                    currentScreen = PosNavDestination.ORDER_WORKSPACE
                },
                onLockRequested = {
                    currentScreen = PosNavDestination.PIN_LOCK
                }
            )
        }
        PosNavDestination.ORDER_WORKSPACE -> {
            val table = activeTableForOrder ?: TableUiModel("T1", "Table 1", "MAIN_DINING", 4, 2, com.dinevique.pos.ui.models.TableShape.RECTANGLE, com.dinevique.pos.ui.models.TableStatus.SEATED)
            val orderWorkspaceViewModel: OrderWorkspaceViewModel = viewModel(
                key = "OrderWorkspace_${table.tableId}",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return OrderWorkspaceViewModel(
                            tableId = table.tableId,
                            tableLabel = table.tableLabel,
                            serverName = table.serverName ?: "Server Elena"
                        ) as T
                    }
                }
            )

            OrderWorkspaceScreen(
                viewModel = orderWorkspaceViewModel,
                onBackToFloorPlan = {
                    currentScreen = PosNavDestination.FLOOR_PLAN
                }
            )
        }
    }
}

@Composable
fun PinLockScreen(
    onPinSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandOnyx
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DINEVIQUE POS",
                color = BrandWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter team PIN to unlock terminal",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // PIN Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                (0 until 4).forEach { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) BrandAmber else Color(0xFF2C3948)
                            )
                    )
                }
            }

            if (isError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Invalid PIN. Try again.",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad (48x48dp min touch target)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLEAR", "0", "ENTER")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keypad.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { key ->
                            Surface(
                                color = if (key == "ENTER") BrandAmber else Color(0xFF1E2A38),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        when (key) {
                                            "CLEAR" -> {
                                                enteredPin = ""
                                                isError = false
                                            }
                                            "ENTER" -> {
                                                if (enteredPin == "1234" || enteredPin.length >= 4) {
                                                    onPinSuccess()
                                                } else {
                                                    isError = true
                                                    enteredPin = ""
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    isError = false
                                                    if (enteredPin.length == 4) {
                                                        // Auto submit on 4 digits
                                                        onPinSuccess()
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        color = if (key == "ENTER") BrandInk else BrandWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (key.length > 1) 12.sp else 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
