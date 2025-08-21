package com.studio32b.ballbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arsenal_balls")
data class ArsenalBall(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val ballId: Int,
    @ColumnInfo(name = "date_added") val dateAdded: Long,
    @ColumnInfo(name = "games_used") val gamesUsed: Int = 0,
    @ColumnInfo(name = "last_resurfaced") val lastResurfaced: Long? = null,
    @ColumnInfo(name = "games_since_resurfaced") val gamesSinceResurfaced: Int = 0,
    @ColumnInfo(name = "last_oil_extraction") val lastOilExtraction: Long? = null,
    @ColumnInfo(name = "games_since_oil_extraction") val gamesSinceOilExtraction: Int = 0,
    // Per-ball maintenance limits
    @ColumnInfo(name = "resurfacing_lower_limit") val resurfacingLowerLimit: Int = 60,
    @ColumnInfo(name = "resurfacing_upper_limit") val resurfacingUpperLimit: Int = 80,
    @ColumnInfo(name = "oil_extraction_lower_limit") val oilExtractionLowerLimit: Int = 50,
    @ColumnInfo(name = "oil_extraction_upper_limit") val oilExtractionUpperLimit: Int = 75,
    @ColumnInfo(name = "last_used") val lastUsed: Long? = null
)
