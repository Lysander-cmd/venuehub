package com.example.venuehub.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.theme.BluePrimary
import com.example.venuehub.ui.theme.TextGray
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomItemData(
    val id: Long = 0,
    val name: String,
    val capacity: Int,
    val description: String? = "",
    val facilities: String? = "",
    val category: String? = "",
    @SerialName("image_url") val imageUrl: String? = null
)

@Composable
fun AdminRoomListScreen(navController: NavController) {

    var roomList by remember { mutableStateOf<List<RoomItemData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val result = SupabaseClient.client.from("rooms")
                .select().decodeList<RoomItemData>()
            roomList = result.reversed()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("admin_add_room") },
                containerColor = BluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Ruangan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            RoomListHeader()

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(roomList) { room ->
                        RoomItemCard(room = room, navController = navController)
                    }
                }
            }
        }
    }
}


@Composable
fun RoomListHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(BluePrimary)
            .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
    ) {
        Box(
            modifier = Modifier.offset(x = (-30).dp, y = (-40).dp).size(120.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-40).dp)
                .size(150.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cari Ruangan", color = TextGray, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Lokasi Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Fakultas Ilmu Komputer", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun RoomItemCard(room: RoomItemData,navController: NavController) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { navController.navigate("admin_room_detail/${room.id}") }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = room.imageUrl ?: "https://placehold.co/600x400/png",
                    contentDescription = room.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {

                Text(
                    text = room.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${room.capacity} Orang",
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SquareFoot, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Fasilitas Lengkap",
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}