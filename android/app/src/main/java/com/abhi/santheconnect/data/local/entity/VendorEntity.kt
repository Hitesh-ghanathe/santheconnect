package com.abhi.santheconnect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photoUrls: List<String>,
    val specialtyTags: List<String>,
    val averageRating: Float,
    val reviewCount: Int,
    val submittedBy: String
)
