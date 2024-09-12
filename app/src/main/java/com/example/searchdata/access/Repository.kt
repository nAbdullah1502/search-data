package com.example.searchdata.access

import com.example.searchdata.data.Drug
import com.example.searchdata.data.DrugDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class Repository @Inject constructor(private val drugDao: DrugDao) {

    fun getDrugCount(): Flow<Int> {
        return drugDao.getDrugCount()
    }

    fun getDrugsOrderedByName(): Flow<List<Drug>> {
        return drugDao.getDrugsOrderedByName()
    }
    suspend fun upsertDrug(drug: Drug) { drugDao.upsertDrug(drug) }

    fun searchDrugs(searchQuery: String): Flow<List<Drug>> {
        return drugDao.searchDrugs(searchQuery)
    }
    fun getDrugsOrderedByFarmGroup(): Flow<List<Drug>>{
        return drugDao.getDrugsOrderedByFarmGroup()
    }
}