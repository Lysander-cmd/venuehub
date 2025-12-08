package com.example.venuehub.ui.features.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var userName by remember { mutableStateOf("Memuat...") }
    var userEmail by remember { mutableStateOf("-") }

    LaunchedEffect(Unit) {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            userEmail = user.email ?: "-"
            val nameMeta = user.userMetadata?.get("full_name")?.jsonPrimitive?.content
            userName = nameMeta ?: "User"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        AdminHeader(title = "Profil Saya", onBackClick = {})

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(15.dp))
                        Column {
                            Text("Nama Lengkap", fontSize = 12.sp, color = Color.Gray)
                            Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 15.dp), color = Color(0xFFEEEEEE))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = BluePrimary)
                        Spacer(modifier = Modifier.width(15.dp))
                        Column {
                            Text("Email", fontSize = 12.sp, color = Color.Gray)
                            Text(userEmail, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    scope.launch {
                        SupabaseClient.client.auth.signOut()

                        Toast.makeText(context, "Berhasil Keluar", Toast.LENGTH_SHORT).show()

                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Keluar Akun", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}