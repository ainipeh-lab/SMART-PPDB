package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApplicantEntity
import com.example.data.model.MajorEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SchoolSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicantDao {
    @Query("SELECT * FROM applicants ORDER BY id DESC")
    fun getAllApplicants(): Flow<List<ApplicantEntity>>

    @Query("SELECT * FROM applicants WHERE id = :id")
    suspend fun getApplicantById(id: Long): ApplicantEntity?

    @Query("SELECT COUNT(*) FROM applicants")
    fun getApplicantCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM applicants WHERE majorCode = :majorCode")
    fun getCountByMajor(majorCode: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplicant(applicant: ApplicantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(applicants: List<ApplicantEntity>)

    @Update
    suspend fun updateApplicant(applicant: ApplicantEntity)

    @Delete
    suspend fun deleteApplicant(applicant: ApplicantEntity)

    @Query("DELETE FROM applicants WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MajorDao {
    @Query("SELECT * FROM majors ORDER BY code ASC")
    fun getAllMajors(): Flow<List<MajorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(majors: List<MajorEntity>)

    @Update
    suspend fun updateMajor(major: MajorEntity)

    @Query("SELECT * FROM majors WHERE code = :code")
    suspend fun getMajorByCode(code: String): MajorEntity?
}

@Dao
interface SchoolSettingsDao {
    @Query("SELECT * FROM registration_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SchoolSettingsEntity?>

    @Query("SELECT * FROM registration_settings WHERE id = 1")
    suspend fun getSettings(): SchoolSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SchoolSettingsEntity)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearLogs()
}
