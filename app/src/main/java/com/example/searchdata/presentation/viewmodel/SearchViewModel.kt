package com.example.searchdata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchdata.presentation.viewmodel.util.DrugEvent
import com.example.searchdata.data.Repository
import com.example.searchdata.data.Drug
import com.example.searchdata.presentation.viewmodel.util.DrugState
import com.example.searchdata.presentation.viewmodel.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo : Repository
) : ViewModel() {

    private val predefinedList : List<Drug> = listOf(
        Drug(drugName = "Furosemide",
            farmGroup = "Diuretic, sulphamoyl derivative",
            farmEffect = "High efficacy diuretic inhibitors of Na+-K+-2Cl--co-transport"),
        Drug(drugName = "Gidrohlortiazid",
            farmGroup = "Diuretic, benzothiazides",
            farmEffect = "Medium efficacy diuretic, inhibitors of Na+-Cl-symporter"),
        Drug(drugName = "Vasopressin (ADH)",
            farmGroup = "antidiuretic",
            farmEffect = "antidiuretic"),
        Drug(drugName ="Famotidine",
            farmGroup = "H2 blocker",
            farmEffect = "anti-GERD"),
    )
    val farmGroups: List<String> = listOf(
        "Antibiotics",
        "Analgesics",
        "Antihistamines",
        "Antidepressants",
        "Antivirals",
        "Diuretics",
        "Antifungals",
        "Antacids",
        "Hormones",
        "Beta-Blockers",
        "Statins",
        "NSAIDs (Non-Steroidal Anti-Inflammatory Drugs)",
        "Vitamins",
        "Sedatives",
        "Antipsychotics"
    )

    private val _sortType = MutableStateFlow(SortType.NAME)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _drugs = _sortType.flatMapLatest { sortType->
        when(sortType){
            SortType.NAME -> repo.getDrugsOrderedByName()
            SortType.GROUP -> repo.getDrugsOrderedByFarmGroup()
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    private val _state = MutableStateFlow((DrugState())) //empty state of drugList state
    val state = combine(_state, _sortType, _drugs){state, sT, d ->
        state.copy(allDrugs = d, sortType = sT)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DrugState()
    )

    private val _searchQuery = MutableStateFlow("")
    private val _searching = MutableStateFlow(false)
    private val _showSearchBar = MutableStateFlow(false)

    val searchQuery = _searchQuery.asStateFlow()
    val searching = _searching.asStateFlow()
    val showSearchBar = _showSearchBar.asStateFlow()

    @OptIn(FlowPreview::class)
    val filterDrugs = searchQuery.debounce(500L.milliseconds)
        .onEach { _searching.update {true} }
        .combine(_drugs) { text, drugs ->
            if(text.isBlank()){ drugs }
            else {
                delay(1000L.milliseconds); drugs.filter { it.matchesQuery(text)}}
        }
        .onEach { _searching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun autoUpsertDrugs() {
        viewModelScope.launch {
            repo.getDrugCount().collect { count ->
                if (count == 0) {  // Only upsert if the database is empty
                    predefinedList.forEach { repo.upsertDrug(it) }
                }
            }
        }
    }
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    private fun Drug.matchesQuery(query: String): Boolean {
        return drugName.contains(query, ignoreCase = true) || farmGroup.contains(query, ignoreCase = true)
    }
    fun toggleSearchBar() { _showSearchBar.value = !_showSearchBar.value }

    fun separateAndFilter(textScanned: String) {
        val separatedWords = textScanned.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val matchedDrugs = _drugs.value.filter { drug ->
            separatedWords.any { word ->
                drug.drugName.contains(word, ignoreCase = true) || drug.farmGroup.contains(word, ignoreCase = true)
            }
        }
        val filteredResults = matchedDrugs.joinToString(", ") { drug ->
            "${drug.drugName} (${drug.farmGroup})"
        }
        _searchQuery.value = filteredResults
    }

    fun onEvent (event: DrugEvent){
        when(event){
            is DrugEvent.SetDrugName -> {_state.update{it.copy(drugStateName = event.setEventDrugName)}}
            is DrugEvent.SetFarmGroup -> {_state.update{it.copy(stateFarmGroup = event.setEventFarmGroup)}}
            is DrugEvent.SetFarmEffect -> {_state.update{it.copy(stateFarmEffect = event.setEventFarmEffect)}}
            is DrugEvent.SortDrugs -> {_sortType.value = event.setEventSortType}
            DrugEvent.SaveDrug -> {
                val drugName = state.value.drugStateName
                val farmGroup = state.value.stateFarmGroup
                val farmEffect = state.value.stateFarmEffect
                if(drugName.isBlank() || farmGroup.isBlank()|| farmEffect.isBlank()){return}
                val finalDrug =
                    Drug(drugName = drugName, farmGroup = farmGroup, farmEffect = farmEffect)
                viewModelScope.launch { repo.upsertDrug(finalDrug)}
                _state.update { it.copy(drugStateName="",stateFarmGroup="",stateFarmEffect="")}
            }
            is DrugEvent.SetSearchQuery -> {
                _searchQuery.value = event.query
//                filterDrugsForSearch(event.query)
            }
        }
    }

}