package com.abhi.santheconnect.data.model

data class Review(
    val id: String = "",
    val vendorId: String = "",
    val authorName: String = "Traveler",
    val text: String = "",
    val transcribedText: String = "",       // Gemini AI transcription of voice note
    val photoUrl: String = "",
    val voiceNoteUrl: String = "",
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
