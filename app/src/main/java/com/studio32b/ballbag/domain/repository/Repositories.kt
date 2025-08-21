package com.studio32b.ballbag.domain.repository

import com.studio32b.ballbag.domain.model.*
import kotlinx.coroutines.flow.Flow

interface LeagueRepository {
    fun getAllLeagues(): Flow<List<League>>
    suspend fun addLeague(league: League): Long
}

interface TeamRepository {
    fun getTeamsByLeague(leagueId: Long): Flow<List<Team>>
    suspend fun addTeam(team: Team): Long
}

interface BowlerRepository {
    fun getBowlersByTeam(teamId: Long): Flow<List<Bowler>>
    suspend fun addBowler(bowler: Bowler): Long
}

interface SeriesRepository {
    fun getSeriesByBowler(bowlerId: Long): Flow<List<Series>>
    suspend fun addSeries(series: Series): Long
}

interface GameScoreRepository {
    fun getScoresBySeries(seriesId: Long): Flow<List<GameScore>>
    suspend fun addGameScore(score: GameScore): Long
}

interface StatsRepository {
    fun getBowlerStats(bowlerId: Long): Flow<BowlerStats>
    fun getTeamStats(teamId: Long): Flow<BowlerStats> // For simplicity, can be a TeamStats model
}

