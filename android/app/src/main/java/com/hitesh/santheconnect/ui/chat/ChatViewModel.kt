package com.hitesh.santheconnect.ui.chat

import android.app.Application
import androidx.lifecycle.*
import com.hitesh.santheconnect.BuildConfig
import com.hitesh.santheconnect.data.model.ChatMessage
import com.hitesh.santheconnect.utils.NetworkUtils
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Namaskara! I'm your Santhe Guide. Ask me anything about local markets, vendors, or Karnataka's specialties!", false)
    ))
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val chatModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction = content {
                text("You are 'SantheGuide', an expert AI on Karnataka's local markets (Santhe). Be warm and helpful.")
            }
        ).startChat()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(text, true))
        currentMessages.add(ChatMessage("SantheGuide is thinking...", false))
        _messages.value = currentMessages

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (!NetworkUtils.isInternetAvailable(getApplication())) {
                    throw Exception("No internet connection. Please go online to chat.")
                }

                if (BuildConfig.GEMINI_API_KEY.contains("YOUR_GEMINI") || BuildConfig.GEMINI_API_KEY.length < 10) {
                    throw Exception("Gemini API Key is missing or invalid. Please check local.properties.")
                }
                
                val response = chatModel.sendMessage(text)
                val responseText = response.text ?: "I'm sorry, I couldn't understand that."
                
                val updatedMessages = _messages.value.orEmpty().toMutableList()
                if (updatedMessages.isNotEmpty() && updatedMessages.last().text == "SantheGuide is thinking...") {
                    updatedMessages.removeAt(updatedMessages.size - 1)
                }
                
                updatedMessages.add(ChatMessage(responseText, false))
                _messages.value = updatedMessages
            } catch (e: Exception) {
                val updatedMessages = _messages.value.orEmpty().toMutableList()
                if (updatedMessages.isNotEmpty() && updatedMessages.last().text == "SantheGuide is thinking...") {
                    updatedMessages.removeAt(updatedMessages.size - 1)
                }
                
                val errorMessage = when {
                    e.message?.contains("internet") == true -> "You are offline. Please check your connection."
                    e.message?.contains("404") == true -> "SantheGuide is having trouble connecting to the AI brain (Model not found). Please ensure your API key supports Gemini 1.5 Flash."
                    e.message?.contains("403") == true -> "Access denied. Your API key might be restricted or invalid."
                    e.message?.contains("429") == true -> "I'm a bit busy right now. Give me a moment to catch my breath!"
                    else -> "Namaskara! I hit a small snag. (${e.localizedMessage ?: "Connection issue"})"
                }
                updatedMessages.add(ChatMessage(errorMessage, false))
                _messages.value = updatedMessages
                android.util.Log.e("ChatBot", "Error sending message", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
