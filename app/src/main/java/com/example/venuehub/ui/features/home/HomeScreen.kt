package com.example.venuehub.ui.features.home
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.R
import com.example.venuehub.ui.features.admin.RoomItemData
import com.example.venuehub.ui.theme.*
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var userName by remember { mutableStateOf("User") }
    var roomList by remember { mutableStateOf<List<RoomItemData>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val result = SupabaseClient.client.from("rooms")
                .select().decodeList<RoomItemData>()
            roomList = result.reversed().take(5) //
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(userName = userName)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
        ) {
            BannerSection()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Informasi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            InfoMenuSection(navController)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ruangan Populer!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PopularRoomsSection(rooms = roomList, navController = navController)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Kategori Ruangan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            CategorySection(navController = navController)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HomeHeader(userName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(BluePrimary)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Selamat Datang, $userName",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fakultas Ilmu Komputer",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { /* TODO: Notifikasi */ }) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 10.dp
        ) { page ->
            Card(
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(5.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                    Text(
                        text = "Banner Info ${page + 1}",
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) BluePrimary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoMenuSection(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        InfoCard(
            title = "Ketentuan\nPeminjaman",
            icon = painterResource(id = R.drawable.informasiprotection),
            modifier = Modifier.weight(1f),
            onClick = {
                navController.navigate("ketentuan")
            }

        )

        InfoCard(
            title = "Alur\nPeminjaman",
            icon = painterResource(id = R.drawable.informasitoa),
            modifier = Modifier.weight(1f),
            onClick = {
                navController.navigate("alur")
            }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFCCCFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
    }
}
@Composable
fun PopularRoomsSection(rooms: List<RoomItemData>, navController: NavController) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        if (rooms.isEmpty()) {
            items(3) {
                Box(modifier = Modifier.size(160.dp, 200.dp).background(Color.White, RoundedCornerShape(12.dp)))
            }
        } else {
            items(rooms) { room ->
                RoomCard(room = room, onClick = {
                    navController.navigate("user_room_detail/${room.id}")
                })
            }
        }
    }
}

@Composable
fun RoomCard(room: RoomItemData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 210.dp)
            .clickable { onClick() }, // Bisa diklik
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color.LightGray)) {
                AsyncImage(
                    model = room.imageUrl ?: "https://placehold.co/600x400/png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = room.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${room.capacity} Orang",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = room.facilities ?: "Fasilitas -",
                    fontSize = 10.sp,
                    color = BluePrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun ReviewsSection() {

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewItem(modifier = Modifier.weight(1f))
            ReviewItem(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewItem(modifier = Modifier.weight(1f))
            ReviewItem(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ReviewItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Joni", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                        Text(text = " 4.5", fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Proses peminjaman cepat dan mudah.",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "23/10/24",
                fontSize = 9.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
@Composable
fun CategorySection(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            CategoryCard(
                title = "Ruang Kelas",
                icon = Icons.Default.School,
                color = Color(0xFFE3F2FD),
                iconColor = BluePrimary,
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Filter ke Ruang Kelas */ }
            )
            CategoryCard(
                title = "Aula Besar",
                icon = Icons.Default.TheaterComedy,
                color = Color(0xFFF3E5F5),
                iconColor = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Filter ke Aula */ }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            CategoryCard(
                title = "Lab Komputer",
                icon = Icons.Default.Computer,
                color = Color(0xFFE0F2F1),
                iconColor = Color(0xFF009688),
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Filter ke Lab */ }
            )
            CategoryCard(
                title = "Ruang Rapat",
                icon = Icons.Default.MeetingRoom,
                color = Color(0xFFFFF3E0),
                iconColor = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Filter ke Rapat */ }
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 16.sp
            )
        }
    }
}