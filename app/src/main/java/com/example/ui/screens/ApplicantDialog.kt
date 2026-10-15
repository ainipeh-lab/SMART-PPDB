package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicantEntity
import com.example.ui.components.getMajorFullName
import com.example.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantDialog(
    applicant: ApplicantEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        nisn: String,
        gender: String,
        schoolOrigin: String,
        phone: String,
        majorCode: String,
        verificationStatus: String,
        admissionStatus: String,
        adminNotes: String,
        notifyWhatsApp: Boolean
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(applicant?.fullName ?: "") }
    var nisn by remember { mutableStateOf(applicant?.nisn ?: "") }
    var gender by remember { mutableStateOf(applicant?.gender ?: "Laki-laki") }
    var schoolOrigin by remember { mutableStateOf(applicant?.schoolOrigin ?: "") }
    var phone by remember { mutableStateOf(applicant?.phone ?: "") }
    var majorCode by remember { mutableStateOf(applicant?.majorCode ?: "TKJ") }
    var verificationStatus by remember { mutableStateOf(applicant?.verificationStatus ?: "Menunggu verifikasi") }
    var admissionStatus by remember { mutableStateOf(applicant?.admissionStatus ?: "Menunggu keputusan") }
    var adminNotes by remember { mutableStateOf(applicant?.adminNotes ?: "") }
    var notifyWhatsApp by remember { mutableStateOf(true) }

    var majorExpanded by remember { mutableStateOf(false) }
    var verifExpanded by remember { mutableStateOf(false) }
    var admissionExpanded by remember { mutableStateOf(false) }

    val majorOptions = listOf("TKJ", "TO", "TSM", "DKV", "AK")
    val verifOptions = listOf("Menunggu verifikasi", "Terverifikasi", "Perlu perbaikan")
    val admissionOptions = listOf("Menunggu keputusan", "Diterima", "Tidak diterima")

    var nameError by remember { mutableStateOf(false) }
    var schoolError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (applicant == null) "Tambah Pendaftar Baru" else "Edit Data Pendaftar",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nama Lengkap
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nama Lengkap Siswa *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Nama wajib diisi") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_fullname")
                )

                // NISN
                OutlinedTextField(
                    value = nisn,
                    onValueChange = { nisn = it },
                    label = { Text("NISN (Nomor Induk Siswa Nasional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_nisn")
                )

                // Jenis Kelamin
                Text("Jenis Kelamin *", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { gender = "Laki-laki" }
                    ) {
                        RadioButton(
                            selected = gender == "Laki-laki",
                            onClick = { gender = "Laki-laki" }
                        )
                        Text("Laki-laki", fontSize = 13.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { gender = "Perempuan" }
                    ) {
                        RadioButton(
                            selected = gender == "Perempuan",
                            onClick = { gender = "Perempuan" }
                        )
                        Text("Perempuan", fontSize = 13.sp)
                    }
                }

                // Asal Sekolah
                OutlinedTextField(
                    value = schoolOrigin,
                    onValueChange = {
                        schoolOrigin = it
                        schoolError = it.isBlank()
                    },
                    label = { Text("Asal Sekolah (SMP/MTs) *") },
                    isError = schoolError,
                    supportingText = if (schoolError) { { Text("Asal sekolah wajib diisi") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_school")
                )

                // Nomor Telepon / WA
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = it.isBlank()
                    },
                    label = { Text("Nomor WhatsApp/HP Siswa/Wali *") },
                    placeholder = { Text("08xxxxxxxxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    supportingText = if (phoneError) { { Text("No WhatsApp wajib diisi") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_phone")
                )

                // Jurusan Dropdown
                ExposedDropdownMenuBox(
                    expanded = majorExpanded,
                    onExpandedChange = { majorExpanded = !majorExpanded }
                ) {
                    OutlinedTextField(
                        value = "$majorCode - ${getMajorFullName(majorCode)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilihan Jurusan *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = majorExpanded,
                        onDismissRequest = { majorExpanded = false }
                    ) {
                        majorOptions.forEach { code ->
                            DropdownMenuItem(
                                text = { Text("$code - ${getMajorFullName(code)}") },
                                onClick = {
                                    majorCode = code
                                    majorExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Verifikasi Dropdown
                ExposedDropdownMenuBox(
                    expanded = verifExpanded,
                    onExpandedChange = { verifExpanded = !verifExpanded }
                ) {
                    OutlinedTextField(
                        value = verificationStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Verifikasi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = verifExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = verifExpanded,
                        onDismissRequest = { verifExpanded = false }
                    ) {
                        verifOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    verificationStatus = opt
                                    verifExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Penerimaan Dropdown
                ExposedDropdownMenuBox(
                    expanded = admissionExpanded,
                    onExpandedChange = { admissionExpanded = !admissionExpanded }
                ) {
                    OutlinedTextField(
                        value = admissionStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Penerimaan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = admissionExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = admissionExpanded,
                        onDismissRequest = { admissionExpanded = false }
                    ) {
                        admissionOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    admissionStatus = opt
                                    admissionExpanded = false
                                }
                            )
                        }
                    }
                }

                // Catatan Admin
                OutlinedTextField(
                    value = adminNotes,
                    onValueChange = { adminNotes = it },
                    label = { Text("Catatan Admin") },
                    placeholder = { Text("Contoh: Berkas asli belum diserahkan") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_notes")
                )

                // Checkbox notification
                if (applicant == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyWhatsApp = !notifyWhatsApp }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = notifyWhatsApp,
                            onCheckedChange = { notifyWhatsApp = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kirim notifikasi pendaftar baru via WhatsApp ke Admin & Kepala Sekolah",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (schoolOrigin.isBlank()) {
                        schoolError = true
                        return@Button
                    }
                    if (phone.isBlank()) {
                        phoneError = true
                        return@Button
                    }
                    onSave(
                        fullName,
                        nisn,
                        gender,
                        schoolOrigin,
                        phone,
                        majorCode,
                        verificationStatus,
                        admissionStatus,
                        adminNotes,
                        notifyWhatsApp
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.testTag("btn_save_applicant")
            ) {
                Text("Simpan Data")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Batal")
            }
        }
    )
}
