package com.example.venuehub.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.venuehub.R
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.theme.BluePrimary
import com.example.venuehub.ui.theme.TextGray

@Composable
fun KetentuanScreen(navController: NavController) {
    GenericInfoTemplate(
        title = "Ketentuan",
        content = stringResource(id = R.string.text_ketentuan),
        navController = navController
    )
}

@Composable
fun AlurScreen(navController: NavController) {
    GenericInfoTemplate(
        title = "Alur Peminjaman",
        content = stringResource(id = R.string.text_alur),
        navController = navController
    )
}

@Composable
fun GenericInfoTemplate(
    title: String,
    content: String,
    navController: NavController
) {
    Scaffold(
        topBar = {
            DetailHeader(title = title, onBackClick = { navController.popBackStack() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState()) // Agar bisa discroll
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(40.dp))

                VenueHubButton(
                    text = "Cari Ruangan",
                    onClick = {
                        navController.popBackStack()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DetailHeader(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(BluePrimary)
            .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-20).dp, y = (-30).dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp), // Padding status bar
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}