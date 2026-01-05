package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.melon.meloscan.R


data class ScanHistoryItem(
    val id: Int,
    val imageRes: Int,
    val quality: String,
    val date: String,
    val confidence: Int
)

val OrangeTint = Color(0xFFFFA500) 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    // Dummy data for preview
    val historyItems = listOf(
        ScanHistoryItem(1, R.drawable.ic_launcher_background, "High Quality", "2024-07-20 10:30 AM", 95),
        ScanHistoryItem(2, R.drawable.ic_launcher_background, "Medium Quality", "2024-07-19 03:15 PM", 72),
        ScanHistoryItem(3, R.drawable.ic_launcher_background, "Low Quality", "2024-07-18 11:00 AM", 48),
        ScanHistoryItem(4, R.drawable.ic_launcher_background, "High Quality", "2024-07-17 09:45 AM", 91),
        ScanHistoryItem(5, R.drawable.ic_launcher_background, "Medium Quality", "2024-07-16 01:20 PM", 65),
        ScanHistoryItem(6, R.drawable.ic_launcher_background, "Low Quality", "2024-07-15 08:30 AM", 55),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scan History",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyItems) { item ->
                HistoryItemCard(item = item)
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: ScanHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.quality,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Quality Text/Tag
                if (item.quality == "Low Quality") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RedTint)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(item.quality, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Text(
                        text = item.quality,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (item.quality == "High Quality") GreenTint else OrangeTint
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.date, fontSize = 12.sp, color = Color.Gray)
                Text(text = "Confidence: ${item.confidence}%", fontSize = 12.sp, color = Color.Gray)
            }

            IconButton(onClick = { /* TODO: Delete item */ }) {
                Icon(painterResource(R.drawable.delete), "Delete", tint = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen()
}
