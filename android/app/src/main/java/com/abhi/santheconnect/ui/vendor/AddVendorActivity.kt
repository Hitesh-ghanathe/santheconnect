package com.abhi.santheconnect.ui.vendor

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abhi.santheconnect.R
import com.abhi.santheconnect.data.local.SantheDatabase
import com.abhi.santheconnect.data.model.Vendor
import com.abhi.santheconnect.data.model.VendorCategory
import com.abhi.santheconnect.data.repository.SantheRepository
import com.abhi.santheconnect.databinding.ActivityAddVendorBinding
import com.abhi.santheconnect.utils.LocationUtils
import com.abhi.santheconnect.utils.showToast
import kotlinx.coroutines.launch

class AddVendorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVendorBinding
    private val repo by lazy { 
        val db = SantheDatabase.getDatabase(applicationContext)
        SantheRepository(db.vendorDao()) 
    }
    private var capturedLat: Double = 0.0
    private var capturedLng: Double = 0.0

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) captureGps() else showToast("Location permission needed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVendorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Local Vendor"

        // Populate category spinner with custom layout to fix visibility
        val categoryNames = VendorCategory.entries.map { "${it.emoji} ${it.displayName}" }
        val spinnerAdapter = android.widget.ArrayAdapter(this,
            R.layout.item_spinner, categoryNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        binding.btnCaptureGps.setOnClickListener {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.btnSubmit.setOnClickListener { submitVendor() }
    }

    private fun captureGps() {
        lifecycleScope.launch {
            binding.btnCaptureGps.isEnabled = false
            binding.btnCaptureGps.text = "Capturing GPS…"
            val location = LocationUtils.getCurrentLocation(this@AddVendorActivity)
            if (location != null) {
                capturedLat = location.latitude
                capturedLng = location.longitude
                binding.tvGpsStatus.text = "📍 %.5f, %.5f".format(capturedLat, capturedLng)
                binding.tvGpsStatus.visibility = View.VISIBLE
                showToast("Location captured!")
            } else {
                showToast("Could not get GPS. Try again.")
            }
            binding.btnCaptureGps.isEnabled = true
            binding.btnCaptureGps.text = "📍 Capture GPS Location"
        }
    }

    private fun submitVendor() {
        val name = binding.etVendorName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val categoryIndex = binding.spinnerCategory.selectedItemPosition

        if (name.isBlank()) { showToast("Enter a vendor name"); return }
        if (capturedLat == 0.0 && capturedLng == 0.0) { showToast("Capture GPS location first"); return }

        val vendor = Vendor(
            name = name,
            description = description,
            category = VendorCategory.entries[categoryIndex].name,
            latitude = capturedLat,
            longitude = capturedLng
        )

        lifecycleScope.launch {
            binding.btnSubmit.isEnabled = false
            try {
                repo.addVendor(vendor)
                showToast("✅ Vendor added successfully!")
                finish()
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
