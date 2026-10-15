package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ApplicantEntity
import com.example.data.model.MajorEntity
import com.example.data.model.NotificationLogEntity
import com.example.data.model.SchoolSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ApplicantEntity::class,
        MajorEntity::class,
        SchoolSettingsEntity::class,
        NotificationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicantDao(): ApplicantDao
    abstract fun majorDao(): MajorDao
    abstract fun schoolSettingsDao(): SchoolSettingsDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spmb_smart_report.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val settingsDao = database.schoolSettingsDao()
            val majorDao = database.majorDao()
            val applicantDao = database.applicantDao()
            val logDao = database.notificationLogDao()

            if (settingsDao.getSettings() == null) {
                settingsDao.saveSettings(
                    SchoolSettingsEntity(
                        id = 1,
                        schoolName = "SMK Negeri 1 Teknologi Unggul",
                        academicYear = "2027/2028",
                        overallTarget = 500,
                        principalName = "Drs. H. Ahmad Fauzi, M.Pd",
                        principalWhatsApp = "6281234567890",
                        adminName = "Budi Santoso, S.Kom",
                        adminWhatsApp = "6281987654321",
                        dailyReportTime = "16:00",
                        notifyPrincipalOnNewApplicant = false,
                        notifyAdminOnNewApplicant = true
                    )
                )
            }

            val defaultMajors = listOf(
                MajorEntity("TKJ", "Teknik Komputer dan Jaringan", 120, "Fokus ke jaringan komputer, mikrotik, cloud, dan server."),
                MajorEntity("TO", "Teknik Otomotif", 100, "Pemeliharaan mesin kendaraan ringan, sistem EFI, dan transmisi."),
                MajorEntity("TSM", "Teknik Sepeda Motor", 100, "Service mesin motor, kelistrikan roda dua, dan injeksi."),
                MajorEntity("DKV", "Desain Komunikasi Visual", 100, "Desain grafis, animasi 2D/3D, video editing, dan fotografi."),
                MajorEntity("AK", "Akuntansi", 80, "Pembukuan digital, perpajakan, audit dasar, dan spreadsheet bisnis.")
            )
            majorDao.insertAll(defaultMajors)

            val now = System.currentTimeMillis()
            val day = 86400000L

            val sampleApplicants = listOf(
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-001",
                    fullName = "Muhammad Rizky Pratama",
                    nisn = "0081234501",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP Negeri 1 Kota",
                    phone = "081234567801",
                    majorCode = "TKJ",
                    registrationDate = now - (6 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima",
                    adminNotes = "Berkas lengkap, nilai rapor matematika 92"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-002",
                    fullName = "Siti Nurhaliza Putri",
                    nisn = "0081234502",
                    gender = "Perempuan",
                    schoolOrigin = "SMP Negeri 3 Kota",
                    phone = "081234567802",
                    majorCode = "DKV",
                    registrationDate = now - (5 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima",
                    adminNotes = "Portofolio karya desain poster memuaskan"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-003",
                    fullName = "Dimas Bagus Saputra",
                    nisn = "0081234503",
                    gender = "Laki-laki",
                    schoolOrigin = "MTs Negeri 1",
                    phone = "081234567803",
                    majorCode = "TO",
                    registrationDate = now - (4 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Menunggu hasil tes fisik"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-004",
                    fullName = "Anisa Rahmawati",
                    nisn = "0081234504",
                    gender = "Perempuan",
                    schoolOrigin = "SMP Bunga Bangsa",
                    phone = "081234567804",
                    majorCode = "AK",
                    registrationDate = now - (3 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima",
                    adminNotes = "Juara 3 Olimpiade IPS tingkat kota"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-005",
                    fullName = "Rendy Firmansyah",
                    nisn = "0081234505",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP Negeri 2 Kota",
                    phone = "081234567805",
                    majorCode = "TSM",
                    registrationDate = now - (2 * day),
                    verificationStatus = "Menunggu verifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Ijazah SKL asli belum diunggah"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-006",
                    fullName = "Fajar Adi Nugroho",
                    nisn = "0081234506",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP IT Al-Hikmah",
                    phone = "081234567806",
                    majorCode = "TKJ",
                    registrationDate = now - (1 * day),
                    verificationStatus = "Menunggu verifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Pendaftar baru, verifikasi dijadwalkan besok"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-007",
                    fullName = "Zaskia Aurelia",
                    nisn = "0081234507",
                    gender = "Perempuan",
                    schoolOrigin = "SMP Negeri 4 Kota",
                    phone = "081234567807",
                    majorCode = "DKV",
                    registrationDate = now - (1 * day),
                    verificationStatus = "Perlu perbaikan",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Foto KK kabur, hubungi wali via WA"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-008",
                    fullName = "Kevin Maulana",
                    nisn = "0081234508",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP Nusantara",
                    phone = "081234567808",
                    majorCode = "TO",
                    registrationDate = now - (3 * 3600000L), // hari ini
                    verificationStatus = "Menunggu verifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Pendaftar hari ini via form online"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-009",
                    fullName = "Putri Ayu Wandira",
                    nisn = "0081234509",
                    gender = "Perempuan",
                    schoolOrigin = "SMP Negeri 1 Kota",
                    phone = "081234567809",
                    majorCode = "AK",
                    registrationDate = now - (1 * 3600000L), // hari ini
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Berkas terverifikasi lengkap"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-010",
                    fullName = "Bagas Arya Yudha",
                    nisn = "0081234510",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP Kartika",
                    phone = "081234567810",
                    majorCode = "TSM",
                    registrationDate = now - (30 * 60000L), // hari ini
                    verificationStatus = "Menunggu verifikasi",
                    admissionStatus = "Menunggu keputusan",
                    adminNotes = "Pendaftar terbaru"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-011",
                    fullName = "Dewi Sekar Larasati",
                    nisn = "0081234511",
                    gender = "Perempuan",
                    schoolOrigin = "SMP Negeri 5 Kota",
                    phone = "081234567811",
                    majorCode = "TKJ",
                    registrationDate = now - (4 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima",
                    adminNotes = "Siswa berprestasi jalur akademik"
                ),
                ApplicantEntity(
                    registrationNumber = "SPMB-2027-012",
                    fullName = "Arif Hidayat",
                    nisn = "0081234512",
                    gender = "Laki-laki",
                    schoolOrigin = "SMP Darul Ulum",
                    phone = "081234567812",
                    majorCode = "TO",
                    registrationDate = now - (5 * day),
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima",
                    adminNotes = "Nilai tes kompetensi dasar A"
                )
            )
            applicantDao.insertAll(sampleApplicants)

            logDao.insertLog(
                NotificationLogEntity(
                    timestamp = now - (24 * 3600000L),
                    recipientType = "KEPALA_SEKOLAH",
                    recipientName = "Drs. H. Ahmad Fauzi, M.Pd",
                    recipientNumber = "6281234567890",
                    reportType = "HARIAN",
                    status = "BERHASIL",
                    messageContent = "Laporan Harian SPMB kemarin berhasil dikirim ke Kepala Sekolah."
                )
            )
        }
    }
}
