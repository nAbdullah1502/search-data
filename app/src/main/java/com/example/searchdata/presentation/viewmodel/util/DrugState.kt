package com.example.searchdata.presentation.viewmodel.util

import com.example.searchdata.util.SortType
import com.example.searchdata.data.Drug

data class DrugState(
    val allDrugs: List<Drug> = emptyList(),
    val drugStateName : String = "",
    val stateFarmGroup : String = "",
    val stateFarmEffect : String = "",
    val sortType: SortType = SortType.NAME
)
