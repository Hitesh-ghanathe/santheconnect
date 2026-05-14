package com.abhi.santheconnect.ui.calendar

import com.abhi.santheconnect.data.local.entity.TaskEntity
import com.abhi.santheconnect.data.model.Santhe

sealed class CalendarItem {
    data class SantheItem(val santhe: Santhe) : CalendarItem()
    data class TaskItem(val task: TaskEntity) : CalendarItem()
    data class Header(val title: String) : CalendarItem()
}
