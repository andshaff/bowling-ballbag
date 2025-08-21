package com.studio32b.ballbag.data.repository

import com.studio32b.ballbag.data.dao.*
import com.studio32b.ballbag.data.entity.*
import com.studio32b.ballbag.domain.model.*
import com.studio32b.ballbag.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeagueRepositoryImpl @Inject constructor(private val dao: LeagueDao) : LeagueRepository {
    override fun getAllLeagues(): Flow<List<League>> =
        dao.getAllLeagues().map { list -> list.map { it.toDomain() } }
    override suspend fun addLeague(league: League): Long =
        dao.insertLeague(league.toEntity())
}

class TeamRepositoryImpl @Inject constructor(private val dao: TeamDao) : TeamRepository {
    override fun getTeamsByLeague(leagueId: Long): Flow<List<Team>> =
        dao.getTeamsByLeague(leagueId).map { list -> list.map { it.toDomain() } }
    override suspend fun addTeam(team: Team): Long =
        dao.insertTeam(team.toEntity())
}

class BowlerRepositoryImpl @Inject constructor(private val dao: BowlerDao) : BowlerRepository {
    override fun getBowlersByTeam(teamId: Long): Flow<List<Bowler>> =
        dao.getBowlersByTeam(teamId).map { list -> list.map { it.toDomain() } }
    override suspend fun addBowler(bowler: Bowler): Long =
        dao.insertBowler(bowler.toEntity())
}

class SeriesRepositoryImpl @Inject constructor(private val dao: SeriesDao) : SeriesRepository {
    override fun getSeriesByBowler(bowlerId: Long): Flow<List<Series>> =
        dao.getSeriesByBowler(bowlerId).map { list -> list.map { it.toDomain() } }
    override suspend fun addSeries(series: Series): Long =
        dao.insertSeries(series.toEntity())
}

class GameScoreRepositoryImpl @Inject constructor(private val dao: GameScoreDao) : GameScoreRepository {
    override fun getScoresBySeries(seriesId: Long): Flow<List<GameScore>> =
        dao.getScoresBySeries(seriesId).map { list -> list.map { it.toDomain() } }
    override suspend fun addGameScore(score: GameScore): Long =
        dao.insertGameScore(score.toEntity())
}

class StatsRepositoryImpl @Inject constructor() : StatsRepository {
    override fun getBowlerStats(bowlerId: Long): Flow<BowlerStats> = flowOf(
        BowlerStats(
            bowlerId = bowlerId,
            average = 0f,
            highGame = 0,
            highSeries = 0,
            totalPins = 0,
            gamesPlayed = 0
        )
    )
    override fun getTeamStats(teamId: Long): Flow<BowlerStats> = flowOf(
        BowlerStats(
            bowlerId = teamId, // Placeholder, as TeamStats is not implemented
            average = 0f,
            highGame = 0,
            highSeries = 0,
            totalPins = 0,
            gamesPlayed = 0
        )
    )
}

// --- Entity <-> Domain mappers ---

fun LeagueEntity.toDomain() = League(id, name)
fun League.toEntity() = LeagueEntity(id, name)

fun TeamEntity.toDomain() = Team(id, leagueId, name)
fun Team.toEntity() = TeamEntity(id, leagueId, name)

fun BowlerEntity.toDomain() = Bowler(id, teamId, name, handicap)
fun Bowler.toEntity() = BowlerEntity(id, teamId, name, handicap)

fun SeriesEntity.toDomain() = Series(id, bowlerId, date)
fun Series.toEntity() = SeriesEntity(id, bowlerId, date)

fun GameScoreEntity.toDomain() = GameScore(id, seriesId, gameNumber, score)
fun GameScore.toEntity() = GameScoreEntity(id, seriesId, gameNumber, score)
