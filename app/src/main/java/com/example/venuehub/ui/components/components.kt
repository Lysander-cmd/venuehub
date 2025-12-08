package com.example.venuehub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venuehub.R
import com.example.venuehub.ui.theme.BluePrimary
import com.example.venuehub.ui.theme.StrokeColor
import com.example.venuehub.ui.theme.TextGray

// Input Text Biasa
@Composable
fun VenueHubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = label, color = TextGray, fontSize = 14.sp) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = StrokeColor,
            focusedBorderColor = BluePrimary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

// Input Password
@Composable
fun VenueHubPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = label, color = TextGray, fontSize = 14.sp) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = StrokeColor,
            focusedBorderColor = BluePrimary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible)
                android.R.drawable.ic_menu_view // Ikon mata terbuka
            else
                android.R.drawable.ic_secure // Ikon gembok/mata tertutup

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                // Menggunakan Icon standar Android sementara
                Icon(
                    painter = painterResource(id = image),
                    contentDescription = if(passwordVisible) "Hide" else "Show",
                    tint = TextGray
                )
            }
        }
    )
}

// Tombol Biru
@Composable
fun VenueHubButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 16.sp, style = MaterialTheme.typography.titleMedium)
    }
}

// Tombol Google
@Composable
fun GoogleLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TextGray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
    ) {
        Text(text = " G ", color = Color.Red, style = MaterialTheme.typography.titleLarge)
        Text(text = "Masuk dengan Google", modifier = Modifier.padding(start = 8.dp))
    }
}