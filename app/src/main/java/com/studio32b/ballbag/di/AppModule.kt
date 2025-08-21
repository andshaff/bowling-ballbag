package com.studio32b.ballbag.di

import android.content.Context
import androidx.room.Room
import com.studio32b.ballbag.data.BowlingBallDatabase
import com.studio32b.ballbag.data.BowlingBallRepository
import com.studio32b.ballbag.domain.repository.BowlerRepository
import com.studio32b.ballbag.data.repository.BowlerRepositoryImpl
import com.studio32b.ballbag.domain.repository.LeagueRepository
import com.studio32b.ballbag.data.repository.LeagueRepositoryImpl
import com.studio32b.ballbag.domain.repository.TeamRepository
import com.studio32b.ballbag.data.repository.TeamRepositoryImpl
import com.studio32b.ballbag.domain.repository.SeriesRepository
import com.studio32b.ballbag.data.repository.SeriesRepositoryImpl
import com.studio32b.ballbag.domain.repository.GameScoreRepository
import com.studio32b.ballbag.data.repository.GameScoreRepositoryImpl
import com.studio32b.ballbag.domain.repository.StatsRepository
import com.studio32b.ballbag.data.repository.StatsRepositoryImpl
import com.studio32b.ballbag.data.dao.BowlerDao
import com.studio32b.ballbag.data.dao.LeagueDao
import com.studio32b.ballbag.data.dao.TeamDao
import com.studio32b.ballbag.data.dao.SeriesDao
import com.studio32b.ballbag.data.dao.GameScoreDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBowlingBallDatabase(@ApplicationContext context: Context): BowlingBallDatabase =
        Room.databaseBuilder(
            context,
            BowlingBallDatabase::class.java,
            "bowling_ball_database"
        ).build()

    @Provides
    @Singleton
    fun provideBowlingBallRepository(
        db: BowlingBallDatabase,
        @ApplicationContext context: Context
    ): BowlingBallRepository = BowlingBallRepository(db, context)

    @Provides
    fun provideBowlerDao(db: BowlingBallDatabase): BowlerDao = db.bowlerDao()

    @Provides
    fun provideLeagueDao(db: BowlingBallDatabase): LeagueDao = db.leagueDao()

    @Provides
    fun provideTeamDao(db: BowlingBallDatabase): TeamDao = db.teamDao()

    @Provides
    fun provideSeriesDao(db: BowlingBallDatabase): SeriesDao = db.seriesDao()

    @Provides
    fun provideGameScoreDao(db: BowlingBallDatabase): GameScoreDao = db.gameScoreDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {
    @Binds
    @Singleton
    abstract fun bindBowlerRepository(impl: BowlerRepositoryImpl): BowlerRepository

    @Binds
    @Singleton
    abstract fun bindLeagueRepository(impl: LeagueRepositoryImpl): LeagueRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository

    @Binds
    @Singleton
    abstract fun bindSeriesRepository(impl: SeriesRepositoryImpl): SeriesRepository

    @Binds
    @Singleton
    abstract fun bindGameScoreRepository(impl: GameScoreRepositoryImpl): GameScoreRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
