package com.example.searchdata.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Drug::class], version=1, exportSchema = false)
abstract class DrugDatabase : RoomDatabase() {
    abstract fun drugDao(): DrugDao
}