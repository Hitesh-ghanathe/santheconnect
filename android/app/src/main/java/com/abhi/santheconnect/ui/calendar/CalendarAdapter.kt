package com.abhi.santheconnect.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abhi.santheconnect.R
import com.abhi.santheconnect.data.local.entity.TaskEntity
import com.abhi.santheconnect.data.model.Santhe
import com.abhi.santheconnect.databinding.ItemCalendarHeaderBinding
import com.abhi.santheconnect.databinding.ItemSantheCardBinding
import com.abhi.santheconnect.databinding.ItemTaskCardBinding

class CalendarAdapter(private val onDeleteTask: (TaskEntity) -> Unit) : 
    ListAdapter<CalendarItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CalendarItem.Header -> R.layout.item_calendar_header
            is CalendarItem.SantheItem -> R.layout.item_santhe_card
            is CalendarItem.TaskItem -> R.layout.item_task_card
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_calendar_header -> HeaderViewHolder(
                ItemCalendarHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_santhe_card -> SantheViewHolder(
                ItemSantheCardBinding.inflate(inflater, parent, false)
            )
            R.layout.item_task_card -> TaskViewHolder(
                ItemTaskCardBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CalendarItem.Header -> (holder as HeaderViewHolder).bind(item)
            is CalendarItem.SantheItem -> (holder as SantheViewHolder).bind(item.santhe)
            is CalendarItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
        }
    }

    class HeaderViewHolder(private val binding: ItemCalendarHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: CalendarItem.Header) {
            binding.tvHeaderTitle.text = header.title
        }
    }

    class SantheViewHolder(private val binding: ItemSantheCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(santhe: Santhe) {
            binding.tvVillageName.text = santhe.villageName
            binding.tvDay.text = santhe.dayOfWeek.lowercase().replaceFirstChar { it.uppercase() }
            binding.tvGoods.text = santhe.specialtyGoods.joinToString(" • ")
            binding.tvDescription.text = santhe.description
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.btnDeleteTask.setOnClickListener { onDeleteTask(task) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CalendarItem>() {
            override fun areItemsTheSame(a: CalendarItem, b: CalendarItem): Boolean {
                return if (a is CalendarItem.SantheItem && b is CalendarItem.SantheItem) a.santhe.id == b.santhe.id
                else if (a is CalendarItem.TaskItem && b is CalendarItem.TaskItem) a.task.id == b.task.id
                else if (a is CalendarItem.Header && b is CalendarItem.Header) a.title == b.title
                else false
            }

            override fun areContentsTheSame(a: CalendarItem, b: CalendarItem) = a == b
        }
    }
}
