package com.hitesh.santheconnect.ui.calendar

import android.app.Application
import androidx.lifecycle.*
import com.hitesh.santheconnect.data.local.SantheDatabase
import com.hitesh.santheconnect.data.local.entity.TaskEntity
import com.hitesh.santheconnect.data.model.Santhe
import com.hitesh.santheconnect.data.repository.SantheRepository
import com.hitesh.santheconnect.utils.getCurrentDayName
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SantheDatabase.getDatabase(application)
    private val repo = SantheRepository(db.vendorDao(), db.taskDao())

    private val _santheList = MutableLiveData<List<Santhe>>()
    val santheList: LiveData<List<Santhe>> = _santheList

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val todayDay: String = getCurrentDayName()
    var selectedDay: String = todayDay
        private set

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    // Observe tasks for the selected date
    val tasks: LiveData<List<TaskEntity>> = _selectedDate.asFlow().flatMapLatest { date ->
        repo.getTasksForDate(date) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.asLiveData()

    // Observe all dates that have tasks to mark them on the calendar
    val datesWithTasks: LiveData<List<String>> = repo.getDatesWithTasks()?.asLiveData() ?: MutableLiveData(emptyList())

    private val _combinedList = MediatorLiveData<List<CalendarItem>>()
    val combinedList: LiveData<List<CalendarItem>> = _combinedList

    init {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        _combinedList.addSource(_santheList) { combine() }
        _combinedList.addSource(tasks) { combine() }

        _selectedDate.value = today
        loadForDay(todayDay)
    }

    private fun combine() {
        val items = mutableListOf<CalendarItem>()
        
        val currentTasks = tasks.value ?: emptyList()
        if (currentTasks.isNotEmpty()) {
            items.add(CalendarItem.Header("My Tasks"))
            items.addAll(currentTasks.map { CalendarItem.TaskItem(it) })
        }

        val currentSanthes = _santheList.value ?: emptyList()
        if (currentSanthes.isNotEmpty()) {
            items.add(CalendarItem.Header("Local Markets"))
            items.addAll(currentSanthes.map { CalendarItem.SantheItem(it) })
        }
        
        _combinedList.value = items
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun loadForDay(day: String) {
        selectedDay = day
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _santheList.value = repo.getSantheByDay(day)
            } catch (e: Exception) {
                _santheList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(title: String, description: String) {
        val date = _selectedDate.value ?: return
        viewModelScope.launch {
            repo.addTask(TaskEntity(date = date, title = title, description = description))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repo.deleteTask(task)
        }
    }
}
