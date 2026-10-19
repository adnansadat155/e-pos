package com.dinevique.handheld

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HandheldUiState(
    val currentOrder: HandheldDraftOrder = HandheldDraftOrder(),
    val selectedCategory: String = "POPULAR",
    val categories: List<Pair<String, String>> = listOf(
        "POPULAR" to "Popular",
        "MAINS" to "Mains",
        "DRINKS" to "Drinks",
        "DESSERTS" to "Desserts"
    ),
    val menuItems: List<HandheldMenuItem> = emptyList(),
    val pendingModifierItem: HandheldMenuItem? = null,
    val isModifierModalOpen: Boolean = false,
    val tapCount: Int = 0,
    val lastFiredTicketUuid: String? = null
)

class HandheldOrderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HandheldUiState())
    val uiState: StateFlow<HandheldUiState> = _uiState.asStateFlow()

    init {
        loadHandheldCatalog()
    }

    private fun loadHandheldCatalog() {
        val items = listOf(
            HandheldMenuItem(
                itemId = "h_ribeye",
                name = "USDA Prime Ribeye",
                categoryId = "MAINS",
                basePriceCents = 4800,
                requiredModifierGroup = HandheldModifierGroup(
                    groupId = "grp_temp",
                    name = "Meat Temperature",
                    options = listOf(
                        HandheldModifierOption("opt_rare", "Rare"),
                        HandheldModifierOption("opt_mr", "Medium Rare"),
                        HandheldModifierOption("opt_med", "Medium"),
                        HandheldModifierOption("opt_wd", "Well Done")
                    )
                )
            ),
            HandheldMenuItem(
                itemId = "h_salmon",
                name = "Pan-Roasted Salmon",
                categoryId = "MAINS",
                basePriceCents = 3800,
                requiredModifierGroup = HandheldModifierGroup(
                    groupId = "grp_doneness",
                    name = "Doneness",
                    options = listOf(
                        HandheldModifierOption("opt_med", "Medium"),
                        HandheldModifierOption("opt_well", "Well Done")
                    )
                )
            ),
            HandheldMenuItem(
                itemId = "h_burger",
                name = "Dinevique Prime Burger",
                categoryId = "POPULAR",
                basePriceCents = 2400,
                requiredModifierGroup = HandheldModifierGroup(
                    groupId = "grp_cheese",
                    name = "Cheese Choice",
                    options = listOf(
                        HandheldModifierOption("opt_cheddar", "Aged White Cheddar"),
                        HandheldModifierOption("opt_swiss", "Gruyere Swiss"),
                        HandheldModifierOption("opt_none", "No Cheese")
                    )
                )
            ),
            HandheldMenuItem(
                itemId = "h_truffle_fries",
                name = "Truffle Parmesan Fries",
                categoryId = "POPULAR",
                basePriceCents = 1400
            ),
            HandheldMenuItem(
                itemId = "h_old_fashioned",
                name = "Smoked Bourbon Old Fashioned",
                categoryId = "DRINKS",
                basePriceCents = 1800
            ),
            HandheldMenuItem(
                itemId = "h_espresso",
                name = "Double Espresso",
                categoryId = "DRINKS",
                basePriceCents = 600
            ),
            HandheldMenuItem(
                itemId = "h_fondant",
                name = "Valrhona Chocolate Fondant",
                categoryId = "DESSERTS",
                basePriceCents = 1600
            )
        )
        _uiState.update { it.copy(menuItems = items) }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
    }

    // Tap 1: Tap item
    fun onItemTapped(item: HandheldMenuItem) {
        if (item.requiredModifierGroup != null) {
            _uiState.update {
                it.copy(
                    pendingModifierItem = item,
                    isModifierModalOpen = true,
                    tapCount = it.tapCount + 1
                )
            }
        } else {
            // Direct addition
            val draftItem = HandheldDraftItem(menuItem = item)
            _uiState.update { state ->
                val updatedItems = state.currentOrder.items + draftItem
                state.copy(
                    currentOrder = state.currentOrder.copy(items = updatedItems),
                    tapCount = state.tapCount + 1
                )
            }
        }
    }

    // Tap 2: Select required modifier
    fun onModifierSelected(option: HandheldModifierOption) {
        val pending = _uiState.value.pendingModifierItem ?: return
        val draftItem = HandheldDraftItem(
            menuItem = pending,
            selectedModifier = option
        )
        _uiState.update { state ->
            val updatedItems = state.currentOrder.items + draftItem
            state.copy(
                currentOrder = state.currentOrder.copy(items = updatedItems),
                isModifierModalOpen = false,
                pendingModifierItem = null,
                tapCount = state.tapCount + 1
            )
        }
    }

    fun dismissModifierModal() {
        _uiState.update { it.copy(isModifierModalOpen = false, pendingModifierItem = null) }
    }

    // Tap 3: Fire to Kitchen via Master Terminal LAN WebSocket
    fun fireOrderToMaster(): String {
        val firedUuid = _uiState.value.currentOrder.clientOrderUuid
        _uiState.update { state ->
            state.copy(
                tapCount = state.tapCount + 1,
                lastFiredTicketUuid = firedUuid,
                currentOrder = HandheldDraftOrder() // Reset order
            )
        }
        return firedUuid
    }
}
