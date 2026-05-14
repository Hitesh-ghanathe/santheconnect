package com.abhi.santheconnect.ui.vendor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.abhi.santheconnect.R
import com.abhi.santheconnect.data.model.Vendor
import com.abhi.santheconnect.data.model.VendorCategory
import com.abhi.santheconnect.databinding.ItemVendorCardBinding

class VendorAdapter(private val onVendorClick: (Vendor) -> Unit) :
    ListAdapter<Vendor, VendorAdapter.VendorViewHolder>(DiffCallback) {

    class VendorViewHolder(private val binding: ItemVendorCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vendor: Vendor, onVendorClick: (Vendor) -> Unit) {
            binding.tvVendorName.text = vendor.name
            binding.tvCategory.text = try {
                VendorCategory.valueOf(vendor.category).displayName
            } catch (e: Exception) {
                vendor.category
            }
            binding.tvRating.text = "%.1f ★".format(vendor.averageRating)
            binding.tvReviewCount.text = "(%d)".format(vendor.reviewCount)
            
            if (vendor.photoUrls.isNotEmpty()) {
                binding.ivVendorPhoto.load(vendor.photoUrls.first()) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(8f))
                    error(R.drawable.ic_image_placeholder)
                }
            } else {
                binding.ivVendorPhoto.setImageResource(R.drawable.ic_image_placeholder)
            }
            
            binding.root.setOnClickListener { onVendorClick(vendor) }
            binding.btnView.setOnClickListener { onVendorClick(vendor) }
            binding.btnFavorite.setOnClickListener { 
                // Toggle favorite
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        return VendorViewHolder(
            ItemVendorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        holder.bind(getItem(position), onVendorClick)
    }

    object DiffCallback : DiffUtil.ItemCallback<Vendor>() {
        override fun areItemsTheSame(oldItem: Vendor, newItem: Vendor) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Vendor, newItem: Vendor) = oldItem == newItem
    }
}
