package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applicants")
data class ApplicantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registrationNumber: String,
    val fullName: String,
    val nisn: String = "",
    val gender: String, // "Laki-laki" | "Perempuan"
    val schoolOrigin: String,
    val phone: String,
    val majorCode: String, // "TKJ", "TO", "TSM", "DKV", "AK"
    val registrationDate: Long = System.currentTimeMillis(),
    val verificationStatus: String = "Menunggu verifikasi", // "Menunggu verifikasi", "Terverifikasi", "Perlu perbaikan"
    val admissionStatus: String = "Menunggu keputusan", // "Menunggu keputusan", "Diterima", "Tidak diterima"
    val adminNotes: String = ""
)

@Entity(tableName = "majors")
data class MajorEntity(
    @PrimaryKey
    val code: String, // "TKJ", "TO", "TSM", "DKV", "AK"
    val name: String,
    val quota: Int,
    val description: String = ""
)

@Entity(tableName = "registration_settings")
data class SchoolSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
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
    val waCloudApiToken: String = "",
    val waCloudPhoneNumberId: String = ""
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val recipientType: String, // "KEPALA_SEKOLAH", "ADMIN_SPMB", "KEDUANYA", "SISWA"
    val recipientName: String,
    val recipientNumber: String,
    val reportType: String, // "HARIAN", "MINGGUAN", "BULANAN", "PENDAFTAR_BARU", "TEST"
    val status: String, // "BERHASIL", "TERKIRIM_VIA_WA", "PENDING", "GAGAL"
    val messageContent: String
)

enum class UserRole(val displayName: String, val description: String) {
    SUPER_ADMIN("Super Admin", "Kelola semua data, akun, target & integrasi WhatsApp"),
    ADMIN_SPMB("Admin SPMB", "Kelola data pendaftar, verifikasi & kirim laporan"),
    KEPALA_SEKOLAH("Kepala Sekolah", "Pantau statistik, dashboard & terima laporan")
}

data class UserSession(
    val id: String,
    val name: String,
    val role: UserRole
)
