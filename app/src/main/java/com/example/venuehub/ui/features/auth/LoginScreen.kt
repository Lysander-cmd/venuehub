package com.example.venuehub.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.venuehub.R
import com.example.venuehub.ui.components.*
import com.example.venuehub.ui.theme.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.providers.builtin.Email
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 35.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_venuehub),
            contentDescription = "Logo VenueHub",
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "VenueHub",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Selamat Datang Kembali!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Masuk untuk mengelola peminjaman ruangan.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        VenueHubTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        Spacer(modifier = Modifier.height(15.dp))

        VenueHubPasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Kata Sandi"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Lupa kata sandi?",
                color = BluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Fitur Reset Password belum dibuat", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        if (isLoading) {
            CircularProgressIndicator(color = BluePrimary)
        } else {
            VenueHubButton(
                text = "Masuk",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                        return@VenueHubButton
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            SupabaseClient.client.auth.signInWith(Email) {
                                this.email = email
                                this.password = password
                            }

                            val user = SupabaseClient.client.auth.currentUserOrNull()
                            val role = user?.userMetadata?.get("role")?.jsonPrimitive?.content

                            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                            if (role == "admin") {
                                navController.navigate("admin_home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                        } catch (e: Exception) {
                            val errorMessage = e.message ?: "Gagal Login"
                            if (errorMessage.contains("Invalid login credentials")) {
                                Toast.makeText(context, "Email atau Password salah.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan koneksi.", Toast.LENGTH_LONG).show()
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Belum memiliki akun? ", color = TextGray, fontSize = 13.sp)
            Text(
                text = "Daftar Sekarang",
                color = BluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}