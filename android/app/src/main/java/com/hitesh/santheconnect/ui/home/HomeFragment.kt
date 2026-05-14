package com.hitesh.santheconnect.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.hitesh.santheconnect.R
import com.hitesh.santheconnect.databinding.FragmentHomeBinding
import com.hitesh.santheconnect.ui.calendar.SantheAdapter
import com.hitesh.santheconnect.ui.vendor.VendorAdapter
import com.hitesh.santheconnect.ui.vendor.VendorDetailActivity
import com.hitesh.santheconnect.utils.showToast
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocationAndTip()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Banner Image
        binding.ivBanner.load("https://images.unsplash.com/photo-1624008915317-cb3ad69b16ad?q=80&w=800") {
            crossfade(true)
            placeholder(R.drawable.ic_image_placeholder)
        }
        
        // Load Specialty Images
        binding.ivSpecialty1.load("https://images.unsplash.com/photo-1590424753051-893074878a1f?q=80&w=400") // Toys
        binding.ivSpecialty2.load("https://images.unsplash.com/photo-1589113110260-29337920786f?q=80&w=400") // Sweets
        binding.ivSpecialty3.load("https://images.unsplash.com/photo-1559056199-641a0ac8b55e?q=80&w=400") // Coffee

        // Search Bar functionality
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchVendors(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Flipkart style Categories
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val categoryAdapter = CategoryAdapter { category ->
            viewModel.filterVendorsByCategory(category)
            if (category != null) {
                requireContext().showToast("Showing ${category.displayName}")
            }
        }
        binding.rvCategories.adapter = categoryAdapter

        // Today's Markets
        binding.rvTodayMarkets.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val marketAdapter = SantheAdapter()
        binding.rvTodayMarkets.adapter = marketAdapter

        // Featured Vendors
        binding.rvFeaturedVendors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val vendorAdapter = VendorAdapter { vendor ->
            val intent = Intent(requireContext(), VendorDetailActivity::class.java)
            intent.putExtra("vendor_id", vendor.id)
            startActivity(intent)
        }
        binding.rvFeaturedVendors.adapter = vendorAdapter

        viewModel.todayMarkets.observe(viewLifecycleOwner) { 
            marketAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        }
        viewModel.featuredVendors.observe(viewLifecycleOwner) { vendorAdapter.submitList(it) }
        viewModel.aiSuggestion.observe(viewLifecycleOwner) { binding.tvAiSuggestion.text = it }
        viewModel.dailyFact.observe(viewLifecycleOwner) { binding.tvDailyFact.text = it }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
            checkLocationAndFetchTip()
        }

        checkLocationAndFetchTip()
    }

    private fun checkLocationAndFetchTip() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndTip()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun fetchLocationAndTip() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // In a real app, we'd reverse geocode or use lat/lng
                    // For now, we'll just trigger a "near you" refresh
                    viewModel.loadData() 
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
