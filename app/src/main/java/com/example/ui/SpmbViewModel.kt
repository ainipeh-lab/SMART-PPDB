package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApplicantEntity
import com.example.data.model.MajorEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SchoolSettingsEntity
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.SpmbRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class SpmbViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SpmbRepository(application)

    // Current navigation tab: 0=Dashboard, 1=Pendaftar, 2=Laporan, 3=WhatsApp, 4=Pengaturan
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Role session management
    val availableUsers = listOf(
        UserSession("1", "Drs. H. Ahmad Fauzi, M.Pd", UserRole.KEPALA_SEKOLAH),
        UserSession("2", "Budi Santoso, S.Kom", UserRole.ADMIN_SPMB),
        UserSession("3", "Admin Utama Sistem", UserRole.SUPER_ADMIN)
    )

    private val _currentUser = MutableStateFlow(availableUsers[2]) // Default to SUPER_ADMIN so all controls are visible
    val currentUser: StateFlow<UserSession> = _currentUser.asStateFlow()

    fun switchUser(user: UserSession) {
        _currentUser.value = user
    }

    // Feedback message channel
    private val _feedbackMessage = MutableSharedFlow<String>()
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _feedbackMessage.emit(msg)
        }
    }

    // Repository data flows
    val applicants: StateFlow<List<ApplicantEntity>> = repository.applicants.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val majors: StateFlow<List<MajorEntity>> = repository.majors.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val settings: StateFlow<SchoolSettingsEntity> = repository.settings.combine(
        MutableStateFlow(SchoolSettingsEntity())
    ) { fromDb, default ->
        fromDb ?: default
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SchoolSettingsEntity()
    )

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = repository.notificationLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Data Pendaftar Search & Filters
    val searchQuery = MutableStateFlow("")
    val filterMajor = MutableStateFlow("SEMUA")
    val filterVerification = MutableStateFlow("SEMUA")
    val filterAdmission = MutableStateFlow("SEMUA")

    // Filtered applicants
    val filteredApplicants: StateFlow<List<ApplicantEntity>> = combine(
        applicants,
        searchQuery,
        filterMajor,
        filterVerification,
        filterAdmission
    ) { list, query, major, verification, admission ->
        list.filter { app ->
            val matchesQuery = query.isBlank() ||
                app.fullName.contains(query, ignoreCase = true) ||
                app.registrationNumber.contains(query, ignoreCase = true) ||
                app.schoolOrigin.contains(query, ignoreCase = true) ||
                app.nisn.contains(query, ignoreCase = true) ||
                app.phone.contains(query, ignoreCase = true)

            val matchesMajor = major == "SEMUA" || app.majorCode.equals(major, ignoreCase = true)
            val matchesVerif = verification == "SEMUA" || app.verificationStatus == verification
            val matchesAdm = admission == "SEMUA" || app.admissionStatus == admission

            matchesQuery && matchesMajor && matchesVerif && matchesAdm
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Report date filter: "HARI_INI", "KEMARIN", "MINGGU_INI", "BULAN_INI", "SEMUA"
    val reportPeriod = MutableStateFlow("SEMUA")
    val reportMajorFilter = MutableStateFlow("SEMUA")
    val reportStatusFilter = MutableStateFlow("SEMUA")

    val reportFilteredApplicants: StateFlow<List<ApplicantEntity>> = combine(
        applicants,
        reportPeriod,
        reportMajorFilter,
        reportStatusFilter
    ) { list, period, major, status ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Start of today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        // Start of yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = cal.timeInMillis
        val endOfYesterday = startOfToday - 1

        // Start of this week
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfWeek = cal.timeInMillis

        // Start of this month
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfMonth = cal.timeInMillis

        list.filter { app ->
            val matchesPeriod = when (period) {
                "HARI_INI" -> app.registrationDate >= startOfToday
                "KEMARIN" -> app.registrationDate in startOfYesterday..endOfYesterday
                "MINGGU_INI" -> app.registrationDate >= startOfWeek
                "BULAN_INI" -> app.registrationDate >= startOfMonth
                else -> true
            }
            val matchesMajor = major == "SEMUA" || app.majorCode.equals(major, ignoreCase = true)
            val matchesStatus = when (status) {
                "SEMUA" -> true
                "TERVERIFIKASI" -> app.verificationStatus == "Terverifikasi"
                "MENUNGGU_VERIFIKASI" -> app.verificationStatus == "Menunggu verifikasi"
                "PERLU_PERBAIKAN" -> app.verificationStatus == "Perlu perbaikan"
                "DITERIMA" -> app.admissionStatus == "Diterima"
                "TIDAK_DITERIMA" -> app.admissionStatus == "Tidak diterima"
                else -> true
            }

            matchesPeriod && matchesMajor && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CRUD operations
    fun addApplicant(
        fullName: String,
        nisn: String,
        gender: String,
        schoolOrigin: String,
        phone: String,
        majorCode: String,
        verificationStatus: String,
        admissionStatus: String,
        adminNotes: String,
        sendWhatsAppNotification: Boolean
    ) {
        viewModelScope.launch {
            val count = applicants.value.size + 1
            val regNumber = "SPMB-2027-${String.format("%03d", count)}"
            val newApp = ApplicantEntity(
                registrationNumber = regNumber,
                fullName = fullName.trim(),
                nisn = nisn.trim(),
                gender = gender,
                schoolOrigin = schoolOrigin.trim(),
                phone = phone.trim(),
                majorCode = majorCode,
                registrationDate = System.currentTimeMillis(),
                verificationStatus = verificationStatus,
                admissionStatus = admissionStatus,
                adminNotes = adminNotes.trim()
            )
            repository.insertApplicant(newApp)
            showMessage("Pendaftar $fullName berhasil didaftarkan ($regNumber)")

            if (sendWhatsAppNotification) {
                val currentSettings = settings.value
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }
                val todayCount = applicants.value.count { it.registrationDate >= cal.timeInMillis } + 1

                // Send to Admin
                if (currentSettings.notifyAdminOnNewApplicant && currentSettings.adminWhatsApp.isNotBlank()) {
                    val adminMsg = repository.generateNewApplicantMessage(
                        newApp,
                        applicants.value.size + 1,
                        todayCount,
                        "ADMIN"
                    )
                    repository.sendWhatsAppMessage(
                        rawPhone = currentSettings.adminWhatsApp,
                        message = adminMsg,
                        recipientType = "ADMIN_SPMB",
                        recipientName = currentSettings.adminName,
                        reportType = "PENDAFTAR_BARU",
                        settings = currentSettings
                    )
                }
                // Send to Principal if configured
                if (currentSettings.notifyPrincipalOnNewApplicant && currentSettings.principalWhatsApp.isNotBlank()) {
                    val princMsg = repository.generateNewApplicantMessage(
                        newApp,
                        applicants.value.size + 1,
                        todayCount,
                        "KEPALA_SEKOLAH"
                    )
                    repository.sendWhatsAppMessage(
                        rawPhone = currentSettings.principalWhatsApp,
                        message = princMsg,
                        recipientType = "KEPALA_SEKOLAH",
                        recipientName = currentSettings.principalName,
                        reportType = "PENDAFTAR_BARU",
                        settings = currentSettings
                    )
                }
            }
        }
    }

    fun updateApplicant(applicant: ApplicantEntity) {
        viewModelScope.launch {
            repository.updateApplicant(applicant)
            showMessage("Data pendaftar ${applicant.fullName} berhasil diperbarui")
        }
    }

    fun deleteApplicant(applicant: ApplicantEntity) {
        viewModelScope.launch {
            repository.deleteApplicant(applicant)
            showMessage("Pendaftar ${applicant.fullName} telah dihapus")
        }
    }

    fun saveSchoolSettings(newSettings: SchoolSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            showMessage("Pengaturan sekolah dan WhatsApp berhasil disimpan")
        }
    }

    fun updateMajorQuota(code: String, quota: Int) {
        viewModelScope.launch {
            val major = majors.value.find { it.code == code }
            if (major != null) {
                repository.updateMajor(major.copy(quota = quota))
                showMessage("Target kuota jurusan $code diubah menjadi $quota siswa")
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetSampleData()
            showMessage("Data contoh SPMB berhasil dimuat ulang")
        }
    }

    // WhatsApp Dispatch Actions
    fun sendReport(
        recipientTarget: String, // "KEPALA_SEKOLAH", "ADMIN_SPMB", "KEDUANYA"
        reportType: String // "HARIAN", "MINGGUAN", "BULANAN"
    ) {
        viewModelScope.launch {
            val currSettings = settings.value
            val currentApplicants = applicants.value
            val currentMajors = majors.value

            val title = when (reportType) {
                "HARIAN" -> "LAPORAN HARIAN SPMB"
                "MINGGUAN" -> "LAPORAN MINGGUAN SPMB"
                "BULANAN" -> "LAPORAN BULANAN SPMB"
                else -> "LAPORAN SPMB"
            }

            val message = repository.generateReportMessage(
                title = title,
                schoolName = currSettings.schoolName,
                applicants = currentApplicants,
                majors = currentMajors,
                overallTarget = currSettings.overallTarget
            )

            when (recipientTarget) {
                "KEPALA_SEKOLAH" -> {
                    val res = repository.sendWhatsAppMessage(
                        rawPhone = currSettings.principalWhatsApp,
                        message = message,
                        recipientType = "KEPALA_SEKOLAH",
                        recipientName = currSettings.principalName,
                        reportType = reportType,
                        settings = currSettings
                    )
                    showMessage(res.message)
                }
                "ADMIN_SPMB" -> {
                    val res = repository.sendWhatsAppMessage(
                        rawPhone = currSettings.adminWhatsApp,
                        message = message,
                        recipientType = "ADMIN_SPMB",
                        recipientName = currSettings.adminName,
                        reportType = reportType,
                        settings = currSettings
                    )
                    showMessage(res.message)
                }
                "KEDUANYA" -> {
                    // Send to Principal
                    repository.sendWhatsAppMessage(
                        rawPhone = currSettings.principalWhatsApp,
                        message = message,
                        recipientType = "KEPALA_SEKOLAH",
                        recipientName = currSettings.principalName,
                        reportType = reportType,
                        settings = currSettings
                    )
                    // Then to Admin
                    val res = repository.sendWhatsAppMessage(
                        rawPhone = currSettings.adminWhatsApp,
                        message = message,
                        recipientType = "ADMIN_SPMB",
                        recipientName = currSettings.adminName,
                        reportType = reportType,
                        settings = currSettings
                    )
                    showMessage("Laporan dikirim ke Kepala Sekolah dan Admin. " + res.message)
                }
            }
        }
    }

    fun testWhatsAppConnection() {
        viewModelScope.launch {
            val currSettings = settings.value
            val testMsg = """
🧪 TES KONEKSI WHATSAPP
SPMB SMART REPORT
${currSettings.schoolName}

Status: Sistem siap mengirim laporan otomatis.
Waktu Tes: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date())}

Notifikasi ini menandakan integrasi WhatsApp berfungsi dengan baik.
            """.trimIndent()

            val res = repository.sendWhatsAppMessage(
                rawPhone = currSettings.adminWhatsApp,
                message = testMsg,
                recipientType = "ADMIN_SPMB",
                recipientName = currSettings.adminName,
                reportType = "TEST",
                settings = currSettings
            )
            showMessage("Hasil Tes: ${res.message}")
        }
    }

    fun exportExcel() {
        val list = filteredApplicants.value.ifEmpty { applicants.value }
        val csv = repository.exportToCsv(list)
        repository.shareTextReport("Data_Pendaftar_SPMB_${settings.value.academicYear.replace("/", "_")}.csv", csv)
        showMessage("Mengekspor ${list.size} data pendaftar ke Excel (CSV)")
    }

    fun exportPdf() {
        val currSettings = settings.value
        val list = reportFilteredApplicants.value.ifEmpty { applicants.value }
        val report = repository.generateReportMessage(
            title = "LAPORAN RESMI SPMB",
            schoolName = currSettings.schoolName,
            applicants = list,
            majors = majors.value,
            overallTarget = currSettings.overallTarget
        )
        repository.shareTextReport("Laporan_Resmi_SPMB_${currSettings.academicYear.replace("/", "_")}.txt", report)
        showMessage("Mencetak / membagikan laporan dokumen SPMB")
    }
}
