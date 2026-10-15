package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SpmbViewModel
import com.example.ui.components.DailyRegistrationChart
import com.example.ui.components.MajorDistributionChart
import com.example.ui.components.StatCard
import com.example.ui.components.getMajorColor
import com.example.ui.components.getMajorFullName
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueSurface
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RedDanger
import com.example.ui.theme.RedLight
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SpmbViewModel,
    onNavigateToApplicants: () -> Unit,
    onNavigateToWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val applicants by viewModel.applicants.collectAsState()
    val majors by viewModel.majors.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val totalApplicants = applicants.size
    val verifiedCount = applicants.count { it.verificationStatus == "Terverifikasi" }
    val pendingCount = applicants.count { it.verificationStatus == "Menunggu verifikasi" }
    val needsFixCount = applicants.count { it.verificationStatus == "Perlu perbaikan" }
    val acceptedCount = applicants.count { it.admissionStatus == "Diterima" }

    // Start of today in millis
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = cal.timeInMillis
    val todayCount = applicants.count { it.registrationDate >= startOfToday }

    val overallTarget = settings.overallTarget.coerceAtLeast(1)
    val overallProgress = (totalApplicants.toFloat() / overallTarget).coerceIn(0f, 1f)
    val overallPercentage = String.format(Locale.US, "%.1f", (totalApplicants.toDouble() / overallTarget) * 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NavyDark, Color(0xFF1E3A8A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Surface(
                                    color = Color(0xFF2563EB).copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "Tahun Ajaran ${settings.academicYear}",
                                        color = Color(0xFF93C5FD),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = settings.schoolName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sistem Monitoring Penerimaan Murid Baru",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Target summary bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(14.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Capaian Target Siswa Baru",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$totalApplicants / $overallTarget ($overallPercentage%)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF67E8F9),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { overallProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Fast Action to WhatsApp Report
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onNavigateToWhatsApp,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenSuccess,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_quick_wa_report")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Laporan WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToApplicants,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.18f),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_quick_view_applicants")
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kelola Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Statistik Utama (6 Indicators)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Statistik Pendaftaran",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Total Pendaftar",
                        value = totalApplicants.toString(),
                        badgeText = "+$todayCount hari ini",
                        badgeColor = BluePrimary,
                        icon = {
                            Icon(Icons.Default.People, contentDescription = null, tint = BluePrimary)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Daftar Hari Ini",
                        value = todayCount.toString(),
                        subtitle = "Pendaftar baru",
                        icon = {
                            Icon(Icons.Default.Today, contentDescription = null, tint = OrangeWarning)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Terverifikasi",
                        value = verifiedCount.toString(),
                        badgeText = "${if (totalApplicants > 0) (verifiedCount * 100 / totalApplicants) else 0}%",
                        badgeColor = GreenSuccess,
                        icon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Menunggu Verif.",
                        value = pendingCount.toString(),
                        subtitle = "Perlu tindakan admin",
                        icon = {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = OrangeWarning)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Siswa Diterima",
                        value = acceptedCount.toString(),
                        badgeText = "Resmi",
                        badgeColor = GreenSuccess,
                        icon = {
                            Icon(Icons.Default.School, contentDescription = null, tint = BluePrimary)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Perlu Perbaikan",
                        value = needsFixCount.toString(),
                        subtitle = "Berkas belum lengkap",
                        icon = {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = RedDanger)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: 5 Kartu Jurusan
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Statistik 5 Jurusan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Target Kuota",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                majors.forEach { major ->
                    val count = applicants.count { it.majorCode.equals(major.code, ignoreCase = true) }
                    val percentOfTotal = if (totalApplicants > 0) {
                        String.format(Locale.US, "%.1f", (count.toDouble() / totalApplicants) * 100)
                    } else "0"
                    val progressToQuota = (count.toFloat() / major.quota.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val majorColor = getMajorColor(major.code)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = majorColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = major.code,
                                            color = majorColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = getMajorFullName(major.code),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$percentOfTotal% dari seluruh pendaftar",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$count Siswa",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = majorColor
                                    )
                                    Text(
                                        text = "Target: ${major.quota}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progressToQuota },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = majorColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Visual Charts
        item {
            MajorDistributionChart(majors = majors, applicants = applicants)
        }

        item {
            DailyRegistrationChart(applicants = applicants)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
