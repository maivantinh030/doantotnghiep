package org.example.project.screen.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.viewmodel.IssueCardViewModel

private val Orange = Color(0xFFFF6B35)

@Composable
fun IssueCardScreen(vm: IssueCardViewModel = viewModel { IssueCardViewModel() }) {
    val s by vm.state.collectAsStateWithLifecycle()

    var nameInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text("Cấp thẻ mới", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Text(
                    "Nhập thông tin → Đặt thẻ lên đầu đọc → Nhấn Cấp thẻ",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (s.writeSuccess) {
                        // ── Trạng thái thành công ──────────────────────────────
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(52.dp)
                                )
                                Text(
                                    "Cấp thẻ thành công!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                s.successMessage?.let {
                                    Text(it, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                vm.reset()
                                nameInput = ""
                                dobInput = ""
                                phoneInput = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cấp thẻ mới", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // ── Form nhập thông tin ────────────────────────────────
                        Text(
                            "Thông tin khách hàng",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1A2E)
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Họ và tên *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Orange) }
                        )

                        OutlinedTextField(
                            value = dobInput,
                            onValueChange = { dobInput = it },
                            label = { Text("Ngày sinh (YYYY-MM-DD)") },
                            placeholder = { Text("Vd: 1995-08-20") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Orange) }
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Số điện thoại *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Orange) }
                        )

                        s.errorMessage?.let { msg ->
                            Surface(
                                color = Color(0xFFE53935).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        tint = Color(0xFFB71C1C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(msg, color = Color(0xFFB71C1C), fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = {
                                vm.issueCard(nameInput.trim(), dobInput.trim(), phoneInput.trim())
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange),
                            enabled = !s.isWriting && nameInput.isNotBlank() && phoneInput.isNotBlank()
                        ) {
                            if (s.isWriting) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Đang ghi thẻ...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            } else {
                                Icon(
                                    Icons.Default.CreditCard,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Cấp thẻ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
