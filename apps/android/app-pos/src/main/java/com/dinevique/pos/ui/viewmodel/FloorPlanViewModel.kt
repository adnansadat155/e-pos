package com.dinevique.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinevique.pos.ui.models.TableShape
import com.dinevique.pos.ui.models.TableStatus
import com.dinevique.pos.ui.models.TableUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloorPlanUiState(
    val selectedSection: String = "MAIN_DINING",
    val sections: List<String> = listOf("MAIN_DINING", "PATIO", "BAR"),
    val tables: List<TableUiModel> = emptyList(),
    val selectedTable: TableUiModel? = null,
    val currentEpochMs: Long = System.currentTimeMillis()
)

class FloorPlanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FloorPlanUiState())
    val uiState: StateFlow<FloorPlanUiState> = _uiState.asStateFlow()

    init {
        loadMockTables()
        startElapsedTimerTicker()
    }

    private fun loadMockTables() {
        val now = System.currentTimeMillis()
        val initialTables = listOf(
            TableUiModel("T1", "Table 1", "MAIN_DINING", 2, 2, TableShape.ROUND, TableStatus.SEATED, "Server Elena", now - 24 * 60 * 1000, "ord-101", 6450),
            TableUiModel("T2", "Table 2", "MAIN_DINING", 4, 4, TableShape.RECTANGLE, TableStatus.SEATED, "Server Elena", now - 48 * 60 * 1000, "ord-102", 12800),
            TableUiModel("T3", "Table 3", "MAIN_DINING", 4, 0, TableShape.RECTANGLE, TableStatus.AVAILABLE),
            TableUiModel("T4", "Table 4", "MAIN_DINING", 6, 5, TableShape.BOOTH, TableStatus.CHECK_DROPPED, "Server Marco", now - 65 * 60 * 1000, "ord-103", 21550),
            TableUiModel("T5", "Table 5", "MAIN_DINING", 2, 0, TableShape.ROUND, TableStatus.DIRTY, "Server Elena"),
            TableUiModel("T6", "Table 6", "MAIN_DINING", 8, 0, TableShape.BOOTH, TableStatus.AVAILABLE),

            TableUiModel("P1", "Patio 1", "PATIO", 4, 3, TableShape.ROUND, TableStatus.SEATED, "Server Marco", now - 12 * 60 * 1000, "ord-201", 4500),
            TableUiModel("P2", "Patio 2", "PATIO", 4, 0, TableShape.ROUND, TableStatus.AVAILABLE),
            TableUiModel("P3", "Patio 3", "PATIO", 6, 0, TableShape.RECTANGLE, TableStatus.AVAILABLE),

            TableUiModel("B1", "Bar 1", "BAR", 2, 1, TableShape.ROUND, TableStatus.SEATED, "Bartender Sam", now - 8 * 60 * 1000, "ord-301", 2200),
            TableUiModel("B2", "Bar 2", "BAR", 2, 0, TableShape.ROUND, TableStatus.AVAILABLE),
            TableUiModel("B3", "Bar 3", "BAR", 2, 2, TableShape.ROUND, TableStatus.CHECK_DROPPED, "Bartender Sam", now - 35 * 60 * 1000, "ord-302", 5600)
        )
        _uiState.update { it.copy(tables = initialTables) }
    }

    private fun startElapsedTimerTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(currentEpochMs = System.currentTimeMillis()) }
            }
        }
    }

    fun selectSection(section: String) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun selectTable(table: TableUiModel) {
        _uiState.update { it.copy(selectedTable = table) }
    }

    fun clearSelectedTable() {
        _uiState.update { it.copy(selectedTable = null) }
    }

    fun seatGuests(tableId: String, guests: Int, serverName: String = "Server Elena") {
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            val updated = state.tables.map { table ->
                if (table.tableId == tableId) {
                    table.copy(
                        status = TableStatus.SEATED,
                        currentGuests = guests,
                        serverName = serverName,
                        seatedAtEpochMs = now,
                        activeCheckTotalCents = 0
                    )
                } else table
            }
            state.copy(tables = updated)
        }
    }

    fun markTableDirty(tableId: String) {
        _uiState.update { state ->
            val updated = state.tables.map {
                if (it.tableId == tableId) it.copy(status = TableStatus.DIRTY, activeCheckUuid = null, activeCheckTotalCents = 0)
                else it
            }
            state.copy(tables = updated)
        }
    }

    fun markTableAvailable(tableId: String) {
        _uiState.update { state ->
            val updated = state.tables.map {
                if (it.tableId == tableId) it.copy(
                    status = TableStatus.AVAILABLE,
                    currentGuests = 0,
                    serverName = null,
                    seatedAtEpochMs = null,
                    activeCheckUuid = null,
                    activeCheckTotalCents = 0
                )
                else it
            }
            state.copy(tables = updated)
        }
    }
}
