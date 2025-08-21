package com.studio32b.ballbag.ui.browse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studio32b.ballbag.data.BowlingBall
import com.studio32b.ballbag.data.BowlingBallRepository
import com.studio32b.ballbag.data.BrandCount
import com.studio32b.ballbag.data.OptionCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BowlingBallSearchUiState(
    val query: String = "",
    val selectedBrands: Set<String> = emptySet(),
    val selectedCoverstocks: Set<String> = emptySet(),
    val selectedCores: Set<String> = emptySet(),
    val selectedReleaseTypes: Set<String> = emptySet(),
    val selectedProdStatus: Set<String> = emptySet(),
    val brands: List<BrandCount> = emptyList(),
    val brandCounts: List<OptionCount> = emptyList(), // <-- add this line
    val coverstocks: List<OptionCount> = emptyList(),
    val cores: List<OptionCount> = emptyList(),
    val releaseTypes: List<OptionCount> = emptyList(),
    val prodStatusCounts: List<OptionCount> = emptyList(),
    val results: List<BowlingBall> = emptyList(),
    val arsenalIds: Set<Int> = emptySet()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BowlingBallSearchViewModel @Inject constructor(
    private val repository: BowlingBallRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _selectedBrands = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCoverstocks = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCores = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedReleaseTypes = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedProdStatus = MutableStateFlow<Set<String>>(emptySet())
    private val _brands = MutableStateFlow<List<BrandCount>>(emptyList())

    val brands: StateFlow<List<BrandCount>> get() = _brands

    private val _arsenalIds = repository.getAllArsenalBallIds().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val arsenalIds: StateFlow<List<Int>> get() = _arsenalIds

    val brandCounts: StateFlow<List<OptionCount>> = combine(
        _query, _selectedCoverstocks, _selectedCores, _selectedReleaseTypes
    ) { query, coverstocks, cores, releaseTypes ->
        repository.getBrandCountsWithFilters(
            if ((query as String).isBlank()) null else query,
            coverstocks as Set<String>,
            cores as Set<String>,
            releaseTypes as Set<String>
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coverstockCounts: StateFlow<List<OptionCount>> = combine(
        _query, _selectedBrands, _selectedCores, _selectedReleaseTypes
    ) { query, brands, cores, releaseTypes ->
        repository.getCoverstockCountsWithFilters(
            if ((query as String).isBlank()) null else query,
            brands as Set<String>,
            cores as Set<String>,
            releaseTypes as Set<String>
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coreCounts: StateFlow<List<OptionCount>> = combine(
        _query, _selectedBrands, _selectedCoverstocks, _selectedReleaseTypes
    ) { query, brands, coverstocks, releaseTypes ->
        repository.getCoreCountsWithFilters(
            if ((query as String).isBlank()) null else query,
            brands as Set<String>,
            coverstocks as Set<String>,
            releaseTypes as Set<String>
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val releaseTypeCounts: StateFlow<List<OptionCount>> = combine(
        _query, _selectedBrands, _selectedCoverstocks, _selectedCores
    ) { query, brands, coverstocks, cores ->
        repository.getReleaseTypeCountsWithFilters(
            if ((query as String).isBlank()) null else query,
            brands as Set<String>,
            coverstocks as Set<String>,
            cores as Set<String>
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prodStatusCounts: StateFlow<List<OptionCount>> = combine(
        _query, _selectedBrands, _selectedCoverstocks, _selectedCores, _selectedReleaseTypes
    ) { query, brands, coverstocks, cores, releaseTypes ->
        repository.getProdStatusCountsWithFilters(
            if ((query as String).isBlank()) null else query,
            brands as Set<String>,
            coverstocks as Set<String>,
            cores as Set<String>,
            releaseTypes as Set<String>
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<BowlingBall>> = combine(
        _query,
        _selectedBrands,
        _selectedCoverstocks,
        _selectedCores,
        _selectedReleaseTypes,
        _selectedProdStatus
    ) { values ->
        val query = values[0] as String
        val brands = values[1] as Set<String>
        val coverstocks = values[2] as Set<String>
        val cores = values[3] as Set<String>
        val releaseTypes = values[4] as Set<String>
        val prodStatus = values[5] as Set<String>
        repository.searchAndFilterBallsFlow(
            if (query.isBlank()) null else query,
            brands,
            coverstocks,
            cores,
            releaseTypes,
            prodStatus
        )
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val arsenalCounts: StateFlow<Map<Int, Int>> =
        repository.getAllArsenalBallsRaw()
            .map { list ->
                list.groupingBy { it.ballId }.eachCount()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<BowlingBallSearchUiState> = combine(
        _query, _selectedBrands, _selectedCoverstocks, _selectedCores, _selectedReleaseTypes,
        _brands, brandCounts, coverstockCounts, coreCounts, releaseTypeCounts, prodStatusCounts, results, _arsenalIds, _selectedProdStatus
    ) { values ->
        val query = values[0] as? String ?: ""
        val selectedBrands = values[1] as? Set<String> ?: emptySet()
        val selectedCoverstocks = values[2] as? Set<String> ?: emptySet()
        val selectedCores = values[3] as? Set<String> ?: emptySet()
        val selectedReleaseTypes = values[4] as? Set<String> ?: emptySet()
        val brands = values[5] as? List<BrandCount> ?: emptyList()
        val brandCounts = values[6] as? List<OptionCount> ?: emptyList()
        val coverstocks = values[7] as? List<OptionCount> ?: emptyList()
        val cores = values[8] as? List<OptionCount> ?: emptyList()
        val releaseTypes = values[9] as? List<OptionCount> ?: emptyList()
        val prodStatusCounts = values[10] as? List<OptionCount> ?: emptyList()
        val results = values[11] as? List<BowlingBall> ?: emptyList()
        val arsenalIds = (values[12] as? List<Int>)?.toSet() ?: emptySet()
        val selectedProdStatus = values[13] as? Set<String> ?: emptySet()
        BowlingBallSearchUiState(
            query = query,
            selectedBrands = selectedBrands,
            selectedCoverstocks = selectedCoverstocks,
            selectedCores = selectedCores,
            selectedReleaseTypes = selectedReleaseTypes,
            brands = brands,
            brandCounts = brandCounts,
            coverstocks = coverstocks,
            cores = cores,
            releaseTypes = releaseTypes,
            prodStatusCounts = prodStatusCounts,
            selectedProdStatus = selectedProdStatus,
            results = results,
            arsenalIds = arsenalIds
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BowlingBallSearchUiState()
    )

    init {
        // Continuously collect brand counts so dropdown updates as soon as data is available
        viewModelScope.launch {
            repository.getBrandCountsSortedFlow().collectLatest {
                _brands.value = it
            }
        }
        // Debug: Log the count of bowling balls in the database
        viewModelScope.launch {
            try {
                val count = repository.count()
                Log.d("BowlingBallSearchVM", "Bowling balls in DB: $count")
            } catch (e: Exception) {
                Log.e("BowlingBallSearchVM", "Error counting balls: ", e)
            }
        }
    }

    fun onQueryChange(newQuery: String) { _query.value = newQuery }
    fun onBrandSelected(brand: String?) {
        val newSelection = brand?.let { setOf(it) } ?: emptySet()
        _selectedBrands.value = newSelection
    }
    fun onCoverstockSelected(coverstock: String?) {
        val newSelection = coverstock?.let { setOf(it) } ?: emptySet()
        _selectedCoverstocks.value = newSelection
    }
    fun onCoreSelected(core: String?) {
        val newSelection = core?.let { setOf(it) } ?: emptySet()
        _selectedCores.value = newSelection
    }
    fun onReleaseTypeSelected(releaseType: String?) {
        val newSelection = releaseType?.let { setOf(it) } ?: emptySet()
        _selectedReleaseTypes.value = newSelection
    }
    fun clearFilters() {
        _query.value = ""
        _selectedBrands.value = emptySet()
        _selectedCoverstocks.value = emptySet()
        _selectedCores.value = emptySet()
        _selectedReleaseTypes.value = emptySet()
        _selectedProdStatus.value = emptySet()
    }

    /**
     * Adds a ball to the arsenal with a custom date.
     */
    fun addToArsenal(ballId: Int, dateAdded: Long) {
        viewModelScope.launch {
            repository.addToArsenal(ballId, dateAdded)
        }
    }

    /**
     * Adds a ball to the arsenal with a custom date and games used.
     */
    fun addToArsenal(ballId: Int, dateAdded: Long, gamesUsed: Int) {
        viewModelScope.launch {
            repository.addToArsenal(ballId, dateAdded, gamesUsed)
        }
    }

    @Deprecated("Use addToArsenal(ballId, dateAdded) instead.")
    fun addToArsenal(ballId: Int) {
        viewModelScope.launch {
            repository.addToArsenal(ballId)
        }
    }

    fun toggleBrand(brand: String) {
        _selectedBrands.value = _selectedBrands.value.toMutableSet().apply {
            if (contains(brand)) remove(brand) else add(brand)
        }
    }
    fun toggleCoverstock(coverstock: String) {
        _selectedCoverstocks.value = _selectedCoverstocks.value.toMutableSet().apply {
            if (contains(coverstock)) remove(coverstock) else add(coverstock)
        }
    }
    fun toggleCore(core: String) {
        _selectedCores.value = _selectedCores.value.toMutableSet().apply {
            if (contains(core)) remove(core) else add(core)
        }
    }
    fun toggleReleaseType(releaseType: String) {
        _selectedReleaseTypes.value = _selectedReleaseTypes.value.toMutableSet().apply {
            if (contains(releaseType)) remove(releaseType) else add(releaseType)
        }
    }
    fun toggleProdStatus(status: String) {
        _selectedProdStatus.value = _selectedProdStatus.value.toMutableSet().apply {
            if (contains(status)) remove(status) else add(status)
        }
    }
}
