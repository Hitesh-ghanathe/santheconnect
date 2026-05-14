package com.abhi.santheconnect.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abhi.santheconnect.data.model.Santhe
import com.abhi.santheconnect.databinding.ItemSantheCardBinding

class SantheAdapter : ListAdapter<Santhe, SantheAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemSantheCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(santhe: Santhe) {
            binding.tvVillageName.text = santhe.villageName
            binding.tvDay.text = santhe.dayOfWeek.lowercase().replaceFirstChar { it.uppercase() }
            binding.tvGoods.text = santhe.specialtyGoods.joinToString(" • ")
            binding.tvDescription.text = santhe.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSantheCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Santhe>() {
            override fun areItemsTheSame(a: Santhe, b: Santhe) = a.id == b.id
            override fun areContentsTheSame(a: Santhe, b: Santhe) = a == b
        }
    }
}
