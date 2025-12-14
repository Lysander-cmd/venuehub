package com.example.venuehub.ui.features.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.features.admin.RoomItemData
import com.example.venuehub.ui.theme.BluePrimary

@Composable
fun UserRoomDetailScreen(
    navController: NavController,
    roomId: Long,
    viewModel: RoomDetailViewModel = viewModel()
) {
    // Panggil fungsi fetch dari ViewModel
    LaunchedEffect(roomId) {
        viewModel.fetchRoomDetail(roomId)
    }

    Scaffold(
        topBar = {
            AdminHeader(title = "Detail Ruangan", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            if (viewModel.roomData != null) {
                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .imePadding()
                ) {
                    VenueHubButton(
                        text = "Pinjam Sekarang",
                        onClick = {
                            navController.navigate("booking_form/$roomId")
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            viewModel.roomData?.let { room ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = room.imageUrl ?: "https://placehold.co/600x400/png",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = room.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Row {
                            InfoChipDetail(Icons.Default.Groups, "${room.capacity} Orang")
                            Spacer(modifier = Modifier.width(15.dp))
                            InfoChipDetail(Icons.Default.SquareFoot, "Fasilitas Lengkap")
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        // Divider diganti HorizontalDivider sesuai Material3 terbaru agar tidak merah
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = room.description ?: "Tidak ada deskripsi tersedia.",
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Fasilitas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = room.facilities ?: "-",
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChipDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text, color = Color.Gray, fontSize = 13.sp)
    }
}