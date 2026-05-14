package com.abhi.santheconnect.data.model

data class Santhe(
    val id: String = "",
    val villageName: String = "",
    val dayOfWeek: String = "",             // "MONDAY", "TUESDAY", etc.
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val specialtyGoods: List<String> = emptyList(),
    val description: String = "",
    val isActive: Boolean = true,
    val organizer: String = ""
)
