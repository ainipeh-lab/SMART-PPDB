package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicantEntity
import com.example.data.model.UserRole
import com.example.ui.SpmbViewModel
import com.example.ui.components.MajorBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.getMajorFullName
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreyBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RedDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsScreen(
    viewModel: SpmbViewModel,
    modifier: Modifier = Modifier
) {
    val applicants by viewModel.filteredApplicants.collectAsState()
    val allApplicants by viewModel.applicants.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterMajor by viewModel.filterMajor.collectAsState()
    val filterVerif by viewModel.filterVerification.collectAsState()
    val filterAdm by viewModel.filterAdmission.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingApplicant by remember { mutableStateOf<ApplicantEntity?>(null) }
    var deletingApplicant by remember { mutableStateOf<ApplicantEntity?>(null) }
    var detailApplicant by remember { mutableStateOf<ApplicantEntity?>(null) }

    val isReadOnly = currentUser.role == UserRole.KEPALA_SEKOLAH

    Box(modifier = modifier.fillMaxSize().testTag("applicants_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Action Bar
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Data Calon Siswa",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Menampilkan ${applicants.size} dari ${allApplicants.size} pendaftar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.exportExcel() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSuccess,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_export_excel")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (!isReadOnly) {
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("btn_add_applicant")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Daftar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Cari nama, no pendaftaran, asal sekolah, NISN...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_applicant_input")
                )
            }

            // Filter Chips (Horizontal Scrollable)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Major Chips
                    Text("Jurusan:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SEMUA", "TKJ", "TO", "TSM", "DKV", "AK").forEach { code ->
                            val isSelected = filterMajor == code
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { viewModel.filterMajor.value = code }
                            ) {
                                Text(
                                    text = if (code == "SEMUA") "Semua Jurusan" else code,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Verification Chips
                    Text("Status Verifikasi:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SEMUA", "Menunggu verifikasi", "Terverifikasi", "Perlu perbaikan").forEach { st ->
                            val isSelected = filterVerif == st
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) NavyDark else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { viewModel.filterVerification.value = st }
                            ) {
                                Text(
                                    text = if (st == "SEMUA") "Semua Verifikasi" else st,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Applicants List
            if (applicants.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(30.dp)
                        ) {
                            Text(
                                text = "Tidak Ada Data Pendaftar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sesuaikan filter pencarian atau tambah pendaftar baru.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(applicants, key = { it.id }) { applicant ->
                    ApplicantCard(
                        applicant = applicant,
                        isReadOnly = isReadOnly,
                        onDetail = { detailApplicant = applicant },
                        onEdit = { editingApplicant = applicant },
                        onDelete = { deletingApplicant = applicant },
                        onWhatsAppChat = {
                            val msg = "Halo ${applicant.fullName}, kami dari Panitia SPMB ${viewModel.settings.value.schoolName}. Mengenai pendaftaran nomor ${applicant.registrationNumber}..."
                            viewModel.repository.launchWhatsAppIntent(applicant.phone, msg)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialog Tambah Pendaftar
    if (showAddDialog) {
        ApplicantDialog(
            onDismiss = { showAddDialog = false },
            onSave = { fullName, nisn, gender, schoolOrigin, phone, majorCode, verificationStatus, admissionStatus, adminNotes, notifyWA ->
                viewModel.addApplicant(
                    fullName = fullName,
                    nisn = nisn,
                    gender = gender,
                    schoolOrigin = schoolOrigin,
                    phone = phone,
                    majorCode = majorCode,
                    verificationStatus = verificationStatus,
                    admissionStatus = admissionStatus,
                    adminNotes = adminNotes,
                    sendWhatsAppNotification = notifyWA
                )
                showAddDialog = false
            }
        )
    }

    // Dialog Edit Pendaftar
    editingApplicant?.let { app ->
        ApplicantDialog(
            applicant = app,
            onDismiss = { editingApplicant = null },
            onSave = { fullName, nisn, gender, schoolOrigin, phone, majorCode, verificationStatus, admissionStatus, adminNotes, _ ->
                val updated = app.copy(
                    fullName = fullName,
                    nisn = nisn,
                    gender = gender,
                    schoolOrigin = schoolOrigin,
                    phone = phone,
                    majorCode = majorCode,
                    verificationStatus = verificationStatus,
                    admissionStatus = admissionStatus,
                    adminNotes = adminNotes
                )
                viewModel.updateApplicant(updated)
                editingApplicant = null
            }
        )
    }

    // Dialog Konfirmasi Hapus
    deletingApplicant?.let { app ->
        AlertDialog(
            onDismissRequest = { deletingApplicant = null },
            title = { Text("Hapus Data Pendaftar", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus data calon siswa ${app.fullName} (${app.registrationNumber})? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteApplicant(app)
                        deletingApplicant = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingApplicant = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Detail Pendaftar
    detailApplicant?.let { app ->
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(app.registrationDate))

        AlertDialog(
            onDismissRequest = { detailApplicant = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = app.registrationNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    MajorBadge(majorCode = app.majorCode)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = app.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Jurusan: ${getMajorFullName(app.majorCode)}", fontSize = 13.sp, color = BluePrimary, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                        Text(text = "NISN: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = if (app.nisn.isNotBlank()) app.nisn else "-", fontSize = 12.sp)
                    }
                    Row {
                        Text(text = "Jenis Kelamin: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = app.gender, fontSize = 12.sp)
                    }
                    Row {
                        Text(text = "Asal Sekolah: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = app.schoolOrigin, fontSize = 12.sp)
                    }
                    Row {
                        Text(text = "No WhatsApp: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = app.phone, fontSize = 12.sp)
                    }
                    Row {
                        Text(text = "Tanggal Daftar: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = dateStr, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(status = app.verificationStatus)
                        StatusBadge(status = app.admissionStatus)
                    }

                    if (app.adminNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Catatan Admin:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = app.adminNotes,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = "Halo ${app.fullName}, panitia SPMB menginformasikan status pendaftaran Anda: ${app.verificationStatus} & ${app.admissionStatus}."
                        viewModel.repository.launchWhatsAppIntent(app.phone, msg)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat WhatsApp Siswa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { detailApplicant = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun ApplicantCard(
    applicant: ApplicantEntity,
    isReadOnly: Boolean,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(applicant.registrationDate))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDetail() }
            .testTag("applicant_item_${applicant.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row Top: Reg number & Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = applicant.registrationNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $dateStr",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MajorBadge(majorCode = applicant.majorCode)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Nama Siswa
            Text(
                text = applicant.fullName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Info Sekolah & Kontak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${applicant.schoolOrigin} • ${applicant.gender}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badges & Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(status = applicant.verificationStatus)
                    StatusBadge(status = applicant.admissionStatus)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Chat WA
                    IconButton(
                        onClick = onWhatsAppChat,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Hubungi WA",
                            tint = GreenSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isReadOnly) {
                        // Edit
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = BluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Hapus
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = RedDanger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
