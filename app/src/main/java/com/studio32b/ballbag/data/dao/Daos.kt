package com.studio32b.ballbag.data.dao

import androidx.room.*
import com.studio32b.ballbag.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LeagueDao {
    @Query("SELECT * FROM LeagueEntity")
    fun getAllLeagues(): Flow<List<LeagueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeague(league: LeagueEntity): Long
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM TeamEntity WHERE leagueId = :leagueId")
    fun getTeamsByLeague(leagueId: Long): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long
}

@Dao
interface BowlerDao {
    @Query("SELECT * FROM BowlerEntity WHERE teamId = :teamId")
    fun getBowlersByTeam(teamId: Long): Flow<List<BowlerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBowler(bowler: BowlerEntity): Long
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM SeriesEntity WHERE bowlerId = :bowlerId")
    fun getSeriesByBowler(bowlerId: Long): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity): Long
}

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM GameScoreEntity WHERE seriesId = :seriesId")
    fun getScoresBySeries(seriesId: Long): Flow<List<GameScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameScore(score: GameScoreEntity): Long
}
