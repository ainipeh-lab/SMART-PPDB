package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreSpmbRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan akun Google sebelum mengakses Firestore.")
    }

    fun observeApplicants(): Flow<List<FirestoreApplicant>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/applicants"
        val query = db.collection("users").document(uid).collection("applicants")
        query.snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreApplicant::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun addApplicant(applicant: FirestoreApplicant): String {
        val uid = requireUserId()
        val path = "users/$uid/applicants"
        val docRef = db.collection("users").document(uid).collection("applicants").document(
            if (applicant.id.isNotBlank()) applicant.id else UUID.randomUUID().toString()
        )

        try {
            val payload = hashMapOf<String, Any?>(
                "userId" to uid,
                "registrationNumber" to applicant.registrationNumber,
                "fullName" to applicant.fullName,
                "gender" to applicant.gender,
                "schoolOrigin" to applicant.schoolOrigin,
                "phone" to applicant.phone,
                "majorCode" to applicant.majorCode,
                "registrationDate" to (if (applicant.registrationDate > 0) applicant.registrationDate else System.currentTimeMillis()),
                "verificationStatus" to applicant.verificationStatus,
                "admissionStatus" to applicant.admissionStatus,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!applicant.nisn.isNullOrBlank()) payload["nisn"] = applicant.nisn
            if (!applicant.adminNotes.isNullOrBlank()) payload["adminNotes"] = applicant.adminNotes

            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun updateApplicant(applicant: FirestoreApplicant) {
        val uid = requireUserId()
        require(applicant.id.isNotBlank()) { "ID pendaftar tidak boleh kosong saat update" }
        val docRef = db.collection("users").document(uid).collection("applicants").document(applicant.id)

        try {
            val payload = hashMapOf<String, Any?>(
                "userId" to uid,
                "registrationNumber" to applicant.registrationNumber,
                "fullName" to applicant.fullName,
                "gender" to applicant.gender,
                "schoolOrigin" to applicant.schoolOrigin,
                "phone" to applicant.phone,
                "majorCode" to applicant.majorCode,
                "registrationDate" to applicant.registrationDate,
                "verificationStatus" to applicant.verificationStatus,
                "admissionStatus" to applicant.admissionStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!applicant.nisn.isNullOrBlank()) payload["nisn"] = applicant.nisn
            if (!applicant.adminNotes.isNullOrBlank()) payload["adminNotes"] = applicant.adminNotes

            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteApplicant(applicantId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("applicants").document(applicantId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeSchoolSettings(): Flow<FirestoreSchoolSettings> = flow {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("school")
        docRef.snapshots()
            .map { snapshot ->
                snapshot.toObject(FirestoreSchoolSettings::class.java)
                    ?: FirestoreSchoolSettings(userId = uid)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveSchoolSettings(settings: FirestoreSchoolSettings) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("school")
        try {
            val payload = hashMapOf<String, Any?>(
                "userId" to uid,
                "schoolName" to settings.schoolName,
                "academicYear" to settings.academicYear,
                "overallTarget" to settings.overallTarget,
                "principalName" to settings.principalName,
                "principalWhatsApp" to settings.principalWhatsApp,
                "adminName" to settings.adminName,
                "adminWhatsApp" to settings.adminWhatsApp,
                "dailyReportTime" to settings.dailyReportTime,
                "notifyPrincipalOnNewApplicant" to settings.notifyPrincipalOnNewApplicant,
                "notifyAdminOnNewApplicant" to settings.notifyAdminOnNewApplicant,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!settings.waCloudApiToken.isNullOrBlank()) payload["waCloudApiToken"] = settings.waCloudApiToken
            if (!settings.waCloudPhoneNumberId.isNullOrBlank()) payload["waCloudPhoneNumberId"] = settings.waCloudPhoneNumberId

            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeNotificationLogs(): Flow<List<FirestoreNotificationLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notification_logs"
        val query = db.collection("users").document(uid).collection("notification_logs")
        query.snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreNotificationLog::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun addNotificationLog(log: FirestoreNotificationLog): String {
        val uid = requireUserId()
        val path = "users/$uid/notification_logs"
        val docRef = db.collection("users").document(uid).collection("notification_logs").document(
            if (log.id.isNotBlank()) log.id else UUID.randomUUID().toString()
        )
        try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "recipientType" to log.recipientType,
                "recipientName" to log.recipientName,
                "recipientNumber" to log.recipientNumber,
                "reportType" to log.reportType,
                "status" to log.status,
                "messageContent" to log.messageContent,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }
}
