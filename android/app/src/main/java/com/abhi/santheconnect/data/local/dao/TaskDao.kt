package com.abhi.santheconnect.data.local.dao

import androidx.room.*
import com.abhi.santheconnect.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt DESC")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT DISTINCT date FROM tasks")
    fun getDatesWithTasks(): Flow<List<String>>
}
