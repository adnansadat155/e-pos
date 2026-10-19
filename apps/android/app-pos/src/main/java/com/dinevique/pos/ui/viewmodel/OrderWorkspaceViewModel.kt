package com.dinevique.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinevique.pos.ui.engine.*
import com.dinevique.pos.ui.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class OrderWorkspaceUiState(
    val activeCheck: DraftCheck,
    val categories: List<Pair<String, String>> = listOf(
        "cat_apps" to "Appetizers",
        "cat_mains" to "Entrees & Steaks",
        "cat_pasta" to "Pastas & Risottos",
        "cat_desserts" to "Desserts",
        "cat_drinks" to "Beverages"
    ),
    val selectedCategoryId: String = "cat_mains",
    val menuItems: List<MenuItemUiModel> = emptyList(),
    val unavailable86ItemIds: Set<String> = emptySet(),
    // Modifier bottom sheet state
    val isModifierSheetOpen: Boolean = false,
    val pendingItem: MenuItemUiModel? = null,
    val selectedModifiersMap: Map<String, List<ModifierOptionUiModel>> = emptyMap(),
    val isModifierSelectionValid: Boolean = false,
    val currentSelectedSeat: Int = 1,
    // Split check state
    val isSplitCheckModalOpen: Boolean = false,
    val lastFiredOrderUuid: String? = null
)

