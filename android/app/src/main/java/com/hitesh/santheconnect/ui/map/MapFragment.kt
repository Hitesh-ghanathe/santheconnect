package com.hitesh.santheconnect.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hitesh.santheconnect.R
import com.hitesh.santheconnect.data.model.Vendor
import com.hitesh.santheconnect.data.model.VendorCategory
import com.hitesh.santheconnect.databinding.FragmentMapBinding
import com.hitesh.santheconnect.ui.vendor.AddVendorActivity
import com.hitesh.santheconnect.ui.vendor.VendorDetailActivity
import com.hitesh.santheconnect.utils.showToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import coil.load
import coil.transform.RoundedCornersTransformation

import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Karnataka center coordinates
    private val karnatakaCenter = LatLng(15.3173, 75.7139)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableMyLocation()
            centerOnUserLocation()
        }
        else requireContext().showToast("Location permission needed for distances")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.vendorPreviewSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.btnRecenter.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                centerOnUserLocation()
            } else {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(karnatakaCenter, 7f))
            }
        }

        setupCategoryChips()
        setupFab()
        observeViewModel()
    }

    private fun setupCategoryChips() {
        val categories = listOf(null) + VendorCategory.entries
        val labels = listOf("All") + VendorCategory.entries.map { "${it.emoji} ${it.displayName}" }

        labels.forEachIndexed { index, label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setChipBackgroundColorResource(R.color.chip_bg_selector)
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
            }
            chip.setOnClickListener {
                viewModel.filterByCategory(if (index == 0) null else VendorCategory.entries[index - 1])
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun setupFab() {
        binding.fabAddVendor.setOnClickListener {
            startActivity(Intent(requireContext(), AddVendorActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.vendors.observe(viewLifecycleOwner) { vendors ->
            updateMapMarkers(vendors)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { 
                requireContext().showToast(it)
                android.util.Log.e("MapFragment", "ViewModel Error: $it")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Use a cleaner map style if needed, but for now just standard
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(karnatakaCenter, 7f))
        map.uiSettings.apply {
            isZoomControlsEnabled = true // Enabled for better UX
            isMyLocationButtonEnabled = false // We use our own recenter button
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
        checkLocationPermission()
        viewModel.vendors.value?.let { updateMapMarkers(it) }

        map.setOnMarkerClickListener { marker ->
            val vendorId = marker.tag as? String ?: return@setOnMarkerClickListener false
            showVendorPreview(vendorId)
            true
        }

        map.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showVendorPreview(vendorId: String) {
        val vendor = viewModel.vendors.value?.find { it.id == vendorId } ?: return
        
        binding.tvPreviewName.text = vendor.name
        binding.tvPreviewCategory.text = try { VendorCategory.valueOf(vendor.category).displayName } catch(e: Exception) { vendor.category }
        
        if (vendor.photoUrls.isNotEmpty()) {
            binding.ivPreviewPhoto.load(vendor.photoUrls.first()) {
                crossfade(true)
                transformations(RoundedCornersTransformation(12f))
            }
        } else {
            binding.ivPreviewPhoto.setImageResource(R.drawable.ic_image_placeholder)
        }

        binding.btnViewDetails.setOnClickListener {
            val intent = Intent(requireContext(), VendorDetailActivity::class.java)
            intent.putExtra("vendor_id", vendorId)
            startActivity(intent)
        }

        binding.btnDirections.setOnClickListener {
            val gmmIntentUri = android.net.Uri.parse("google.navigation:q=${vendor.latitude},${vendor.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                requireContext().showToast("Google Maps not found")
            }
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateMapMarkers(vendors: List<Vendor>) {
        googleMap?.let { map ->
            map.clear()
            if (vendors.isEmpty()) return@let

            val boundsBuilder = LatLngBounds.Builder()
            var hasMarkers = false

            vendors.forEach { vendor ->
                if (vendor.latitude != 0.0 && vendor.longitude != 0.0) {
                    val position = LatLng(vendor.latitude, vendor.longitude)
                    boundsBuilder.include(position)
                    hasMarkers = true

                    val hue = when (vendor.category) {
                        "FOOD"   -> BitmapDescriptorFactory.HUE_ORANGE
                        "MARKET" -> BitmapDescriptorFactory.HUE_YELLOW
                        "CRAFT"  -> BitmapDescriptorFactory.HUE_GREEN
                        "STAY"   -> BitmapDescriptorFactory.HUE_AZURE
                        else     -> BitmapDescriptorFactory.HUE_RED
                    }
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(vendor.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )
                    marker?.tag = vendor.id
                }
            }

            if (hasMarkers && viewModel.selectedCategory != null) {
                val padding = 200 // offset from edges of the map in pixels
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding))
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            centerOnUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocation() {
        try { googleMap?.isMyLocationEnabled = true } catch (_: SecurityException) {}
    }

    private fun centerOnUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                }
            }
        } catch (_: SecurityException) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

