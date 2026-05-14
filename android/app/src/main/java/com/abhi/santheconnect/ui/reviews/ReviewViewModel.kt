package com.abhi.santheconnect.ui.reviews

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.santheconnect.ai.GeminiHelper
import com.abhi.santheconnect.data.model.Review
import com.abhi.santheconnect.data.repository.SantheRepository
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val repo = SantheRepository()

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _transcription = MutableLiveData<String>()
    val transcription: LiveData<String> = _transcription

    fun loadAllReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            try { _reviews.value = repo.getAllReviews() }
            catch (e: Exception) { _reviews.value = emptyList() }
            finally { _isLoading.value = false }
        }
    }

    fun loadReviewsForVendor(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _reviews.value = repo.getReviews(vendorId) }
            catch (e: Exception) { _reviews.value = emptyList() }
            finally { _isLoading.value = false }
        }
    }

    fun submitTextReview(vendorId: String, author: String, text: String, rating: Float) {
        viewModelScope.launch {
            val review = Review(vendorId = vendorId, authorName = author, text = text, rating = rating)
            repo.addReview(review)
            loadAllReviews()
        }
    }

    fun submitPhotoReview(vendorId: String, author: String, caption: String, photoUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val photoUrl = repo.uploadMedia(photoUri, "reviews/${System.currentTimeMillis()}.jpg")
                val review = Review(vendorId = vendorId, authorName = author, text = caption, photoUrl = photoUrl)
                repo.addReview(review)
                loadAllReviews()
            } finally { _isLoading.value = false }
        }
    }

    fun transcribeAndSubmitVoiceReview(vendorId: String, author: String, audioBytes: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fix: Standardized extension and MIME type for AAC audio
                val audioUrl = repo.uploadAudio(audioBytes, "voice_reviews/${System.currentTimeMillis()}.m4a")
                
                // Fix: Passing correct MIME type to Gemini for processing
                val transcribed = GeminiHelper.transcribeAudio(audioBytes, mimeType = "audio/aac")
                
                _transcription.value = transcribed
                val review = Review(
                    vendorId = vendorId,
                    authorName = author,
                    transcribedText = transcribed,
                    voiceNoteUrl = audioUrl
                )
                repo.addReview(review)
                loadAllReviews()
            } catch (e: Exception) {
                // Log or handle error
            } finally { _isLoading.value = false }
        }
    }
}
