package com.studio32b.ballbag.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BowlingBallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(balls: List<BowlingBall>)

    @Query("SELECT * FROM bowling_balls")
    suspend fun getAll(): List<BowlingBall>

    @Query("SELECT * FROM bowling_balls WHERE ballName LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ")
    suspend fun searchBalls(query: String): List<BowlingBall>

    @Query("""
        SELECT * FROM bowling_balls
        WHERE (:query IS NULL OR LOWER(ballName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brand) LIKE '%' || LOWER(:query) || '%')
        AND (:brandsSize = 0 OR brand IN (:brands))
        AND (:coverstocksSize = 0 OR coverstock IN (:coverstocks))
        AND (:coresSize = 0 OR core IN (:cores))
        AND (:releaseTypesSize = 0 OR releaseType IN (:releaseTypes))
        ORDER BY 
            CAST(SUBSTR(releaseDate, 4, 4) AS INTEGER) DESC,
            CAST(SUBSTR(releaseDate, 1, 2) AS INTEGER) DESC
    """)
    fun searchAndFilterBalls(
        query: String?,
        brands: List<String>,
        brandsSize: Int,
        coverstocks: List<String>,
        coverstocksSize: Int,
        cores: List<String>,
        coresSize: Int,
        releaseTypes: List<String>,
        releaseTypesSize: Int
    ): Flow<List<BowlingBall>>

    @Query("SELECT DISTINCT brand FROM bowling_balls ORDER BY brand")
    fun getAllBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT coverstock FROM bowling_balls ORDER BY coverstock")
    fun getAllCoverstocks(): Flow<List<String>>

    @Query("SELECT DISTINCT core FROM bowling_balls ORDER BY core")
    fun getAllCores(): Flow<List<String>>

    @Query("SELECT DISTINCT releaseType FROM bowling_balls ORDER BY releaseType")
    fun getAllReleaseTypes(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM bowling_balls")
    suspend fun count(): Int

    @Query("SELECT brand FROM bowling_balls GROUP BY brand ORDER BY COUNT(*) DESC")
    fun getBrandsSortedByCount(): Flow<List<String>>

    @Query("SELECT brand, COUNT(*) as count FROM bowling_balls GROUP BY brand ORDER BY count DESC")
    fun getBrandCountsSorted(): Flow<List<BrandCount>>

    @Query("SELECT * FROM bowling_balls")
    fun getAllFlow(): Flow<List<BowlingBall>>

    @Query("""
        SELECT brand AS value, COUNT(*) as count FROM bowling_balls
        WHERE (:coverstocksSize = 0 OR coverstock IN (:coverstocks))
          AND (:coresSize = 0 OR core IN (:cores))
          AND (:releaseTypesSize = 0 OR releaseType IN (:releaseTypes))
          AND (:query IS NULL OR LOWER(ballName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brand) LIKE '%' || LOWER(:query) || '%')
        GROUP BY brand
        ORDER BY count DESC
    """)
    fun getBrandCountsWithFilters(
        query: String?,
        coverstocks: List<String>, coverstocksSize: Int,
        cores: List<String>, coresSize: Int,
        releaseTypes: List<String>, releaseTypesSize: Int
    ): Flow<List<OptionCount>>

    @Query("""
        SELECT coverstock AS value, COUNT(*) as count FROM bowling_balls
        WHERE (:brandsSize = 0 OR brand IN (:brands))
          AND (:coresSize = 0 OR core IN (:cores))
          AND (:releaseTypesSize = 0 OR releaseType IN (:releaseTypes))
          AND (:query IS NULL OR LOWER(ballName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brand) LIKE '%' || LOWER(:query) || '%')
        GROUP BY coverstock
        ORDER BY count DESC
    """)
    fun getCoverstockCountsWithFilters(
        query: String?,
        brands: List<String>, brandsSize: Int,
        cores: List<String>, coresSize: Int,
        releaseTypes: List<String>, releaseTypesSize: Int
    ): Flow<List<OptionCount>>

    @Query("""
        SELECT core AS value, COUNT(*) as count FROM bowling_balls
        WHERE (:brandsSize = 0 OR brand IN (:brands))
          AND (:coverstocksSize = 0 OR coverstock IN (:coverstocks))
          AND (:releaseTypesSize = 0 OR releaseType IN (:releaseTypes))
          AND (:query IS NULL OR LOWER(ballName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brand) LIKE '%' || LOWER(:query) || '%')
        GROUP BY core
        ORDER BY count DESC
    """)
    fun getCoreCountsWithFilters(
        query: String?,
        brands: List<String>, brandsSize: Int,
        coverstocks: List<String>, coverstocksSize: Int,
        releaseTypes: List<String>, releaseTypesSize: Int
    ): Flow<List<OptionCount>>

    @Query("""
        SELECT releaseType AS value, COUNT(*) as count FROM bowling_balls
        WHERE (:brandsSize = 0 OR brand IN (:brands))
          AND (:coverstocksSize = 0 OR coverstock IN (:coverstocks))
          AND (:coresSize = 0 OR core IN (:cores))
          AND (:query IS NULL OR LOWER(ballName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brand) LIKE '%' || LOWER(:query) || '%')
        GROUP BY releaseType
        ORDER BY count DESC
    """)
    fun getReleaseTypeCountsWithFilters(
        query: String?,
        brands: List<String>, brandsSize: Int,
        coverstocks: List<String>, coverstocksSize: Int,
        cores: List<String>, coresSize: Int
    ): Flow<List<OptionCount>>
}

data class BrandCount(
    val brand: String,
    val count: Int
)

data class OptionCount(val value: String, val count: Int)
