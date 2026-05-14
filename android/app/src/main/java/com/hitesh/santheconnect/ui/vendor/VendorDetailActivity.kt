package com.hitesh.santheconnect.ui.vendor

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.hitesh.santheconnect.R
import com.hitesh.santheconnect.ai.GeminiHelper
import com.hitesh.santheconnect.data.local.SantheDatabase
import com.hitesh.santheconnect.data.model.Vendor
import com.hitesh.santheconnect.data.model.VendorCategory
import com.hitesh.santheconnect.data.repository.SantheRepository
import com.hitesh.santheconnect.databinding.ActivityVendorDetailBinding
import com.hitesh.santheconnect.ui.reviews.ReviewAdapter
import com.hitesh.santheconnect.ui.reviews.ReviewViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class VendorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVendorDetailBinding
    private val repo by lazy { 
        val db = SantheDatabase.getDatabase(applicationContext)
        SantheRepository(db.vendorDao()) 
    }
    private val reviewViewModel: ReviewViewModel by viewModels()
    private val reviewAdapter = ReviewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vendorId = intent.getStringExtra("vendor_id") ?: run { finish(); return }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter

        loadVendor(vendorId)
        reviewViewModel.loadReviewsForVendor(vendorId)
        reviewViewModel.reviews.observe(this) { reviewAdapter.submitList(it) }
    }

    private fun loadVendor(vendorId: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            try {
                val vendors = repo.getVendors()
                val vendor = vendors.find { it.id == vendorId } ?: return@launch
                bindVendor(vendor)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun bindVendor(vendor: Vendor) {
        supportActionBar?.title = vendor.name
        binding.tvVendorName.text = vendor.name
        
        // Fix: Show display name instead of internal enum name
        binding.tvCategory.text = try { 
            VendorCategory.valueOf(vendor.category).displayName 
        } catch (e: Exception) { 
            vendor.category 
        }

        binding.tvDescription.text = vendor.description

        if (vendor.photoUrls.isNotEmpty()) {
            binding.ivVendorPhoto.load(vendor.photoUrls.first()) {
                crossfade(true)
                transformations(RoundedCornersTransformation(12f))
                placeholder(R.drawable.ic_image_placeholder)
            }
        }

        if (vendor.specialtyTags.isNotEmpty()) {
            showTags(vendor.specialtyTags)
        } else {
            generateTags(vendor)
        }

        binding.btnGetDirections.setOnClickListener {
            val uri = "geo:${vendor.latitude},${vendor.longitude}?q=${vendor.name}"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
            startActivity(intent)
        }
    }

    private fun generateTags(vendor: Vendor) {
        lifecycleScope.launch {
            binding.tvTagsLabel.text = "Generating AI tags…"
            val tags = GeminiHelper.generateSpecialtyTags(
                vendorName = vendor.name,
                category = vendor.category,
                description = vendor.description
            )
            if (tags.isNotEmpty()) {
                repo.updateVendorTags(vendor.id, tags)
                showTags(tags)
            }
            binding.tvTagsLabel.text = "Specialty Tags"
        }
    }

    private fun showTags(tags: List<String>) {
        binding.chipGroupTags.removeAllViews()
        tags.forEach { tag ->
            val chip = Chip(this).apply {
                text = tag
                isClickable = false
                setChipBackgroundColorResource(R.color.olive_green_light)
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    override fun onSupportNavigateUp(): Boolean { 
        onBackPressedDispatcher.onBackPressed()
        return true 
    }
}
