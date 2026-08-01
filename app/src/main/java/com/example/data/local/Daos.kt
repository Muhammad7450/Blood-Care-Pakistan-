package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BloodCampEntity
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonationRecordEntity
import com.example.data.model.DonorEntity
import com.example.data.model.HospitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors ORDER BY totalDonations DESC")
    fun getAllDonors(): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE city = :city ORDER BY isAvailable DESC")
    fun getDonorsByCity(city: String): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup AND isAvailable = 1")
    fun getAvailableDonorsByBloodGroup(bloodGroup: String): Flow<List<DonorEntity>>

    @Query("SELECT * FROM donors WHERE id = :id")
    suspend fun getDonorById(id: Int): DonorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: DonorEntity): Long

    @Update
    suspend fun updateDonor(donor: DonorEntity)

    @Query("UPDATE donors SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: Int, isAvailable: Boolean)

    @Query("DELETE FROM donors WHERE id = :id")
    suspend fun deleteDonor(id: Int)
}

@Dao
interface BloodRequestDao {
    @Query("SELECT * FROM blood_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<BloodRequestEntity>>

    @Query("SELECT * FROM blood_requests WHERE emergencyLevel = 'Critical' OR emergencyLevel = 'Urgent' ORDER BY createdAt DESC")
    fun getEmergencyRequests(): Flow<List<BloodRequestEntity>>

    @Query("SELECT * FROM blood_requests WHERE isThalassemia = 1 ORDER BY createdAt DESC")
    fun getThalassemiaRequests(): Flow<List<BloodRequestEntity>>

    @Query("SELECT * FROM blood_requests WHERE id = :id")
    suspend fun getRequestById(id: Int): BloodRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BloodRequestEntity): Long

    @Update
    suspend fun updateRequest(request: BloodRequestEntity)

    @Query("UPDATE blood_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Int, status: String)

    @Query("DELETE FROM blood_requests WHERE id = :id")
    suspend fun deleteRequest(id: Int)
}

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals ORDER BY name ASC")
    fun getAllHospitals(): Flow<List<HospitalEntity>>

    @Query("SELECT * FROM hospitals WHERE city = :city")
    fun getHospitalsByCity(city: String): Flow<List<HospitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospital(hospital: HospitalEntity): Long

    @Update
    suspend fun updateHospital(hospital: HospitalEntity)

    @Query("UPDATE hospitals SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateHospitalVerification(id: Int, isVerified: Boolean)
}

@Dao
interface DonationRecordDao {
    @Query("SELECT * FROM donation_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<DonationRecordEntity>>

    @Query("SELECT * FROM donation_records WHERE donorId = :donorId ORDER BY id DESC")
    fun getRecordsForDonor(donorId: Int): Flow<List<DonationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DonationRecordEntity): Long
}

@Dao
interface BloodCampDao {
    @Query("SELECT * FROM blood_camps ORDER BY id DESC")
    fun getAllCamps(): Flow<List<BloodCampEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamp(camp: BloodCampEntity): Long

    @Query("UPDATE blood_camps SET registeredCount = registeredCount + 1 WHERE id = :id")
    suspend fun registerForCamp(id: Int)
}
