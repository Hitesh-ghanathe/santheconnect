package com.hitesh.santheconnect.ui.map

import android.app.Application
import androidx.lifecycle.*
import com.hitesh.santheconnect.data.local.SantheDatabase
import com.hitesh.santheconnect.data.local.entity.VendorEntity
import com.hitesh.santheconnect.data.model.Vendor
import com.hitesh.santheconnect.data.model.VendorCategory
import com.hitesh.santheconnect.data.repository.SantheRepository
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SantheDatabase.getDatabase(application)
    private val repo = SantheRepository(db.vendorDao())

    // Observe Room DB Flow - UI will update automatically when database changes
    private val _vendorsFlow = repo.getLocalVendorsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    
    private val _vendors = MutableLiveData<List<Vendor>>()
    val vendors: LiveData<List<Vendor>> = _vendors

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allVendors: List<Vendor> = emptyList()
    var selectedCategory: VendorCategory? = null

    init {
        // Collect Flow from Room and update the LiveData
        viewModelScope.launch {
            _vendorsFlow.collect { list ->
                if (list.isEmpty()) {
                    // If local DB is empty, try to seed with mock data for testing
                    seedMockData()
                } else {
                    allVendors = list
                    applyFilter()
                }
            }
        }
        loadVendors()
    }

    private suspend fun seedMockData() {
        val mockEntities = getMockVendors().map { vendor ->
            VendorEntity(
                id = vendor.id,
                name = vendor.name,
                category = vendor.category,
                latitude = vendor.latitude,
                longitude = vendor.longitude,
                description = vendor.description,
                photoUrls = vendor.photoUrls,
                specialtyTags = vendor.specialtyTags,
                averageRating = vendor.averageRating,
                reviewCount = vendor.reviewCount,
                submittedBy = vendor.submittedBy
            )
        }
        db.vendorDao().insertVendors(mockEntities)
    }

    private fun getMockVendors() = listOf(
        Vendor(id="v1", name = "Gowda's Organic Honey", category = "FOOD", latitude = 12.9716, longitude = 77.5946, description = "Pure raw honey from the forests of Western Ghats.", averageRating = 4.8f, reviewCount = 124),
        Vendor(id="v2", name = "Kaveri Silk House", category = "CRAFT", latitude = 12.2958, longitude = 76.6394, description = "Authentic hand-woven Ilkal and Mysore silk sarees.", averageRating = 4.5f, reviewCount = 85),
        Vendor(id="v3", name = "Kodagu Coffee Roasters", category = "FOOD", latitude = 12.4244, longitude = 75.7382, description = "Freshly roasted Arabica and Robusta beans from Coorg.", averageRating = 4.9f, reviewCount = 210),
        Vendor(id="v4", name = "Channapatna Toy Palace", category = "CRAFT", latitude = 12.6518, longitude = 77.2088, description = "Traditional lacquer-ware toys made by local artisans.", averageRating = 4.7f, reviewCount = 56),
        Vendor(id="v5", name = "Dharwad Peda Corner", latitude = 15.4589, longitude = 75.0078, category = "FOOD", description = "The original taste of Dharwad's famous milk sweets.", averageRating = 4.8f, reviewCount = 320)
    )

    fun loadVendors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repo.refreshVendors()
            } catch (e: Exception) {
                _error.value = "Offline mode: showing saved vendors"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(category: VendorCategory?) {
        selectedCategory = category
        applyFilter()
    }

    private fun applyFilter() {
        _vendors.value = if (selectedCategory == null) allVendors
        else allVendors.filter { it.category == selectedCategory!!.name }
    }
}
