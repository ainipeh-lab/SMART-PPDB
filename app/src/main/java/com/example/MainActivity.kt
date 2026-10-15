package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.SpmbViewModel
import com.example.ui.screens.ApplicantsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WhatsAppScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.SPMBTheme

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SPMBTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: SpmbViewModel = viewModel()) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    var firebaseUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(auth.currentUser) }
    var forceOfflineMode by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { a ->
            firebaseUser = a.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (firebaseUser == null && !forceOfflineMode) {
        com.example.ui.auth.AuthScreen(
            onAuthSuccess = {
                firebaseUser = auth.currentUser
            },
            onContinueOffline = {
                forceOfflineMode = true
            }
        )
    } else {
        MainApp(
            viewModel = viewModel,
            firebaseUser = firebaseUser,
            onSignOut = {
                forceOfflineMode = false
                auth.signOut()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: SpmbViewModel = viewModel(),
    firebaseUser: com.google.firebase.auth.FirebaseUser? = null,
    onSignOut: () -> Unit = {}
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showRoleDialog by remember { mutableStateOf(false) }

    // Listen to feedback messages
    LaunchedEffect(viewModel) {
        viewModel.feedbackMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back press: if not on Dashboard (tab 0), return to tab 0
    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    val navItems = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem("Pendaftar", Icons.Default.People, "nav_applicants"),
        NavItem("Laporan", Icons.Default.Assessment, "nav_reports"),
        NavItem("WhatsApp", Icons.Default.Chat, "nav_whatsapp"),
        NavItem("Pengaturan", Icons.Default.Settings, "nav_settings")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SPMB SMART REPORT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyDark,
                                maxLines = 1
                            )
                            Text(
                                text = settings.schoolName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        // Role indicator button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (currentUser.role) {
                                UserRole.SUPER_ADMIN -> Color(0xFFDC2626).copy(alpha = 0.12f)
                                UserRole.ADMIN_SPMB -> OrangeWarning.copy(alpha = 0.15f)
                                UserRole.KEPALA_SEKOLAH -> BluePrimary.copy(alpha = 0.12f)
                            },
                            modifier = Modifier
                                .clickable { showRoleDialog = true }
                                .testTag("btn_switch_role")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = when (currentUser.role) {
                                        UserRole.SUPER_ADMIN -> Color(0xFFDC2626)
                                        UserRole.ADMIN_SPMB -> OrangeWarning
                                        UserRole.KEPALA_SEKOLAH -> BluePrimary
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser.role.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentUser.role) {
                                        UserRole.SUPER_ADMIN -> Color(0xFFDC2626)
                                        UserRole.ADMIN_SPMB -> OrangeWarning
                                        UserRole.KEPALA_SEKOLAH -> BluePrimary
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToApplicants = { viewModel.setTab(1) },
                    onNavigateToWhatsApp = { viewModel.setTab(3) }
                )
                1 -> ApplicantsScreen(viewModel = viewModel)
                2 -> ReportsScreen(
                    viewModel = viewModel,
                    onNavigateToWhatsApp = { viewModel.setTab(3) }
                )
                3 -> WhatsAppScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { viewModel.setTab(4) }
                )
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Role switcher dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = {
                Text(text = "Pilih Peran Pengguna", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Setiap peran memiliki hak akses sesuai kebijakan sekolah:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    viewModel.availableUsers.forEach { user ->
                        val isSelected = currentUser.id == user.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NavyDark else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user)
                                    viewModel.showMessage("Masuk sebagai ${user.role.displayName}")
                                    showRoleDialog = false
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.role.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = user.name,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = user.role.description,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38BDF8))
                                }
                            }
                        }
                    }

                    if (firebaseUser != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Akun Google Cloud Firebase:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                                Text(
                                    text = firebaseUser.email ?: firebaseUser.uid,
                                    fontSize = 12.sp,
                                    color = NavyDark
                                )
                            }
                        }

                        androidx.compose.material3.Button(
                            onClick = {
                                showRoleDialog = false
                                onSignOut()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_sign_out")
                        ) {
                            Text("Keluar dari Akun Google", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showRoleDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
