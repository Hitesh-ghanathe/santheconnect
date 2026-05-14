package com.hitesh.santheconnect.data.local.dao

import androidx.room.*
import com.hitesh.santheconnect.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :vendorId")
    suspend fun getVendorById(vendorId: String): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<VendorEntity>)

    @Query("DELETE FROM vendors")
    suspend fun clearAll()
}
