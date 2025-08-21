package com.studio32b.ballbag.data

import android.content.Context
import com.studio32b.ballbag.data.dao.ArsenalBallDao
import com.studio32b.ballbag.data.entity.ArsenalBall
import com.studio32b.ballbag.data.entity.ArsenalBallUsageHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BowlingBallRepository(private val db: BowlingBallDatabase, private val context: Context) {
    private val dao = db.bowlingBallDao()
    private val arsenalDao = db.arsenalBallDao()

    suspend fun seedIfNeeded() {
        if (dao.count() == 0) {
            val balls = loadBowlingBallsFromJson(context)
            dao.insertAll(balls)
        }
    }

    suspend fun getAllBalls(): List<BowlingBall> = withContext(Dispatchers.IO) {
        seedIfNeeded()
        dao.getAll()
    }

    suspend fun searchBalls(query: String): List<BowlingBall> = withContext(Dispatchers.IO) {
        seedIfNeeded()
        dao.searchBalls(query)
    }

    private fun loadBowlingBallsFromJson(context: Context): List<BowlingBall> {
        val jsonString = context.assets.open("balls.json").bufferedReader().use { it.readText() }
        return Json.decodeFromString(jsonString)
    }

    /**
     * Adds a ball to the arsenal with a custom date.
     */
    suspend fun addToArsenal(ballId: Int, dateAdded: Long) = withContext(Dispatchers.IO) {
        arsenalDao.addToArsenal(ArsenalBall(ballId = ballId, dateAdded = dateAdded, gamesUsed = 0))
    }

    /**
     * Adds a ball to the arsenal with a custom date and games used.
     */
    suspend fun addToArsenal(ballId: Int, dateAdded: Long, gamesUsed: Int) = withContext(Dispatchers.IO) {
        arsenalDao.addToArsenal(ArsenalBall(ballId = ballId, dateAdded = dateAdded, gamesUsed = gamesUsed))
    }

    @Deprecated("Use addToArsenal(ballId, dateAdded) instead.")
    suspend fun addToArsenal(ballId: Int) = withContext(Dispatchers.IO) {
        arsenalDao.addToArsenal(ArsenalBall(ballId = ballId, dateAdded = System.currentTimeMillis(), gamesUsed = 0))
    }

    suspend fun removeFromArsenalById(arsenalId: Long) = withContext(Dispatchers.IO) {
        arsenalDao.removeFromArsenal(ArsenalBall(id = arsenalId, ballId = 0, dateAdded = 0L, gamesUsed = 0))
    }

    suspend fun updateGamesUsed(id: Long, gamesUsed: Int) = withContext(Dispatchers.IO) {
        arsenalDao.updateGamesUsedAndLastUsed(id, gamesUsed, System.currentTimeMillis())
    }

    suspend fun deleteArsenalBallById(id: Long) = withContext(Dispatchers.IO) {
        arsenalDao.deleteById(id)
    }

    fun countInArsenal(ballId: Int): Flow<Int> = arsenalDao.countInArsenal(ballId)

    fun getAllArsenalBallIds(): Flow<List<Int>> = arsenalDao.getAllArsenalBallIds()

    fun getAllArsenalBalls(): Flow<List<BowlingBall>> =
        combine(arsenalDao.getAllArsenalBallIds(), dao.getAllFlow()) { ids, allBalls ->
            allBalls.filter { ids.contains(it.id) }
        }

    fun getAllArsenalBallsRaw(): Flow<List<ArsenalBall>> = arsenalDao.getAllArsenalBalls()

    fun searchAndFilterBallsFlow(
        query: String?,
        brands: Set<String>,
        coverstocks: Set<String>,
        cores: Set<String>,
        releaseTypes: Set<String>,
        prodStatus: Set<String>? = null
    ): Flow<List<BowlingBall>> = flow {
        seedIfNeeded()
        emitAll(
            dao.getAllFlow().map { balls ->
                balls.filter { ball ->
                    (query == null || ball.ballName.contains(query, ignoreCase = true) || ball.brand.contains(query, ignoreCase = true)) &&
                    (brands.isEmpty() || brands.contains(ball.brand)) &&
                    (coverstocks.isEmpty() || coverstocks.contains(ball.coverstock)) &&
                    (cores.isEmpty() || cores.contains(ball.core)) &&
                    (releaseTypes.isEmpty() || releaseTypes.contains(ball.releaseType)) &&
                    (prodStatus == null || prodStatus.isEmpty() ||
                        (prodStatus.contains("In Prod") && (ball.discontinued == "FALSE")) ||
                        (prodStatus.contains("Discontinued") && (ball.discontinued == "TRUE")))
                }
            }
        )
    }

    fun getProdStatusCountsWithFilters(
        query: String?,
        brands: Set<String>,
        coverstocks: Set<String>,
        cores: Set<String>,
        releaseTypes: Set<String>
    ): Flow<List<OptionCount>> = flow {
        seedIfNeeded()
        emitAll(
            dao.getAllFlow().map { balls ->
                val filtered = balls.filter { ball ->
                    (query == null || ball.ballName.contains(query, ignoreCase = true) || ball.brand.contains(query, ignoreCase = true)) &&
                    (brands.isEmpty() || brands.contains(ball.brand)) &&
                    (coverstocks.isEmpty() || coverstocks.contains(ball.coverstock)) &&
                    (cores.isEmpty() || cores.contains(ball.core)) &&
                    (releaseTypes.isEmpty() || releaseTypes.contains(ball.releaseType))
                }
                listOf(
                    OptionCount("In Prod", filtered.count { it.discontinued == "FALSE" }),
                    OptionCount("Discontinued", filtered.count { it.discontinued == "TRUE" })
                )
            }
        )
    }

    fun getBrandCountsSortedFlow(): Flow<List<BrandCount>> = dao.getBrandCountsSorted()

    suspend fun count(): Int = dao.count()

    fun getAllBallsFlow(): Flow<List<BowlingBall>> = dao.getAllFlow()

    suspend fun insertUsageHistory(history: ArsenalBallUsageHistory) = withContext(Dispatchers.IO) {
        db.arsenalBallUsageHistoryDao().insertUsage(history)
    }

    fun getUsageHistoryForBall(arsenalBallId: Long) =
        db.arsenalBallUsageHistoryDao().getUsageHistoryForBall(arsenalBallId)

    fun getAllUsageHistories(): Flow<List<ArsenalBallUsageHistory>> =
        db.arsenalBallUsageHistoryDao().getAllUsageHistories()

    suspend fun updateResurfaced(id: Long, lastResurfaced: Long, gamesSinceResurfaced: Int) = withContext(Dispatchers.IO) {
        db.arsenalBallDao().updateResurfaced(id, lastResurfaced, gamesSinceResurfaced)
    }

    suspend fun updateOilExtraction(id: Long, lastOilExtraction: Long, gamesSinceOilExtraction: Int) = withContext(Dispatchers.IO) {
        db.arsenalBallDao().updateOilExtraction(id, lastOilExtraction, gamesSinceOilExtraction)
    }

    suspend fun updateMaintenanceLimits(
        id: Long,
        resurfacingLowerLimit: Int,
        resurfacingUpperLimit: Int,
        oilExtractionLowerLimit: Int,
        oilExtractionUpperLimit: Int
    ) = withContext(Dispatchers.IO) {
        arsenalDao.updateMaintenanceLimits(
            id,
            resurfacingLowerLimit,
            resurfacingUpperLimit,
            oilExtractionLowerLimit,
            oilExtractionUpperLimit
        )
    }

    suspend fun incrementGamesAndMaintenanceCounters(id: Long, gamesToAdd: Int) = withContext(Dispatchers.IO) {
        arsenalDao.incrementGamesAndMaintenanceCounters(id, gamesToAdd)
    }

    fun getBrandCountsWithFilters(
        query: String?,
        coverstocks: Set<String>,
        cores: Set<String>,
        releaseTypes: Set<String>
    ): Flow<List<OptionCount>> =
        dao.getBrandCountsWithFilters(
            query,
            coverstocks.toList(), coverstocks.size,
            cores.toList(), cores.size,
            releaseTypes.toList(), releaseTypes.size
        )

    fun getCoverstockCountsWithFilters(
        query: String?,
        brands: Set<String>,
        cores: Set<String>,
        releaseTypes: Set<String>
    ): Flow<List<OptionCount>> =
        dao.getCoverstockCountsWithFilters(
            query,
            brands.toList(), brands.size,
            cores.toList(), cores.size,
            releaseTypes.toList(), releaseTypes.size
        )

    fun getCoreCountsWithFilters(
        query: String?,
        brands: Set<String>,
        coverstocks: Set<String>,
        releaseTypes: Set<String>
    ): Flow<List<OptionCount>> =
        dao.getCoreCountsWithFilters(
            query,
            brands.toList(), brands.size,
            coverstocks.toList(), coverstocks.size,
            releaseTypes.toList(), releaseTypes.size
        )

    fun getReleaseTypeCountsWithFilters(
        query: String?,
        brands: Set<String>,
        coverstocks: Set<String>,
        cores: Set<String>
    ): Flow<List<OptionCount>> =
        dao.getReleaseTypeCountsWithFilters(
            query,
            brands.toList(), brands.size,
            coverstocks.toList(), coverstocks.size,
            cores.toList(), cores.size
        )
}
