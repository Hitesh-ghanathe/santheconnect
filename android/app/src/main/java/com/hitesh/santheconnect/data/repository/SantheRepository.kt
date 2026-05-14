package com.hitesh.santheconnect.data.repository

import android.net.Uri
import com.hitesh.santheconnect.data.local.dao.VendorDao
import com.hitesh.santheconnect.data.local.dao.TaskDao
import com.hitesh.santheconnect.data.local.entity.VendorEntity
import com.hitesh.santheconnect.data.local.entity.TaskEntity
import com.hitesh.santheconnect.data.model.Review
import com.hitesh.santheconnect.data.model.Santhe
import com.hitesh.santheconnect.data.model.Vendor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SantheRepository(
    private val vendorDao: VendorDao? = null,
    private val taskDao: TaskDao? = null
) {

    private val db: FirebaseFirestore = Firebase.firestore
    private val storage = Firebase.storage

    // ─── Tasks ──────────────────────────────────────────────────────────────

    fun getTasksForDate(date: String): Flow<List<TaskEntity>>? {
        return taskDao?.getTasksForDate(date)
    }

    suspend fun addTask(task: TaskEntity) {
        taskDao?.insertTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao?.deleteTask(task)
    }

    fun getDatesWithTasks(): Flow<List<String>>? {
        return taskDao?.getDatesWithTasks()
    }

    // ─── Vendors ──────────────────────────────────────────────────────────────

    /**
     * Get vendors as a Flow from the local database (Offline-first)
     */
    fun getLocalVendorsFlow(): Flow<List<Vendor>>? {
        return vendorDao?.getAllVendors()?.map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Refresh vendors from Firebase and save to local Room DB
     */
    suspend fun refreshVendors() {
        try {
            val remoteVendors = db.collection("vendors")
                .whereEqualTo("isActive", true)
                .get().await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Vendor::class.java)?.copy(id = doc.id)
                }
            
            vendorDao?.let { dao ->
                dao.clearAll()
                dao.insertVendors(remoteVendors.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle error or just ignore if offline
        }
    }

    suspend fun getVendors(): List<Vendor> {
        val remoteVendors = db.collection("vendors")
            .whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Vendor::class.java)?.copy(id = doc.id)
            }
        
        // Sync to local DB if available
        vendorDao?.insertVendors(remoteVendors.map { it.toEntity() })
        
        return remoteVendors
    }

    suspend fun getVendorsByCategory(category: String): List<Vendor> {
        return db.collection("vendors")
            .whereEqualTo("category", category)
            .whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Vendor::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun addVendor(vendor: Vendor): String {
        val ref = db.collection("vendors").add(vendor).await()
        val newVendor = vendor.copy(id = ref.id)
        
        // Save to local DB immediately for interactivity
        vendorDao?.insertVendors(listOf(newVendor.toEntity()))
        
        return ref.id
    }

    suspend fun updateVendorTags(vendorId: String, tags: List<String>) {
        db.collection("vendors").document(vendorId)
            .update("specialtyTags", tags).await()
            
        // Update local DB
        vendorDao?.getVendorById(vendorId)?.let { entity ->
            vendorDao.insertVendors(listOf(entity.copy(specialtyTags = tags)))
        }
    }

    // ─── Santhe Calendar ──────────────────────────────────────────────────────

    suspend fun getAllSanthe(): List<Santhe> {
        return db.collection("santhe")
            .whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Santhe::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getSantheByDay(day: String): List<Santhe> {
        return db.collection("santhe")
            .whereEqualTo("dayOfWeek", day)
            .whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Santhe::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun addSanthe(santhe: Santhe): String {
        val ref = db.collection("santhe").add(santhe).await()
        return ref.id
    }

    // ─── Reviews ──────────────────────────────────────────────────────────────

    suspend fun getReviews(vendorId: String): List<Review> {
        return db.collection("reviews")
            .whereEqualTo("vendorId", vendorId)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getAllReviews(): List<Review> {
        return db.collection("reviews")
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun addReview(review: Review): String {
        val ref = db.collection("reviews").add(review).await()
        return ref.id
    }

    // ─── Storage Upload ────────────────────────────────────────────────────────

    suspend fun uploadMedia(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadAudio(bytes: ByteArray, path: String): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    // ─── Mapper Extensions ────────────────────────────────────────────────────

    private fun Vendor.toEntity() = VendorEntity(
        id = id,
        name = name,
        category = category,
        latitude = latitude,
        longitude = longitude,
        description = description,
        photoUrls = photoUrls,
        specialtyTags = specialtyTags,
        averageRating = averageRating,
        reviewCount = reviewCount,
        submittedBy = submittedBy
    )

    private fun VendorEntity.toDomainModel() = Vendor(
        id = id,
        name = name,
        category = category,
        latitude = latitude,
        longitude = longitude,
        description = description,
        photoUrls = photoUrls,
        specialtyTags = specialtyTags,
        averageRating = averageRating,
        reviewCount = reviewCount,
        submittedBy = submittedBy
    )
}
