package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.screen.staff.*
import org.example.project.viewmodel.AuthViewModel

enum class StaffTab { ISSUE_CARD, CARD_REQUESTS, TOP_UP, RETURN_CARD }

@Composable
fun StaffApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF6B35),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE0D0),
            secondary = Color(0xFF1A1A2E),
            surface = Color.White,
            background = Color(0xFFF8F9FA)
        )
    ) {
        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()

        if (!authState.isLoggedIn) {
            LoginScreen(viewModel = authViewModel)
        } else {
            StaffMainLayout(
                staffName = authState.adminInfo?.fullName ?: "Nhân viên",
                onLogout = { authViewModel.logout() }
            )
        }
    }
}

@Composable
private fun StaffMainLayout(staffName: String, onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(StaffTab.ISSUE_CARD) }

    Row(modifier = Modifier.fillMaxSize()) {
        // ── Sidebar ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(Color(0xFF1A1A2E))
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Logo
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎡", fontSize = 36.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Quầy Lễ Tân", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Smart Card System", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }

                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))

                // Staff info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFF6B35).copy(alpha = 0.2f)) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFFFF6B35), modifier = Modifier.padding(6.dp).size(16.dp))
                        }
                        Column {
                            Text(staffName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Nhân viên", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nav items
                NavItem(icon = Icons.Default.CreditCard,    label = "Cấp thẻ mới",    isSelected = currentTab == StaffTab.ISSUE_CARD)    { currentTab = StaffTab.ISSUE_CARD }
                NavItem(icon = Icons.Default.Inbox,          label = "Yêu cầu cấp thẻ", isSelected = currentTab == StaffTab.CARD_REQUESTS) { currentTab = StaffTab.CARD_REQUESTS }
                NavItem(icon = Icons.Default.AccountBalanceWallet, label = "Nạp tiền", isSelected = currentTab == StaffTab.TOP_UP)       { currentTab = StaffTab.TOP_UP }
                NavItem(icon = Icons.Default.AssignmentReturn, label = "Trả thẻ",     isSelected = currentTab == StaffTab.RETURN_CARD)    { currentTab = StaffTab.RETURN_CARD }
            }

            // Logout
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Đăng xuất", fontSize = 13.sp)
            }
        }

        // ── Main content ────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (currentTab) {
                StaffTab.ISSUE_CARD    -> IssueCardScreen()
                StaffTab.CARD_REQUESTS -> CardRequestsScreen()
                StaffTab.TOP_UP        -> TopUpScreen()
                StaffTab.RETURN_CARD   -> ReturnCardScreen()
            }
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) Color(0xFFFF6B35) else Color.White.copy(alpha = 0.65f)
    val iconColor = if (isSelected) Color(0xFFFF6B35) else Color.White.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 13.sp, color = textColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
    Spacer(Modifier.height(2.dp))
}
