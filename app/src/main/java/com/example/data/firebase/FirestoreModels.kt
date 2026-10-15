package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "SUPER_ADMIN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreApplicant(
    @DocumentId val id: String = "",
    val userId: String = "",
    val registrationNumber: String = "",
    val fullName: String = "",
    val nisn: String? = null,
    val gender: String = "Laki-laki",
    val schoolOrigin: String = "",
    val phone: String = "",
    val majorCode: String = "TKJ",
    val registrationDate: Long = 0L,
    val verificationStatus: String = "Menunggu verifikasi",
    val admissionStatus: String = "Menunggu keputusan",
    val adminNotes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreSchoolSettings(
    @DocumentId val id: String = "school",
    val userId: String = "",
    val schoolName: String = "SMK Negeri 1 Teknologi Unggul",
    val academicYear: String = "2027/2028",
    val overallTarget: Int = 500,
    val principalName: String = "Drs. H. Ahmad Fauzi, M.Pd",
    val principalWhatsApp: String = "6281234567890",
    val adminName: String = "Budi Santoso, S.Kom",
    val adminWhatsApp: String = "6281987654321",
    val dailyReportTime: String = "16:00",
    val notifyPrincipalOnNewApplicant: Boolean = false,
    val notifyAdminOnNewApplicant: Boolean = true,
    val waCloudApiToken: String? = null,
    val waCloudPhoneNumberId: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreNotificationLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recipientType: String = "",
    val recipientName: String = "",
    val recipientNumber: String = "",
    val reportType: String = "",
    val status: String = "",
    val messageContent: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
