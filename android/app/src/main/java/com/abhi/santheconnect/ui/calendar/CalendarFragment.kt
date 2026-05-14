package com.abhi.santheconnect.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhi.santheconnect.databinding.DialogAddTaskBinding
import com.abhi.santheconnect.databinding.FragmentCalendarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private val adapter = CalendarAdapter { task -> viewModel.deleteTask(task) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSanthe.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSanthe.adapter = adapter

        setupCalendar()
        setupFab()
        observeViewModel()
    }

    private fun setupFab() {
        binding.fabAddEvent.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Task for ${viewModel.selectedDate.value}")
            .setView(dialogBinding.root)
            .setPositiveButton("Add Task") { _, _ ->
                val name = dialogBinding.etTaskName.text.toString()
                val desc = dialogBinding.etTaskDescription.text.toString()
                if (name.isNotBlank()) {
                    viewModel.addTask(name, desc)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            
            val dbDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time).uppercase()
            val displayDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
            
            binding.tvSelectedDate.text = "$displayDate ($dayName)"
            
            viewModel.setSelectedDate(dbDate)
            viewModel.loadForDay(dayName)
        }
        
        // Initial setup
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvSelectedDate.text = "$today (${viewModel.todayDay})"
    }

    private fun observeViewModel() {
        viewModel.combinedList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { 
        super.onDestroyView()
        _binding = null 
    }
}
