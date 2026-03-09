

package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.auth.AdminSession
import org.example.project.screen.FloatingBubbles



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    session: AdminSession,
    onLoggedIn: () -> Unit,
    baseUrl: String,
    onBaseUrlChange:  (String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()  // ✅ THÊM

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG
                        Color(0xFFFFF4E6),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)  // ✅ THÊM scroll
                .padding(horizontal = 80.dp, vertical = 20.dp),  // ✅ GIỐNG
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ LOGO CARD
            Card(
                modifier = Modifier
                    .size(140.dp)  // ✅ TĂNG:  120→140
                    .shadow(12.dp, CircleShape),  // ✅ GIỐNG
                shape = CircleShape,
                colors = CardDefaults. cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush. radialGradient(  // ✅ ĐỔI:  linear→radial
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍💼", fontSize = 64.sp)  // ✅ TĂNG: 56→64
                }
            }

            Spacer(modifier = Modifier. height(28.dp))  // ✅ GIẢM: 32→28

            // ✅ TITLE
            Text(
                text = "🔐 Admin Portal",
                fontSize = 28.sp,  // ✅ GIẢM: 32→28
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B00)  // ✅ ĐỔI
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đăng nhập để quản lý hệ thống",
                fontSize = 15.sp,  // ✅ GIẢM: 16→15
                fontWeight = FontWeight.Medium,  // ✅ THÊM
                color = Color(0xFF666666)  // ✅ ĐỔI
            )

            Spacer(modifier = Modifier.height(32.dp))  // ✅ GIẢM: 40→32

            // ✅ LOGIN FORM CARD
            Card(
                modifier = Modifier
                    .widthIn(max = 600.dp)  // ✅ ĐỔI: 500→600
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),  // ✅ GIỐNG
                colors = CardDefaults. cardColors(containerColor = Color. White),
                elevation = CardDefaults.cardElevation(6.dp)  // ✅ GIỐNG
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)  // ✅ GIẢM: 40→32
                ) {
                    // ✅ TITLE TRONG CARD
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Đăng nhập quản trị",
                            fontSize = 20.sp,
                            fontWeight = FontWeight. ExtraBold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ USERNAME
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            error = null
                        },
                        label = { Text("Tên đăng nhập", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("Nhập tên đăng nhập", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                tint = Color(0xFFFF6B9D),  // ✅ ĐỔI
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(64.dp),  // ✅ GIỐNG
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults. colors(
                            focusedBorderColor = Color(0xFFFF6B9D),  // ✅ ĐỔI
                            focusedLabelColor = Color(0xFFFF6B9D),
                            focusedLeadingIconColor = Color(0xFFFF6B9D),
                            cursorColor = Color(0xFFFF6B9D)
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))  // ✅ GIỐNG

                    // ✅ PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("Mật khẩu", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        placeholder = { Text("Nhập mật khẩu", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default. Lock,
                                contentDescription = "Password",
                                tint = Color(0xFF4CAF50),  // ✅ ĐỔI
                                modifier = Modifier. size(24.dp)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),  // ✅ GIỮ NGUYÊN
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),  // ✅ ĐỔI
                            focusedLabelColor = Color(0xFF4CAF50),
                            focusedLeadingIconColor = Color(0xFF4CAF50),
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ✅ LOGIN BUTTON
                    Button(
                        onClick = {
                            error = null
                            loading = true
                            scope.launch {
                                val res = session. login(username.trim(), password)
                                loading = false
                                res.onSuccess {
                                    onLoggedIn()
                                }.onFailure { e ->
                                    error = e.message ?: "Đăng nhập thất bại"
                                }
                            }
                        },
                        enabled = !loading && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),  // ✅ GIỐNG
                        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),  // ✅ ĐỔI
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 16.dp  // ✅ TĂNG:  4→16
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement. Center
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),  // ✅ TĂNG: 24→28
                                    color = Color.White,
                                    strokeWidth = 4.dp  // ✅ TĂNG: 3→4
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    "Đang đăng nhập...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    "Đăng nhập",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // ✅ ERROR MESSAGE
                    if (error != null) {
                        Spacer(modifier = Modifier. height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("❌", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = error!! ,
                                    color = Color(0xFFE53935),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // ✅ FOOTER
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color. White. copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier. padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Card Management System",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}