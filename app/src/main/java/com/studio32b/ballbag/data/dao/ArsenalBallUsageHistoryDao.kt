package com.studio32b.ballbag.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.studio32b.ballbag.data.entity.ArsenalBallUsageHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ArsenalBallUsageHistoryDao {
    @Insert
    suspend fun insertUsage(history: ArsenalBallUsageHistory)

    @Query("SELECT * FROM arsenal_ball_usage_history WHERE arsenalBallId = :arsenalBallId ORDER BY date DESC")
    fun getUsageHistoryForBall(arsenalBallId: Long): Flow<List<ArsenalBallUsageHistory>>

    @Query("SELECT * FROM arsenal_ball_usage_history")
    fun getAllUsageHistories(): Flow<List<ArsenalBallUsageHistory>>
}
