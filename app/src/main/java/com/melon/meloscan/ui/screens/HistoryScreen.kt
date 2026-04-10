package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.R
import com.melon.meloscan.ui.navigation.AppBottomBar

// Updated Data Class to reflect Thesis requirements
data class ScanHistoryItem(
    val id: Int,
    val imageRes: Int,
    val scanType: String, // "Leaf Disease" or "Fruit Quality"
    val result: String,   // e.g., "Anthracnose" or "High Quality"
    val date: String,
    val confidence: Int
)

// Thesis-aligned Color Palette
val GreenResult = Color(0xFF2E7D32)
val OrangeResult = Color(0xFFEF6C00)
val RedResult = Color(0xFFC62828)
val BlueType = Color(0xFF1976D2) // For Fruit Quality
val LeafType = Color(0xFF689F38) // For Leaf Disease

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
    val historyItems = listOf(
        ScanHistoryItem(1, R.drawable.watermelon, "Leaf Disease", "Healthy", "Oct 24, 2024 • 10:30 AM", 98),
        ScanHistoryItem(2, R.drawable.fruity, "Fruit Quality", "High Quality", "Oct 23, 2024 • 03:15 PM", 92),
        ScanHistoryItem(3, R.drawable.watermelon, "Leaf Disease", "Anthracnose", "Oct 22, 2024 • 11:00 AM", 85),
        ScanHistoryItem(4, R.drawable.fruity, "Fruit Quality", "Low Quality", "Oct 21, 2024 • 09:45 AM", 76),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Detection History", fontWeight = FontWeight.ExtraBold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentScreen = "History")
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Statistical Header for Thesis Feel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Scans: ${historyItems.size}", fontSize = 14.sp, color = Color.Gray)
                Text("Avg. Confidence: 87%", fontSize = 14.sp, color = Color.Gray)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyItems) { item ->
                    HistoryItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: ScanHistoryItem) {
    // Determine color based on result
    val statusColor = when {
        item.result.contains("Good") || item.result == "Mosaic Disease" -> GreenResult
        item.result.contains("Bad") || item.result.contains("Downy Meldew") -> OrangeResult
        else -> RedResult
    }

    val typeColor = if (item.scanType == "Leaf Disease") LeafType else BlueType

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image with Category Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                )
                // Small Category Icon/Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(typeColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Category Label
                Text(
                    text = item.scanType.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    letterSpacing = 1.sp
                )

                // Result Result
                Text(
                    text = item.result,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Metadata
                Text(
                    text = item.date,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Confidence Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = item.confidence / 100f,
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.confidence}% match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // Action Button
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen(navController = rememberNavController())
}