class OrderWorkspaceViewModel(
    tableId: String = "T1",
    tableLabel: String = "Table 1",
    serverName: String = "Server Elena"
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OrderWorkspaceUiState(
            activeCheck = DraftCheck(
                tableId = tableId,
                tableLabel = tableLabel,
                serverName = serverName
            )
        )
    )
    val uiState: StateFlow<OrderWorkspaceUiState> = _uiState.asStateFlow()

    init {
        loadMockCatalog()
        observe86State()
    }

    private fun observe86State() {
        viewModelScope.launch {
            Pass86Manager.unavailableItemIds.collect { ids ->
                _uiState.update { it.copy(unavailable86ItemIds = ids) }
            }
        }
    }

    private fun loadMockCatalog() {
        val items = listOf(
            // Mains
            MenuItemUiModel(
                itemId = "item_ribeye",
                categoryId = "cat_mains",
                name = "USDA Prime Ribeye 14oz",
                description = "Cast-iron seared prime beef, roasted garlic herb butter, demi-glace.",
                basePriceCents = 4800,
                prepStation = "GRILL",
                modifierGroups = listOf(
                    ModifierGroupUiModel(
                        groupId = "grp_temp",
                        name = "Meat Temperature",
                        minSelection = 1,
                        maxSelection = 1,
                        isRequired = true,
                        options = listOf(
                            ModifierOptionUiModel("opt_rare", "Rare"),
                            ModifierOptionUiModel("opt_med_rare", "Medium Rare"),
                            ModifierOptionUiModel("opt_medium", "Medium"),
                            ModifierOptionUiModel("opt_med_well", "Medium Well"),
                            ModifierOptionUiModel("opt_well", "Well Done")
                        )
                    ),
                    ModifierGroupUiModel(
                        groupId = "grp_sides",
                        name = "Choice of Side",
                        minSelection = 1,
                        maxSelection = 1,
                        isRequired = true,
                        options = listOf(
                            ModifierOptionUiModel("opt_truffle_fries", "Truffle Parmesan Fries", 300),
                            ModifierOptionUiModel("opt_asp", "Grilled Asparagus", 200),
                            ModifierOptionUiModel("opt_mash", "Yukon Gold Mashed Potatoes", 0)
                        )
                    ),
                    ModifierGroupUiModel(
                        groupId = "grp_addons",
                        name = "Enhancements",
                        minSelection = 0,
                        maxSelection = 3,
                        isRequired = false,
                        options = listOf(
                            ModifierOptionUiModel("opt_shrimp", "Jumbo Garlic Prawns (3)", 1200),
                            ModifierOptionUiModel("opt_bleu", "Melted Bleu Cheese Crust", 400),
                            ModifierOptionUiModel("opt_mush", "Wild Foraged Mushrooms", 500)
                        )
                    )
                )
            ),
            MenuItemUiModel(
                itemId = "item_salmon",
                categoryId = "cat_mains",
                name = "Pan-Roasted King Salmon",
                description = "Wild Alaskan salmon, saffron fennel broth, baby fingerling potatoes.",
                basePriceCents = 3800,
                prepStation = "GRILL",
                modifierGroups = listOf(
                    ModifierGroupUiModel(
                        groupId = "grp_doneness",
                        name = "Cooking Level",
                        minSelection = 1,
                        maxSelection = 1,
                        isRequired = true,
                        options = listOf(
                            ModifierOptionUiModel("opt_sal_med", "Medium (Recommended)"),
                            ModifierOptionUiModel("opt_sal_well", "Well Done")
                        )
                    )
                )
            ),
            // Appetizers
            MenuItemUiModel(
                itemId = "item_carpaccio",
                categoryId = "cat_apps",
                name = "Wagyu Beef Carpaccio",
                description = "Capers, shaved Parmigiano-Reggiano, white truffle oil, crostini.",
                basePriceCents = 2200,
                prepStation = "SALAD"
            ),
            MenuItemUiModel(
                itemId = "item_calamari",
                categoryId = "cat_apps",
                name = "Crispy Point Judith Calamari",
                description = "Cherry pepper aioli, charred lemon, fresh oregano.",
                basePriceCents = 1900,
                prepStation = "FRY"
            ),
            // Pastas
            MenuItemUiModel(
                itemId = "item_tagliatelle",
                categoryId = "cat_pasta",
                name = "Handmade Tagliatelle Bolognese",
                description = "12-hour braised veal & pork ragu, aged pecorino.",
                basePriceCents = 3200,
                prepStation = "EXPO"
            ),
            // Desserts
            MenuItemUiModel(
                itemId = "item_fondant",
                categoryId = "cat_desserts",
                name = "Warm Valrhona Chocolate Fondant",
                description = "Madagascar vanilla bean gelato, salted caramel brittle.",
                basePriceCents = 1600,
                prepStation = "PASTRY"
            ),
            // Drinks
            MenuItemUiModel(
                itemId = "item_old_fashioned",
                categoryId = "cat_drinks",
                name = "Smoked Bourbon Old Fashioned",
                description = "Woodford Reserve, Demerara, Angostura, hickory smoke.",
                basePriceCents = 1800,
                prepStation = "BAR"
            ),
            MenuItemUiModel(
                itemId = "item_sparkling",
                categoryId = "cat_drinks",
                name = "San Pellegrino 750ml",
                description = "Italian sparkling natural mineral water.",
                basePriceCents = 800,
                prepStation = "BAR"
            )
        )
        _uiState.update { it.copy(menuItems = items) }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun selectSeat(seatNumber: Int) {
        _uiState.update { it.copy(currentSelectedSeat = seatNumber) }
    }

    fun onMenuItemClicked(item: MenuItemUiModel) {
        // Block if 86'd
        if (_uiState.value.unavailable86ItemIds.contains(item.itemId)) {
            return
        }

        if (item.modifierGroups.isEmpty()) {
            addItemDirectly(item)
        } else {
            val initialMap = mutableMapOf<String, List<ModifierOptionUiModel>>()
            item.modifierGroups.forEach { grp ->
                if (grp.isRequired && grp.minSelection == 1 && grp.options.isNotEmpty()) {
                    initialMap[grp.groupId] = listOf(grp.options.first())
                }
            }
            val valid = validateModifiers(item, initialMap)
            _uiState.update {
                it.copy(
                    isModifierSheetOpen = true,
                    pendingItem = item,
                    selectedModifiersMap = initialMap,
                    isModifierSelectionValid = valid
                )
            }
        }
    }

    private fun addItemDirectly(item: MenuItemUiModel) {
        val draftItem = DraftOrderItem(
            menuItem = item,
            quantity = 1,
            seatNumber = _uiState.value.currentSelectedSeat,
            courseHold = CourseHoldState.FIRE
        )
        _uiState.update { state ->
            val updatedCheck = state.activeCheck.copy(
                items = state.activeCheck.items + draftItem
            )
            state.copy(activeCheck = updatedCheck)
        }
    }

    fun toggleModifierOption(groupId: String, option: ModifierOptionUiModel) {
        val pending = _uiState.value.pendingItem ?: return
        val currentMap = _uiState.value.selectedModifiersMap.toMutableMap()
        val grp = pending.modifierGroups.find { it.groupId == groupId } ?: return
        val currentList = currentMap[groupId] ?: emptyList()

        if (grp.maxSelection == 1) {
            currentMap[groupId] = listOf(option)
        } else {
            val exists = currentList.any { it.optionId == option.optionId }
            if (exists) {
                currentMap[groupId] = currentList.filter { it.optionId != option.optionId }
            } else {
                if (currentList.size < grp.maxSelection) {
                    currentMap[groupId] = currentList + option
                }
            }
        }

        val valid = validateModifiers(pending, currentMap)
        _uiState.update {
            it.copy(
                selectedModifiersMap = currentMap,
                isModifierSelectionValid = valid
            )
        }
    }

    private fun validateModifiers(item: MenuItemUiModel, selections: Map<String, List<ModifierOptionUiModel>>): Boolean {
        for (grp in item.modifierGroups) {
            val selectedCount = selections[grp.groupId]?.size ?: 0
            if (grp.isRequired && selectedCount < grp.minSelection) return false
            if (selectedCount > grp.maxSelection) return false
        }
        return true
    }

    fun confirmPendingModifiers() {
        val pending = _uiState.value.pendingItem ?: return
        if (!_uiState.value.isModifierSelectionValid) return

        val selectedMods = mutableListOf<SelectedModifierItem>()
        _uiState.value.selectedModifiersMap.forEach { (grpId, opts) ->
            val grp = pending.modifierGroups.find { it.groupId == grpId }
            val grpName = grp?.name ?: ""
            opts.forEach { opt ->
                selectedMods.add(
                    SelectedModifierItem(
                        groupId = grpId,
                        groupName = grpName,
                        optionId = opt.optionId,
                        optionName = opt.name,
                        priceDeltaCents = opt.priceDeltaCents
                    )
                )
            }
        }

        val draftItem = DraftOrderItem(
            menuItem = pending,
            quantity = 1,
            seatNumber = _uiState.value.currentSelectedSeat,
            courseHold = CourseHoldState.FIRE,
            selectedModifiers = selectedMods
        )

        _uiState.update { state ->
            val updatedCheck = state.activeCheck.copy(
                items = state.activeCheck.items + draftItem
            )
            state.copy(
                activeCheck = updatedCheck,
                isModifierSheetOpen = false,
                pendingItem = null,
                selectedModifiersMap = emptyMap()
            )
        }
    }

    fun dismissModifierSheet() {
        _uiState.update {
            it.copy(
                isModifierSheetOpen = false,
                pendingItem = null,
                selectedModifiersMap = emptyMap()
            )
        }
    }

    fun toggleItemCourseHold(itemInstanceUuid: String) {
        _uiState.update { state ->
            val updatedItems = state.activeCheck.items.map {
                if (it.itemInstanceUuid == itemInstanceUuid) {
                    val nextState = if (it.courseHold == CourseHoldState.FIRE) CourseHoldState.HOLD else CourseHoldState.FIRE
                    it.copy(courseHold = nextState)
                } else it
            }
            state.copy(activeCheck = state.activeCheck.copy(items = updatedItems))
        }
    }

    fun holdAllItems() {
        _uiState.update { state ->
            val updatedItems = state.activeCheck.items.map { it.copy(courseHold = CourseHoldState.HOLD) }
            state.copy(activeCheck = state.activeCheck.copy(items = updatedItems))
        }
    }

    fun fireAllItems() {
        _uiState.update { state ->
            val updatedItems = state.activeCheck.items.map { it.copy(courseHold = CourseHoldState.FIRE) }
            state.copy(activeCheck = state.activeCheck.copy(items = updatedItems))
        }
    }

    fun fireCourse(course: CourseCategory): CourseFiredEventPayload? {
        val check = _uiState.value.activeCheck
        val (updatedItems, payload) = CoursingEngine.fireCourse(
            orderUuid = check.clientOrderUuid,
            tableLabel = check.tableLabel,
            course = course,
            allItems = check.items
        )
        _uiState.update { state ->
            state.copy(activeCheck = state.activeCheck.copy(items = updatedItems))
        }
        return payload
    }

    fun togglePass86(item: MenuItemUiModel) {
        val isCurrently86d = _uiState.value.unavailable86ItemIds.contains(item.itemId)
        Pass86Manager.toggle86Item(item.itemId, item.name, isAvailable = isCurrently86d)
    }

    fun removeItem(itemInstanceUuid: String) {
        _uiState.update { state ->
            val updatedItems = state.activeCheck.items.filter { it.itemInstanceUuid != itemInstanceUuid }
            state.copy(activeCheck = state.activeCheck.copy(items = updatedItems))
        }
    }

    fun openSplitCheckModal() {
        _uiState.update { it.copy(isSplitCheckModalOpen = true) }
    }

    fun dismissSplitCheckModal() {
        _uiState.update { it.copy(isSplitCheckModalOpen = false) }
    }

    fun fireOrder(): String {
        val firedOrderUuid = _uiState.value.activeCheck.clientOrderUuid
        _uiState.update { state ->
            state.copy(lastFiredOrderUuid = firedOrderUuid)
        }
        return firedOrderUuid
    }
}
