package com.studio32b.ballbag.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studio32b.ballbag.data.dao.ArsenalBallDao
import com.studio32b.ballbag.data.dao.ArsenalBallUsageHistoryDao
import com.studio32b.ballbag.data.dao.BowlerDao
import com.studio32b.ballbag.data.dao.GameScoreDao
import com.studio32b.ballbag.data.dao.LeagueDao
import com.studio32b.ballbag.data.dao.SeriesDao
import com.studio32b.ballbag.data.dao.TeamDao
import com.studio32b.ballbag.data.entity.ArsenalBall
import com.studio32b.ballbag.data.entity.ArsenalBallUsageHistory
import com.studio32b.ballbag.data.entity.BowlerEntity
import com.studio32b.ballbag.data.entity.GameScoreEntity
import com.studio32b.ballbag.data.entity.LeagueEntity
import com.studio32b.ballbag.data.entity.SeriesEntity
import com.studio32b.ballbag.data.entity.TeamEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream

@Database(
    entities = [
        BowlingBall::class,
        ArsenalBall::class,
        LeagueEntity::class,
        TeamEntity::class,
        BowlerEntity::class,
        SeriesEntity::class,
        GameScoreEntity::class,
        ArsenalBallUsageHistory::class
    ],
    version = 8, // Bump version to 8
    exportSchema = false
)
abstract class BowlingBallDatabase : RoomDatabase() {
    abstract fun bowlingBallDao(): BowlingBallDao
    abstract fun arsenalBallDao(): ArsenalBallDao
    abstract fun bowlerDao(): BowlerDao
    abstract fun leagueDao(): LeagueDao
    abstract fun teamDao(): TeamDao
    abstract fun seriesDao(): SeriesDao
    abstract fun gameScoreDao(): GameScoreDao
    abstract fun arsenalBallUsageHistoryDao(): ArsenalBallUsageHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BowlingBallDatabase? = null

        fun getInstance(context: Context): BowlingBallDatabase {
            val MIGRATION_2_3 = object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN games_used INTEGER NOT NULL DEFAULT 0")
                }
            }
            val MIGRATION_3_4 = object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS arsenal_ball_usage_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            arsenalBallId INTEGER NOT NULL,
                            date INTEGER NOT NULL,
                            games_added INTEGER NOT NULL,
                            type TEXT NOT NULL
                        )
                    """)
                }
            }
            val MIGRATION_4_5 = object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN last_resurfaced INTEGER")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN games_since_resurfaced INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN last_oil_extraction INTEGER")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN games_since_oil_extraction INTEGER NOT NULL DEFAULT 0")
                }
            }
            val MIGRATION_5_6 = object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN resurfacing_lower_limit INTEGER NOT NULL DEFAULT 60")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN resurfacing_upper_limit INTEGER NOT NULL DEFAULT 80")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN oil_extraction_lower_limit INTEGER NOT NULL DEFAULT 50")
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN oil_extraction_upper_limit INTEGER NOT NULL DEFAULT 75")
                }
            }
            val MIGRATION_6_7 = object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE bowling_balls ADD COLUMN acquiredDate TEXT")
                    database.execSQL("ALTER TABLE bowling_balls ADD COLUMN gamesPlayed INTEGER NOT NULL DEFAULT 0")
                }
            }
            val MIGRATION_7_8 = object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE arsenal_balls ADD COLUMN last_used INTEGER")
                }
            }
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BowlingBallDatabase::class.java,
                    "bowling_ball_db"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val balls = readBallsFromAssets(context)
                            getInstance(context).bowlingBallDao().insertAll(balls)
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }

        private val jsonFormat = Json { ignoreUnknownKeys = true }
        private fun readBallsFromAssets(context: Context): List<BowlingBall> {
            return try {
                val inputStream: InputStream = context.assets.open("balls.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                jsonFormat.decodeFromString<List<BowlingBall>>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
