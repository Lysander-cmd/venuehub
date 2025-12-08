package com.example.venuehub.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
        verticalArrangement = Arrangement.Center // Konten di tengah vertikal
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_venuehub),
            contentDescription = "Logo VenueHub",
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "VenueHub",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Buat Akun Baru",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Silakan isi data diri Anda untuk mendaftar.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        VenueHubTextField(value = name, onValueChange = { name = it }, label = "Nama Lengkap")
        Spacer(modifier = Modifier.height(10.dp))

        VenueHubTextField(value = email, onValueChange = { email = it }, label = "Email")
        Spacer(modifier = Modifier.height(10.dp))

        VenueHubPasswordTextField(value = password, onValueChange = { password = it }, label = "Kata Sandi")
        Spacer(modifier = Modifier.height(10.dp))

        VenueHubPasswordTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Konfirmasi Kata Sandi")

        Spacer(modifier = Modifier.height(20.dp))

        val termsText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = TextGray, fontSize = 11.sp)) {
                append("Dengan mendaftar, Anda menyetujui ")
            }
            withStyle(style = SpanStyle(color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)) {
                append("Syarat & Ketentuan")
            }
            withStyle(style = SpanStyle(color = TextGray, fontSize = 11.sp)) {
                append(" kami.")
            }
        }

        Text(
            text = termsText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        if (isLoading) {
            CircularProgressIndicator(color = BluePrimary)
        } else {
            VenueHubButton(
                text = "Daftar Sekarang",
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                        return@VenueHubButton
                    }

                    if (password != confirmPassword) {
                        Toast.makeText(context, "Password tidak sama", Toast.LENGTH_SHORT).show()
                        return@VenueHubButton
                    }

                    if (password.length < 6) {
                        Toast.makeText(context, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                        return@VenueHubButton
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            SupabaseClient.client.auth.signUpWith(Email) {
                                this.email = email
                                this.password = password

                                data = buildJsonObject {
                                    put("full_name", name)
                                }
                            }

                            Toast.makeText(context, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()

                            navController.popBackStack()

                        } catch (e: Exception) {
                            val errorMessage = e.message ?: "Terjadi kesalahan"
                            Toast.makeText(context, "Gagal: $errorMessage", Toast.LENGTH_LONG).show()
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
            Text(text = "Sudah memiliki akun? ", color = TextGray, fontSize = 13.sp)
            Text(
                text = "Masuk",
                color = BluePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.popBackStack() // Kembali ke Login
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}