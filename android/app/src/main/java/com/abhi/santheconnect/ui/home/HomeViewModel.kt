package com.abhi.santheconnect.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.abhi.santheconnect.ai.GeminiHelper
import com.abhi.santheconnect.data.local.SantheDatabase
import com.abhi.santheconnect.data.model.Santhe
import com.abhi.santheconnect.data.model.Vendor
import com.abhi.santheconnect.data.model.VendorCategory
import com.abhi.santheconnect.data.repository.SantheRepository
import com.abhi.santheconnect.utils.getCurrentDayName
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SantheDatabase.getDatabase(application)
    private val repo = SantheRepository(db.vendorDao())

    private val _todayMarkets = MutableLiveData<List<Santhe>>()
    val todayMarkets: LiveData<List<Santhe>> = _todayMarkets

    // Source of Truth from Room
    private val localVendors: LiveData<List<Vendor>> = repo.getLocalVendorsFlow()?.asLiveData() 
        ?: MutableLiveData(emptyList())

    private val _featuredVendors = MediatorLiveData<List<Vendor>>()
    val featuredVendors: LiveData<List<Vendor>> = _featuredVendors

    private val _aiSuggestion = MutableLiveData<String>()
    val aiSuggestion: LiveData<String> = _aiSuggestion

    private val _dailyFact = MutableLiveData<String>()
    val dailyFact: LiveData<String> = _dailyFact

    private var query: String = ""
    private var selectedCategory: VendorCategory? = null

    init {
        _featuredVendors.addSource(localVendors) { applyFilters() }
        loadData()
    }

    private fun applyFilters() {
        val list = localVendors.value ?: emptyList()
        _featuredVendors.value = list.filter { vendor ->
            val matchesQuery = query.isBlank() || 
                vendor.name.contains(query, ignoreCase = true) || 
                vendor.description.contains(query, ignoreCase = true)
            val matchesCategory = selectedCategory == null || vendor.category == selectedCategory?.name
            matchesQuery && matchesCategory
        }.take(15)
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                repo.refreshVendors()
                
                val markets = repo.getSantheByDay(getCurrentDayName())
                _todayMarkets.value = if (markets.isEmpty()) getMockMarkets() else markets
                
                val categories = _todayMarkets.value?.flatMap { it.specialtyGoods }?.distinct() ?: emptyList()
                _aiSuggestion.value = GeminiHelper.getTravelSuggestion(categories)
                _dailyFact.value = GeminiHelper.getDailyKarnatakaFact()
            } catch (e: Exception) {
                _aiSuggestion.value = "Explore a local Santhe market for an unforgettable Karnataka experience!"
                _dailyFact.value = "Karnataka is home to India's first silk farm."
                if (_todayMarkets.value.isNullOrEmpty()) {
                    _todayMarkets.value = getMockMarkets()
                }
            }
        }
    }

    fun searchVendors(newQuery: String) {
        query = newQuery
        applyFilters()
    }

    fun filterVendorsByCategory(category: VendorCategory?) {
        selectedCategory = category
        applyFilters()
    }

    private fun getMockMarkets() = listOf(
        Santhe(id="m1", villageName = "Hampi Market", dayOfWeek = "TODAY", specialtyGoods = listOf("Banana Fibers", "Statues"), description = "Historic market near temples."),
        Santhe(id="m2", villageName = "Mysuru Santhe", dayOfWeek = "TODAY", specialtyGoods = listOf("Sandalwood", "Silk"), description = "Famous for royal heritage goods."),
        Santhe(id="m3", villageName = "Dharwad Santhe", dayOfWeek = "TODAY", specialtyGoods = listOf("Peda", "Textiles"), description = "Traditional Tuesday market.")
    )
}
