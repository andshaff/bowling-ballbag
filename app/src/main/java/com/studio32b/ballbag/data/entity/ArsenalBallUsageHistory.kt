package com.studio32b.ballbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arsenal_ball_usage_history")
data class ArsenalBallUsageHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val arsenalBallId: Long,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "games_added") val gamesAdded: Int,
    @ColumnInfo(name = "type") val type: String // "practice", "league", "tournament"
)
