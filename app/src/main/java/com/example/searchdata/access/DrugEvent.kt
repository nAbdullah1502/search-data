package com.example.searchdata.access


sealed interface DrugEvent {
    data object SaveDrug: DrugEvent
    data class SetDrugName (val setEventDrugName: String): DrugEvent
    data class SetFarmGroup (val setEventFarmGroup: String): DrugEvent
    data class SetFarmEffect (val setEventFarmEffect: String): DrugEvent
    data class SortDrugs(val setEventSortType: SortType): DrugEvent

    data class SetSearchQuery(val query: String) : DrugEvent
}