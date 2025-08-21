package com.studio32b.ballbag.data.dao

import androidx.room.*
import com.studio32b.ballbag.data.entity.ArsenalBall
import kotlinx.coroutines.flow.Flow

@Dao
interface ArsenalBallDao {
    @Insert
    suspend fun addToArsenal(ball: ArsenalBall)

    @Delete
    suspend fun removeFromArsenal(ball: ArsenalBall)

    @Query("SELECT COUNT(*) FROM arsenal_balls WHERE ballId = :ballId")
    fun countInArsenal(ballId: Int): Flow<Int>

    @Query("SELECT * FROM arsenal_balls")
    fun getAllArsenalBalls(): Flow<List<ArsenalBall>>

    @Query("SELECT ballId FROM arsenal_balls")
    fun getAllArsenalBallIds(): Flow<List<Int>>

    @Query("UPDATE arsenal_balls SET games_used = :gamesUsed WHERE id = :id")
    suspend fun updateGamesUsed(id: Long, gamesUsed: Int)

    @Query("DELETE FROM arsenal_balls WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE arsenal_balls SET last_resurfaced = :lastResurfaced, games_since_resurfaced = :gamesSinceResurfaced WHERE id = :id")
    suspend fun updateResurfaced(id: Long, lastResurfaced: Long, gamesSinceResurfaced: Int)

    @Query("UPDATE arsenal_balls SET last_oil_extraction = :lastOilExtraction, games_since_oil_extraction = :gamesSinceOilExtraction WHERE id = :id")
    suspend fun updateOilExtraction(id: Long, lastOilExtraction: Long, gamesSinceOilExtraction: Int)

    @Query("""
        UPDATE arsenal_balls SET
            resurfacing_lower_limit = :resurfacingLowerLimit,
            resurfacing_upper_limit = :resurfacingUpperLimit,
            oil_extraction_lower_limit = :oilExtractionLowerLimit,
            oil_extraction_upper_limit = :oilExtractionUpperLimit
        WHERE id = :id
    """)
    suspend fun updateMaintenanceLimits(
        id: Long,
        resurfacingLowerLimit: Int,
        resurfacingUpperLimit: Int,
        oilExtractionLowerLimit: Int,
        oilExtractionUpperLimit: Int
    )

    @Query("""
        UPDATE arsenal_balls SET
            games_used = games_used + :gamesToAdd,
            games_since_resurfaced = games_since_resurfaced + :gamesToAdd,
            games_since_oil_extraction = games_since_oil_extraction + :gamesToAdd
        WHERE id = :id
    """)
    suspend fun incrementGamesAndMaintenanceCounters(id: Long, gamesToAdd: Int)

    @Query("UPDATE arsenal_balls SET games_used = :gamesUsed, last_used = :lastUsed WHERE id = :id")
    suspend fun updateGamesUsedAndLastUsed(id: Long, gamesUsed: Int, lastUsed: Long?)
}
