package com.abhi.santheconnect.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhi.santheconnect.data.model.VendorCategory
import com.abhi.santheconnect.databinding.ItemCategoryBinding

class CategoryAdapter(private val onCategoryClick: (VendorCategory?) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = VendorCategory.entries
    private var selectedIndex: Int = -1

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: VendorCategory, position: Int, onClick: (VendorCategory?) -> Unit) {
            binding.tvEmoji.text = category.emoji
            binding.tvCategoryName.text = category.displayName.split(" ")[0]
            
            // Highlight selected category
            if (position == selectedIndex) {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#ECAC2F")) // ochre
                binding.tvCategoryName.setTextColor(android.graphics.Color.WHITE)
            } else {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
                binding.tvCategoryName.setTextColor(android.graphics.Color.BLACK)
            }

            binding.root.setOnClickListener { 
                val newSelection = if (selectedIndex == position) -1 else position
                val oldSelection = selectedIndex
                selectedIndex = newSelection
                
                notifyItemChanged(oldSelection)
                notifyItemChanged(newSelection)
                
                onClick(if (newSelection == -1) null else categories[newSelection])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position, onCategoryClick)
    }

    override fun getItemCount() = categories.size
}
