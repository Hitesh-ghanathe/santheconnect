package com.hitesh.santheconnect.ui.calendar

import com.hitesh.santheconnect.data.local.entity.TaskEntity
import com.hitesh.santheconnect.data.model.Santhe

sealed class CalendarItem {
    data class SantheItem(val santhe: Santhe) : CalendarItem()
    data class TaskItem(val task: TaskEntity) : CalendarItem()
    data class Header(val title: String) : CalendarItem()
}
