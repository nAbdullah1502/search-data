package com.example.searchdata.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Drug (
    val drugName: String,
    val farmGroup: String,
    val farmEffect: String,
    @PrimaryKey(autoGenerate=true)
    val id: Int = 0
)