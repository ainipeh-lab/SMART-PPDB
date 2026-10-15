package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpmbViewModel
import com.example.ui.components.MajorDistributionChart
import com.example.ui.components.StatCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeWarning

@Composable
fun ReportsScreen(
    viewModel: SpmbViewModel,
    onNavigateToWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val period by viewModel.reportPeriod.collectAsState()
    val majorFilter by viewModel.reportMajorFilter.collectAsState()
    val statusFilter by viewModel.reportStatusFilter.collectAsState()
    val filteredList by viewModel.reportFilteredApplicants.collectAsState()
    val majors by viewModel.majors.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val total = filteredList.size
    val verified = filteredList.count { it.verificationStatus == "Terverifikasi" }
    val accepted = filteredList.count { it.admissionStatus == "Diterima" }

    val periodTitle = when (period) {
        "HARI_INI" -> "LAPORAN HARIAN SPMB"
        "KEMARIN" -> "LAPORAN KEMARIN SPMB"
        "MINGGU_INI" -> "LAPORAN MINGGUAN SPMB"
        "BULAN_INI" -> "LAPORAN BULANAN SPMB"
        else -> "LAPORAN REKAPITULASI SPMB"
    }

    val reportText = viewModel.repository.generateReportMessage(
        title = periodTitle,
        schoolName = settings.schoolName,
        applicants = filteredList,
        majors = majors,
        overallTarget = settings.overallTarget
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Laporan & Rekapitulasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Analisis penerimaan siswa baru berdasarkan periode & jurusan",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Periode Selector (Pills)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Pilih Periode Laporan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val periods = listOf(
                        "SEMUA" to "Semua Data",
                        "HARI_INI" to "Hari Ini",
                        "KEMARIN" to "Kemarin",
                        "MINGGU_INI" to "Minggu Ini",
                        "BULAN_INI" to "Bulan Ini"
                    )
                    periods.forEach { (key, label) ->
                        val isSelected = period == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewModel.reportPeriod.value = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Summary Cards
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Total Periode Ini",
                    value = "$total Siswa",
                    icon = {
                        Text("📊", fontSize = 20.sp)
                    },
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Terverifikasi",
                    value = "$verified Siswa",
                    badgeText = "${if (total > 0) verified * 100 / total else 0}%",
                    badgeColor = GreenSuccess,
                    icon = {
                        Text("✅", fontSize = 20.sp)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Buttons: Ekspor PDF, Excel, Cetak, WhatsApp
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Ekspor & Bagikan Laporan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.exportExcel() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            modifier = Modifier.weight(1f).testTag("btn_report_excel")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.exportPdf() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.weight(1f).testTag("btn_report_pdf")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onNavigateToWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        modifier = Modifier.fillMaxWidth().testTag("btn_report_to_wa")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Laporan Ini via WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Preview of Generated WhatsApp Report
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pratinjau Format Pesan WhatsApp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Laporan SPMB", reportText)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showMessage("Isi laporan berhasil disalin ke clipboard")
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Salin",
                                    tint = BluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.repository.shareTextReport(periodTitle, reportText)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Bagikan",
                                    tint = NavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            MajorDistributionChart(majors = majors, applicants = filteredList)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
