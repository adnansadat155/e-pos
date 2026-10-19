package com.dinevique.handheld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

val BrandOnyx = Color(0xFF0D0D0D)
val BrandAmber = Color(0xFFD4820A)
val BrandSlate = Color(0xFF1E2A38)
val BrandInk = Color(0xFF1A1A1A)
val BrandWhite = Color(0xFFFFFFFF)

class HandheldMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DineviqueHandheldTheme {
                val viewModel: HandheldOrderViewModel = viewModel()
                HandheldOrderScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DineviqueHandheldTheme(content: @Composable () -> Unit) {
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
