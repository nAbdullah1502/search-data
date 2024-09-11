package com.example.searchdata.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugDao {
    @Upsert
    suspend fun upsertDrug(drug: Drug)

    @Query("SELECT * FROM drug ORDER BY drugName ASC")
    fun getDrugsOrderedByName(): Flow<List<Drug>>

    @Query("SELECT * FROM drug ORDER BY farmGroup ASC")
    fun getDrugsOrderedByFarmGroup(): Flow<List<Drug>>

    @Query("SELECT DISTINCT farmGroup FROM drug")
    fun getFarmGroup() : Flow<List<String>>

    @Query("SELECT * FROM drug WHERE drugName LIKE :query OR farmGroup LIKE :query")
    fun searchDrugs(query: String): Flow<List<Drug>>

    @Query("SELECT COUNT(*) FROM Drug")
    fun getDrugCount(): Flow<Int>
}