package com.example.searchdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchdata.access.DrugEvent
import com.example.searchdata.access.DrugState
import com.example.searchdata.access.SortType
import com.example.searchdata.data.Drug
import com.example.searchdata.data.DrugDao
import com.example.searchdata.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo : Repository, private val dao: DrugDao
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching = _searching.asStateFlow()
    private val predefinedList = listOf(
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
    private val _searchList = MutableStateFlow(predefinedList)
    private fun Drug.matchesQuery(query: String): Boolean {
        return drugName.contains(query, ignoreCase = true) || farmGroup.contains(query, ignoreCase = true)
    }

    @OptIn(FlowPreview::class)
    val filter = searchQuery.debounce(500L)
        .onEach { _searching.update{true} }
        .combine(_searchList) {text, drugs ->
            if(text.isBlank()){ drugs }
            else {delay(1000L); drugs.filter { it.matchesQuery(text)}}
        }
        .onEach { _searching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
        _searchList.value
    )
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun autoUpsertDrugs() {
        viewModelScope.launch {
            repo.getDrugCount().collect { count ->
                if (count == 0) {  // Only upsert if the database is empty
                    predefinedList.forEach { dao.upsertDrug(it) }
                }
            }
        }
    }
    private val _state = MutableStateFlow((DrugState())) //empty state of drugList state
    private val _sortType = MutableStateFlow(SortType.NAME)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _drugs = _sortType.flatMapLatest { sortType->
/*
*   flatMapLatest is a function that takes in flows, in this case _sortType flow,
*   and whenever the action is performed, fe clicked a button to change order,
*   function changes based on the given dao either order by name or farm group
*/
        when(sortType){
            SortType.NAME -> dao.getDrugsOrderedByName()
            SortType.GROUP -> dao.getDrugsOrderedByFarmGroup()
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    val state = combine(_state, _sortType, _drugs){state, sT, d ->
        state.copy(allDrugs = d, sortType = sT)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = DrugState())

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
                val finalDrug = Drug(drugName=drugName, farmGroup=farmGroup, farmEffect=farmEffect)
                viewModelScope.launch { dao.upsertDrug(finalDrug)}
                _state.update { it.copy(drugStateName="",stateFarmGroup="",stateFarmEffect="")}
            }
            is DrugEvent.SetSearchQuery -> {
                _searchQuery.value = event.query
                filterDrugsForSearch(event.query)
            }
        }
    }

    private fun filterDrugsForSearch(query: String) {
        viewModelScope.launch {
            val filteredDrugs = if (query.isEmpty()) {
                dao.getDrugsOrderedByName().first() // or your preferred default sorting
            } else {
                dao.searchDrugs(query).first()
            }
            _state.value = _state.value.copy(allDrugs = filteredDrugs)
        }
    }

    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar = _showSearchBar.asStateFlow()

    fun toggleSearchBar() {
        _showSearchBar.value = !_showSearchBar.value
    }
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

    private val _filteredDrugs = MutableStateFlow<List<Drug>>(emptyList())
    val filteredDrugs: StateFlow<List<Drug>> = _filteredDrugs.asStateFlow()

    fun filterDrugsForSearch(query: String, searchWords: List<String>) {
        viewModelScope.launch {
            val filteredDrugsList = if (query.isEmpty() && searchWords.isEmpty()) {
                dao.getDrugsOrderedByName().first() // or your preferred default sorting
            } else {
                val allDrugs = dao.getDrugsOrderedByName().first()
                allDrugs.filter { drug ->
                    searchWords.any { word ->
                        drug.drugName.contains(word, ignoreCase = true) || drug.farmGroup.contains(word, ignoreCase = true)
                    }
                }
            }
            _filteredDrugs.value = filteredDrugsList
        }
    }

    private val _words = MutableStateFlow<List<String>>(emptyList())
    private val _searchWord = MutableStateFlow("")


    fun separateWordsAndFilter(text: String) {
        // Split the text into words and filter out any empty results
        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Update the state with separated words
        _words.value = words

        // Filter the drugs based on the separated words
        val filteredWords = words.filter { word ->
            // Assuming you have a list of drugs available to filter
            val matchedDrugs = _searchList.value.filter { drug ->
                drug.drugName.contains(word, ignoreCase = true) || drug.farmGroup.contains(word, ignoreCase = true)
            }
            matchedDrugs.isNotEmpty()
        }

        // Update the searchWord StateFlow with the filtered words
        _searchWord.value = filteredWords.joinToString(", ")
    }

    val searchWord = _searchWord.asStateFlow()
}