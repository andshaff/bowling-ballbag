package com.studio32b.ballbag.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studio32b.ballbag.data.dao.*
import com.studio32b.ballbag.data.entity.*

@Database(
    entities = [
        LeagueEntity::class,
        TeamEntity::class,
        BowlerEntity::class,
        SeriesEntity::class,
        GameScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BowlingDatabase : RoomDatabase() {
    abstract fun leagueDao(): LeagueDao
    abstract fun teamDao(): TeamDao
    abstract fun bowlerDao(): BowlerDao
    abstract fun seriesDao(): SeriesDao
    abstract fun gameScoreDao(): GameScoreDao
}
