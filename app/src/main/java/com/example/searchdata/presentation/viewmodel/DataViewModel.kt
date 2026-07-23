package com.example.searchdata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchdata.presentation.viewmodel.util.DrugState
import com.example.searchdata.data.Repository
import com.example.searchdata.data.Drug
import com.example.searchdata.presentation.viewmodel.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DataViewModel @Inject constructor(
    private val repo : Repository
) : ViewModel() {
    /*
  _____               _______
 |  __ \      /\     |__   __|     /\
 | |  | |    /  \       | |       /  \
 | |  | |   / /\ \      | |      / /\ \
 | |__| |  / ____ \     | |     / ____ \
 |_____/  /_/    \_\    |_|    /_/    \_\

    */
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
    fun autoUpsertDrugs() {
        viewModelScope.launch {
            repo.getDrugCount().collect { count ->
                if (count == 0) {  // Only upsert if the database is empty
                    predefinedList.forEach { repo.upsertDrug(it) }
                }
            }
        }
    }
    /*
   _____  ____  _____ _______
  / ____|/ __ \|  __ \__   __|
 | (___ | |  | | |__) | | |
  \___ \| |  | |  _  /  | |
  ____) | |__| | | \ \  | |
 |_____/ \____/|_|  \_\ |_|

    */
}