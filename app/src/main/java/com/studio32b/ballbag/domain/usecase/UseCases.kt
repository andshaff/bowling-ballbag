package com.studio32b.ballbag.domain.usecase

import com.studio32b.ballbag.domain.model.*
import com.studio32b.ballbag.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddLeagueUseCase @Inject constructor(private val repo: LeagueRepository) {
    suspend operator fun invoke(league: League) = repo.addLeague(league)
}

class GetLeaguesUseCase @Inject constructor(private val repo: LeagueRepository) {
    operator fun invoke(): Flow<List<League>> = repo.getAllLeagues()
}

class AddTeamUseCase @Inject constructor(private val repo: TeamRepository) {
    suspend operator fun invoke(team: Team) = repo.addTeam(team)
}

class GetTeamsUseCase @Inject constructor(private val repo: TeamRepository) {
    operator fun invoke(leagueId: Long): Flow<List<Team>> = repo.getTeamsByLeague(leagueId)
}

class AddBowlerUseCase @Inject constructor(private val repo: BowlerRepository) {
    suspend operator fun invoke(bowler: Bowler) = repo.addBowler(bowler)
}

class GetBowlersUseCase @Inject constructor(private val repo: BowlerRepository) {
    operator fun invoke(teamId: Long): Flow<List<Bowler>> = repo.getBowlersByTeam(teamId)
}

class AddSeriesUseCase @Inject constructor(private val repo: SeriesRepository) {
    suspend operator fun invoke(series: Series) = repo.addSeries(series)
}

class GetSeriesUseCase @Inject constructor(private val repo: SeriesRepository) {
    operator fun invoke(bowlerId: Long): Flow<List<Series>> = repo.getSeriesByBowler(bowlerId)
}

class AddGameScoreUseCase @Inject constructor(private val repo: GameScoreRepository) {
    suspend operator fun invoke(score: GameScore) = repo.addGameScore(score)
}

class GetScoresUseCase @Inject constructor(private val repo: GameScoreRepository) {
    operator fun invoke(seriesId: Long): Flow<List<GameScore>> = repo.getScoresBySeries(seriesId)
}

class GetBowlerStatsUseCase @Inject constructor(private val repo: StatsRepository) {
    operator fun invoke(bowlerId: Long): Flow<BowlerStats> = repo.getBowlerStats(bowlerId)
}

class GetTeamStatsUseCase @Inject constructor(private val repo: StatsRepository) {
    operator fun invoke(teamId: Long): Flow<BowlerStats> = repo.getTeamStats(teamId)
}
