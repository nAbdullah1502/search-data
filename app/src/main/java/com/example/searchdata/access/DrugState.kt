package com.example.searchdata.access

import com.example.searchdata.data.Drug

data class DrugState(
    val allDrugs: List<Drug> = emptyList(),
    val drugStateName : String = "",
    val stateFarmGroup : String = "",
    val stateFarmEffect : String = "",
    val sortType: SortType = SortType.NAME
)
