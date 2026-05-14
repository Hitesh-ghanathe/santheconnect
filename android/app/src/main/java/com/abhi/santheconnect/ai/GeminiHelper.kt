package com.abhi.santheconnect.ai

import com.abhi.santheconnect.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

object GeminiHelper {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            },
            systemInstruction = content {
                text("You are 'SantheGuide', an expert AI for Karnataka markets.")
            }
        )
    }

    /**
     * Generate specialty tags for a vendor based on its description and category.
     */
    suspend fun generateSpecialtyTags(
        vendorName: String,
        category: String,
        description: String
    ): List<String> {
        val prompt = "Generate 5 short specialty tags for: $vendorName ($category). Desc: $description. Format: CSV."
        return try {
            val response = model.generateContent(content { text(prompt) })
            response.text?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Transcribe a voice note from raw audio bytes.
     */
    suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String = "audio/wav"): String {
        return try {
            val response = model.generateContent(
                content {
                    blob(mimeType, audioBytes)
                    text("Transcribe this audio exactly. Return only the text.")
                }
            )
            response.text?.trim() ?: "[Transcription unavailable]"
        } catch (e: Exception) {
            "[Transcription failed: ${e.message}]"
        }
    }

    /**
     * Generate a personalized travel suggestion.
     */
    suspend fun getTravelSuggestion(visitedCategories: List<String>): String {
        val categoriesText = visitedCategories.joinToString(", ")
        val prompt = "Suggest one unique experience in Karnataka for someone who likes: $categoriesText. 1-2 sentences."
        return try {
            model.generateContent(content { text(prompt) }).text?.trim()
                ?: "Visit a local Santhe for an authentic experience!"
        } catch (e: Exception) {
            "Explore Karnataka's local markets today!"
        }
    }

    /**
     * Get a daily interesting fact about Karnataka.
     */
    suspend fun getDailyKarnatakaFact(): String {
        val prompt = "One short interesting fact about Karnataka's heritage or markets. Max 15 words."
        return try {
            model.generateContent(content { text(prompt) }).text?.trim()
                ?: "Karnataka has the highest number of GI tagged products in India."
        } catch (e: Exception) {
            "Karnataka is known for its rich cultural diversity."
        }
    }
}
