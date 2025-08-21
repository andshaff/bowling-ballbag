package com.studio32b.ballbag.ui.arsenal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studio32b.ballbag.data.BowlingBall
import com.studio32b.ballbag.data.BowlingBallRepository
import com.studio32b.ballbag.data.entity.ArsenalBall
import com.studio32b.ballbag.data.entity.ArsenalBallUsageHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BaggedBall(
    val arsenalId: Long,
    val dateAdded: Long,
    val gamesUsed: Int,
    val ball: BowlingBall,
    val lastUsed: Long? = null,
    val lastResurfaced: Long? = null,
    val gamesSinceResurfaced: Int = 0,
    val lastOilExtraction: Long? = null,
    val gamesSinceOilExtraction: Int = 0,
    // Per-ball maintenance limits
    val resurfacingLowerLimit: Int = 60,
    val resurfacingUpperLimit: Int = 80,
    val oilExtractionLowerLimit: Int = 50,
    val oilExtractionUpperLimit: Int = 75
)

@HiltViewModel
class MyArsenalViewModel @Inject constructor(
    private val repository: BowlingBallRepository
) : ViewModel() {
    val baggedBalls: StateFlow<List<BaggedBall>> =
        combine(
            repository.getAllArsenalBallsRaw(),
            repository.getAllBallsFlow(),
            repository.getAllUsageHistories()
        ) { arsenalList, allBalls, allUsageHistories ->
            arsenalList.mapNotNull { ab ->
                val ball = allBalls.find { it.id == ab.ballId }
                val lastUsed = allUsageHistories
                    .filter { it.arsenalBallId == ab.id }
                    .maxByOrNull { it.date }?.date
                ball?.let {
                    BaggedBall(
                        ab.id,
                        ab.dateAdded,
                        ab.gamesUsed,
                        it,
                        lastUsed,
                        ab.lastResurfaced,
                        ab.gamesSinceResurfaced,
                        ab.lastOilExtraction,
                        ab.gamesSinceOilExtraction,
                        ab.resurfacingLowerLimit,
                        ab.resurfacingUpperLimit,
                        ab.oilExtractionLowerLimit,
                        ab.oilExtractionUpperLimit
                    )
                }
            }.sortedByDescending { it.gamesUsed } // Sort by games played descending
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun incrementGamesAndMaintenanceCounters(arsenalId: Long, gamesToAdd: Int) {
        viewModelScope.launch {
            repository.incrementGamesAndMaintenanceCounters(arsenalId, gamesToAdd)
        }
    }

    @Deprecated("Use incrementGamesAndMaintenanceCounters instead")
    fun updateGamesUsed(arsenalId: Long, gamesUsed: Int) {
        // Deprecated: use incrementGamesAndMaintenanceCounters for adding games
        viewModelScope.launch {
            repository.updateGamesUsed(arsenalId, gamesUsed)
        }
    }

    fun deleteBallFromBag(arsenalId: Long) {
        viewModelScope.launch {
            repository.deleteArsenalBallById(arsenalId)
        }
    }

    fun recordUsage(arsenalId: Long, currentGamesUsed: Int, gamesToAdd: Int, type: String) {
        viewModelScope.launch {
            repository.incrementGamesAndMaintenanceCounters(arsenalId, gamesToAdd)
            repository.insertUsageHistory(
                ArsenalBallUsageHistory(
                    arsenalBallId = arsenalId,
                    date = System.currentTimeMillis(),
                    gamesAdded = gamesToAdd,
                    type = type
                )
            )
        }
    }

    fun getUsageHistory(arsenalId: Long): Flow<List<ArsenalBallUsageHistory>> =
        repository.getUsageHistoryForBall(arsenalId)

    fun updateResurfaced(arsenalId: Long) {
        viewModelScope.launch {
            repository.updateResurfaced(
                arsenalId,
                System.currentTimeMillis(),
                0 // reset games since resurfaced
            )
        }
    }

    fun updateOilExtraction(arsenalId: Long) {
        viewModelScope.launch {
            repository.updateOilExtraction(
                arsenalId,
                System.currentTimeMillis(),
                0 // reset games since oil extraction
            )
        }
    }

    fun updateMaintenanceLimits(
        arsenalId: Long,
        resurfacingLowerLimit: Int,
        resurfacingUpperLimit: Int,
        oilExtractionLowerLimit: Int,
        oilExtractionUpperLimit: Int
    ) {
        viewModelScope.launch {
            repository.updateMaintenanceLimits(
                arsenalId,
                resurfacingLowerLimit,
                resurfacingUpperLimit,
                oilExtractionLowerLimit,
                oilExtractionUpperLimit
            )
        }
    }

    fun updateBallMaintenanceLimits(
        id: Long,
        resurfacingLowerLimit: Int,
        resurfacingUpperLimit: Int,
        oilExtractionLowerLimit: Int,
        oilExtractionUpperLimit: Int
    ) {
        viewModelScope.launch {
            repository.updateMaintenanceLimits(
                id,
                resurfacingLowerLimit,
                resurfacingUpperLimit,
                oilExtractionLowerLimit,
                oilExtractionUpperLimit
            )
        }
    }
}
