package com.studio32b.ballbag.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LeagueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val name: String
)

@Entity
data class BowlerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val name: String,
    val handicap: Int
)

@Entity
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bowlerId: Long,
    val date: Long
)

@Entity
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val gameNumber: Int,
    val score: Int
)

// Not stored, but for stat queries
class BowlerStatsEntity(
    val bowlerId: Long,
    val average: Float,
    val highGame: Int,
    val highSeries: Int,
    val totalPins: Int,
    val gamesPlayed: Int
)
