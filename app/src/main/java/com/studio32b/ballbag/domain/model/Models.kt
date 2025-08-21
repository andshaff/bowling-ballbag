package com.studio32b.ballbag.domain.model

data class League(
    val id: Long = 0,
    val name: String
)

data class Team(
    val id: Long = 0,
    val leagueId: Long,
    val name: String
)

data class Bowler(
    val id: Long = 0,
    val teamId: Long,
    val name: String,
    val handicap: Int
)

data class Series(
    val id: Long = 0,
    val bowlerId: Long,
    val date: Long
)

data class GameScore(
    val id: Long = 0,
    val seriesId: Long,
    val gameNumber: Int,
    val score: Int
)

data class BowlerStats(
    val bowlerId: Long,
    val average: Float,
    val highGame: Int,
    val highSeries: Int,
    val totalPins: Int,
    val gamesPlayed: Int
)
