package com.abhi.santheconnect.data.model

data class Vendor(
    val id: String = "",
    val name: String = "",
    val category: String = "FOOD",          // VendorCategory enum name
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val specialtyTags: List<String> = emptyList(),
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val isActive: Boolean = true,
    val submittedBy: String = "Community"
)

enum class VendorCategory(val displayName: String, val emoji: String) {
    FOOD("Food & Khana-Vali", "🍛"),
    MARKET("Santhe Market", "🛒"),
    CRAFT("Craftspeople", "🧶"),
    STAY("Stay", "🏡")
}
