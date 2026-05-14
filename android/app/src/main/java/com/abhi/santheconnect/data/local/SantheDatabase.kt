package com.abhi.santheconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abhi.santheconnect.data.local.dao.VendorDao
import com.abhi.santheconnect.data.local.dao.TaskDao
import com.abhi.santheconnect.data.local.entity.VendorEntity
import com.abhi.santheconnect.data.local.entity.TaskEntity

@Database(entities = [VendorEntity::class, TaskEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SantheDatabase : RoomDatabase() {
    abstract fun vendorDao(): VendorDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: SantheDatabase? = null

        fun getDatabase(context: Context): SantheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SantheDatabase::class.java,
                    "santhe_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
