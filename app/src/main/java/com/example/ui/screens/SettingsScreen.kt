package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolSettingsEntity
import com.example.data.model.UserRole
import com.example.ui.SpmbViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeWarning

@Composable
fun SettingsScreen(
    viewModel: SpmbViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val majors by viewModel.majors.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var schoolName by remember(settings) { mutableStateOf(settings.schoolName) }
    var academicYear by remember(settings) { mutableStateOf(settings.academicYear) }
    var overallTarget by remember(settings) { mutableStateOf(settings.overallTarget.toString()) }
    var principalName by remember(settings) { mutableStateOf(settings.principalName) }
    var principalWhatsApp by remember(settings) { mutableStateOf(settings.principalWhatsApp) }
    var adminName by remember(settings) { mutableStateOf(settings.adminName) }
    var adminWhatsApp by remember(settings) { mutableStateOf(settings.adminWhatsApp) }
    var dailyReportTime by remember(settings) { mutableStateOf(settings.dailyReportTime) }
    var notifyPrincipal by remember(settings) { mutableStateOf(settings.notifyPrincipalOnNewApplicant) }
    var notifyAdmin by remember(settings) { mutableStateOf(settings.notifyAdminOnNewApplicant) }
    var cloudApiToken by remember(settings) { mutableStateOf(settings.waCloudApiToken) }
    var cloudPhoneId by remember(settings) { mutableStateOf(settings.waCloudPhoneNumberId) }

    // Quotas state per major
    var tkjQuota by remember(majors) { mutableStateOf((majors.find { it.code == "TKJ" }?.quota ?: 120).toString()) }
    var toQuota by remember(majors) { mutableStateOf((majors.find { it.code == "TO" }?.quota ?: 100).toString()) }
    var tsmQuota by remember(majors) { mutableStateOf((majors.find { it.code == "TSM" }?.quota ?: 100).toString()) }
    var dkvQuota by remember(majors) { mutableStateOf((majors.find { it.code == "DKV" }?.quota ?: 100).toString()) }
    var akQuota by remember(majors) { mutableStateOf((majors.find { it.code == "AK" }?.quota ?: 80).toString()) }

    val canEditAll = currentUser.role == UserRole.SUPER_ADMIN

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Pengaturan Sistem & Hak Akses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Konfigurasi identitas sekolah, target jurusan, penerima WhatsApp, dan akun",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Akun & Hak Akses Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Akun & Hak Akses Pengguna",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (currentUser.role) {
                                UserRole.SUPER_ADMIN -> Color(0xFFDC2626).copy(alpha = 0.15f)
                                UserRole.ADMIN_SPMB -> OrangeWarning.copy(alpha = 0.15f)
                                UserRole.KEPALA_SEKOLAH -> BluePrimary.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = currentUser.role.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentUser.role) {
                                    UserRole.SUPER_ADMIN -> Color(0xFFDC2626)
                                    UserRole.ADMIN_SPMB -> OrangeWarning
                                    UserRole.KEPALA_SEKOLAH -> BluePrimary
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pengguna aktif: ${currentUser.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser.role.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ganti Peran Pengguna (Quick Role Switcher):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.availableUsers.forEach { user ->
                            val isSelected = currentUser.id == user.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NavyDark else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.switchUser(user)
                                        viewModel.showMessage("Beralih ke akun ${user.role.displayName}")
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = user.role.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Identitas Sekolah & Target Keseluruhan
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = NavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identitas Sekolah & Target",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nama Sekolah") },
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = academicYear,
                            onValueChange = { academicYear = it },
                            label = { Text("Tahun Ajaran") },
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = overallTarget,
                            onValueChange = { overallTarget = it },
                            label = { Text("Target Keseluruhan") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Target Kuota Per Jurusan (5 Jurusan)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target Kuota Siswa per Jurusan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tkjQuota,
                            onValueChange = { tkjQuota = it },
                            label = { Text("1. TKJ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = toQuota,
                            onValueChange = { toQuota = it },
                            label = { Text("2. TO") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tsmQuota,
                            onValueChange = { tsmQuota = it },
                            label = { Text("3. TSM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = dkvQuota,
                            onValueChange = { dkvQuota = it },
                            label = { Text("4. DKV") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = akQuota,
                            onValueChange = { akQuota = it },
                            label = { Text("5. AK") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = canEditAll,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // WhatsApp Recipients & Auto Schedule
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kontak WhatsApp Kepala Sekolah & Admin",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Kepala Sekolah
                    OutlinedTextField(
                        value = principalName,
                        onValueChange = { principalName = it },
                        label = { Text("Nama Kepala Sekolah") },
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = principalWhatsApp,
                        onValueChange = { principalWhatsApp = it },
                        label = { Text("No. WhatsApp Kepala Sekolah (628xxxx)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Admin SPMB
                    OutlinedTextField(
                        value = adminName,
                        onValueChange = { adminName = it },
                        label = { Text("Nama Admin SPMB") },
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adminWhatsApp,
                        onValueChange = { adminWhatsApp = it },
                        label = { Text("No. WhatsApp Admin SPMB (628xxxx)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pengaturan Otomatisasi Jadwal Laporan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Jadwal info
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Laporan Harian: Setiap hari pukul 16.00 WIB", fontSize = 11.sp)
                            Text("• Laporan Mingguan: Setiap hari Sabtu pukul 16.00 WIB", fontSize = 11.sp)
                            Text("• Laporan Bulanan: Hari terakhir setiap bulan pukul 16.00 WIB", fontSize = 11.sp)
                        }
                    }

                    // Toggle notifications
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifikasi Pendaftar Baru ke Admin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Kirim WA setiap ada siswa mendaftar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyAdmin,
                            onCheckedChange = { if (canEditAll) notifyAdmin = it },
                            enabled = canEditAll
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifikasi Pendaftar Baru ke Kepala Sekolah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Kirim WA seketika (opsional)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyPrincipal,
                            onCheckedChange = { if (canEditAll) notifyPrincipal = it },
                            enabled = canEditAll
                        )
                    }
                }
            }
        }

        // WhatsApp Business Cloud API (Opsional)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Konfigurasi WhatsApp Business Cloud API (Opsional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Jika diisi, pengiriman laporan akan memanggil WhatsApp Business Cloud API Meta. Jika dikosongkan, sistem memakai deep-link resmi WhatsApp dengan pesan terisi otomatis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = cloudPhoneId,
                        onValueChange = { cloudPhoneId = it },
                        label = { Text("Phone Number ID") },
                        placeholder = { Text("Contoh: 106543210987654") },
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cloudApiToken,
                        onValueChange = { cloudApiToken = it },
                        label = { Text("System User Access Token") },
                        placeholder = { Text("EAABw...") },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = canEditAll,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Tombol Simpan
        item {
            if (canEditAll) {
                Button(
                    onClick = {
                        val newSettings = settings.copy(
                            schoolName = schoolName.trim(),
                            academicYear = academicYear.trim(),
                            overallTarget = overallTarget.toIntOrNull() ?: 500,
                            principalName = principalName.trim(),
                            principalWhatsApp = principalWhatsApp.trim(),
                            adminName = adminName.trim(),
                            adminWhatsApp = adminWhatsApp.trim(),
                            dailyReportTime = dailyReportTime.trim(),
                            notifyPrincipalOnNewApplicant = notifyPrincipal,
                            notifyAdminOnNewApplicant = notifyAdmin,
                            waCloudApiToken = cloudApiToken.trim(),
                            waCloudPhoneNumberId = cloudPhoneId.trim()
                        )
                        viewModel.saveSchoolSettings(newSettings)

                        // Update major quotas
                        viewModel.updateMajorQuota("TKJ", tkjQuota.toIntOrNull() ?: 120)
                        viewModel.updateMajorQuota("TO", toQuota.toIntOrNull() ?: 100)
                        viewModel.updateMajorQuota("TSM", tsmQuota.toIntOrNull() ?: 100)
                        viewModel.updateMajorQuota("DKV", dkvQuota.toIntOrNull() ?: 100)
                        viewModel.updateMajorQuota("AK", akQuota.toIntOrNull() ?: 80)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_settings")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Semua Pengaturan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Demo / Sample Data Reset
        item {
            OutlinedButton(
                onClick = { viewModel.resetDemoData() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_reset_demo_data")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = OrangeWarning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Muat Ulang Data Contoh / Demo SPMB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
