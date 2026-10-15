package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.ApplicantEntity
import com.example.data.model.MajorEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SchoolSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SpmbRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val applicantDao = database.applicantDao()
    private val majorDao = database.majorDao()
    private val settingsDao = database.schoolSettingsDao()
    private val logDao = database.notificationLogDao()
    private val okHttpClient = OkHttpClient()

    val applicants: Flow<List<ApplicantEntity>> = applicantDao.getAllApplicants()
    val majors: Flow<List<MajorEntity>> = majorDao.getAllMajors()
    val settings: Flow<SchoolSettingsEntity?> = settingsDao.getSettingsFlow()
    val notificationLogs: Flow<List<NotificationLogEntity>> = logDao.getAllLogs()

    suspend fun getSettings(): SchoolSettingsEntity {
        return withContext(Dispatchers.IO) {
            settingsDao.getSettings() ?: SchoolSettingsEntity().also {
                settingsDao.saveSettings(it)
            }
        }
    }

    suspend fun saveSettings(settings: SchoolSettingsEntity) {
        withContext(Dispatchers.IO) {
            settingsDao.saveSettings(settings)
        }
    }

    suspend fun insertApplicant(applicant: ApplicantEntity): Long {
        return withContext(Dispatchers.IO) {
            applicantDao.insertApplicant(applicant)
        }
    }

    suspend fun updateApplicant(applicant: ApplicantEntity) {
        withContext(Dispatchers.IO) {
            applicantDao.updateApplicant(applicant)
        }
    }

    suspend fun deleteApplicant(applicant: ApplicantEntity) {
        withContext(Dispatchers.IO) {
            applicantDao.deleteApplicant(applicant)
        }
    }

    suspend fun updateMajor(major: MajorEntity) {
        withContext(Dispatchers.IO) {
            majorDao.updateMajor(major)
        }
    }

    suspend fun logNotification(
        recipientType: String,
        recipientName: String,
        recipientNumber: String,
        reportType: String,
        status: String,
        messageContent: String
    ) {
        withContext(Dispatchers.IO) {
            logDao.insertLog(
                NotificationLogEntity(
                    timestamp = System.currentTimeMillis(),
                    recipientType = recipientType,
                    recipientName = recipientName,
                    recipientNumber = recipientNumber,
                    reportType = reportType,
                    status = status,
                    messageContent = messageContent
                )
            )
        }
    }

    suspend fun resetSampleData() {
        withContext(Dispatchers.IO) {
            AppDatabase.seedInitialData(database)
        }
    }

    /**
     * Formats the WhatsApp report according to Specification Section 6
     */
    fun generateReportMessage(
        title: String,
        schoolName: String,
        applicants: List<ApplicantEntity>,
        majors: List<MajorEntity>,
        overallTarget: Int
    ): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH.mm 'WIB'", Locale("id", "ID"))
        val now = Date()
        val todayDateStr = dateFormat.format(now)
        val todayTimeStr = timeFormat.format(now)

        val total = applicants.size
        val verified = applicants.count { it.verificationStatus == "Terverifikasi" }
        val pending = applicants.count { it.verificationStatus == "Menunggu verifikasi" }
        val accepted = applicants.count { it.admissionStatus == "Diterima" }

        // Start of today in millis
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val registeredToday = applicants.count { it.registrationDate >= startOfToday }

        val percentage = if (overallTarget > 0) {
            String.format(Locale.US, "%.1f", (total.toDouble() / overallTarget) * 100)
        } else "0"

        // Rincian per jurusan
        val majorCodes = listOf("TKJ", "TO", "TSM", "DKV", "AK")
        val majorBreakdown = majorCodes.mapIndexed { index, code ->
            val count = applicants.count { it.majorCode.equals(code, ignoreCase = true) }
            "${index + 1}. $code: $count siswa"
        }.joinToString("\n")

        val statusText = when {
            total >= overallTarget -> "🎯 TARGET PENERIMAAN TELAH TERCAPAI 100%! Kuota pendaftaran penuh."
            total >= (overallTarget * 0.75) -> "🔥 Pendaftaran sangat tinggi, mencapai lebih dari 75% dari total target."
            total >= (overallTarget * 0.5) -> "⚡ Progres pendaftaran stabil, telah mencapai lebih dari 50% target."
            else -> "📝 Pendaftaran sedang berlangsung aktif. Sosialisasi terus ditingkatkan."
        }

        return """
📊 $title
$schoolName

📅 Tanggal: $todayDateStr
🕒 Waktu laporan: $todayTimeStr

TOTAL PENDAFTAR
👥 Seluruh pendaftar: $total
✅ Terverifikasi: $verified
⏳ Menunggu verifikasi: $pending
🎓 Diterima: $accepted

RINCIAN PER JURUSAN
$majorBreakdown

📈 Pendaftar hari ini: $registeredToday
🎯 Target keseluruhan: $overallTarget
📊 Pencapaian target: $percentage%

STATUS:
$statusText

Laporan ini dibuat otomatis oleh
SPMB SMART REPORT.
        """.trimIndent()
    }

    /**
     * Formats New Applicant Notification for Admin and Principal
     */
    fun generateNewApplicantMessage(
        applicant: ApplicantEntity,
        totalApplicants: Int,
        todayCount: Int,
        forRole: String
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH.mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(applicant.registrationDate))

        return if (forRole == "ADMIN") {
            """
🔔 PENDAFTAR BARU SPMB

Nama: ${applicant.fullName}
Nomor pendaftaran: ${applicant.registrationNumber}
Pilihan jurusan: ${applicant.majorCode}
Asal sekolah: ${applicant.schoolOrigin}
Tanggal daftar: $dateStr

Status: Menunggu verifikasi.

Silakan periksa data melalui dashboard SPMB.
            """.trimIndent()
        } else {
            """
📢 INFORMASI PENDAFTARAN SPMB

Terdapat pendaftar baru di sekolah.

Total pendaftar saat ini: $totalApplicants
Jurusan pilihan: ${applicant.majorCode}
Pendaftar hari ini: $todayCount

Informasi diperbarui oleh sistem SPMB SMART REPORT.
            """.trimIndent()
        }
    }

    /**
     * Dispatches WhatsApp message:
     * If Cloud API credentials are provided, attempts Cloud API REST call.
     * Otherwise opens Android Intent with prefilled WhatsApp message.
     */
    suspend fun sendWhatsAppMessage(
        rawPhone: String,
        message: String,
        recipientType: String,
        recipientName: String,
        reportType: String,
        settings: SchoolSettingsEntity
    ): SendResult {
        // Normalize phone number to international format without + or spaces
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }

        // Check if Cloud API is configured
        if (settings.waCloudApiToken.isNotBlank() && settings.waCloudPhoneNumberId.isNotBlank()) {
            val apiResult = sendViaCloudApi(cleanPhone, message, settings)
            if (apiResult.isSuccess) {
                logNotification(
                    recipientType = recipientType,
                    recipientName = recipientName,
                    recipientNumber = cleanPhone,
                    reportType = reportType,
                    status = "BERHASIL",
                    messageContent = message
                )
                return SendResult(true, "Berhasil terkirim via WhatsApp Cloud API")
            }
        }

        // Alternative / Direct WhatsApp Intent mode
        val intentLaunched = launchWhatsAppIntent(cleanPhone, message)
        logNotification(
            recipientType = recipientType,
            recipientName = recipientName,
            recipientNumber = cleanPhone,
            reportType = reportType,
            status = if (intentLaunched) "TERKIRIM_VIA_WA" else "GAGAL",
            messageContent = message
        )

        return if (intentLaunched) {
            SendResult(true, "Membuka WhatsApp dengan pesan laporan terisi otomatis...")
        } else {
            SendResult(false, "Tidak dapat membuka aplikasi WhatsApp di perangkat ini.")
        }
    }

    private suspend fun sendViaCloudApi(
        phone: String,
        message: String,
        settings: SchoolSettingsEntity
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "https://graph.facebook.com/v19.0/${settings.waCloudPhoneNumberId}/messages"
            val json = JSONObject().apply {
                put("messaging_product", "whatsapp")
                put("recipient_type", "individual")
                put("to", phone)
                put("type", "text")
                put("text", JSONObject().apply {
                    put("preview_url", false)
                    put("body", message)
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${settings.waCloudApiToken}")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Log.e("SpmbRepository", "WA Cloud API error: ${response.code} ${response.body?.string()}")
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("SpmbRepository", "WA Cloud API Exception", e)
            Result.failure(e)
        }
    }

    fun launchWhatsAppIntent(phone: String, message: String): Boolean {
        return try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // Fallback to generic share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan SPMB").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                true
            } catch (err: Exception) {
                false
            }
        }
    }

    fun shareTextReport(title: String, message: String) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Ekspor / Bagikan $title").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Log.e("SpmbRepository", "Share error", e)
        }
    }

    fun exportToCsv(applicants: List<ApplicantEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("No,No Pendaftaran,Nama Lengkap,NISN,Jenis Kelamin,Asal Sekolah,No HP/WA,Jurusan,Tanggal Daftar,Status Verifikasi,Status Penerimaan,Catatan Admin\n")
        applicants.forEachIndexed { index, app ->
            val dateStr = dateFormat.format(Date(app.registrationDate))
            sb.append("${index + 1},\"${app.registrationNumber}\",\"${app.fullName}\",\"${app.nisn}\",\"${app.gender}\",\"${app.schoolOrigin}\",\"${app.phone}\",\"${app.majorCode}\",\"$dateStr\",\"${app.verificationStatus}\",\"${app.admissionStatus}\",\"${app.adminNotes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}

data class SendResult(
    val isSuccess: Boolean,
    val message: String
)
