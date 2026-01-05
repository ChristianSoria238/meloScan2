package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melon.meloscan.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityGuideScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quality Guide",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(painterResource(R.drawable.ic_home),
                        null) },
                    label = { Text("Home") },
                    modifier = Modifier.size(28.dp)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(painterResource(R.drawable.ic_scnner),
                        null) },
                    label = { Text("Scan") },
                    modifier = Modifier.size(28.dp)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(painterResource(R.drawable.ic_history),
                        null) },
                    label = { Text("History") },
                    modifier = Modifier.size(28.dp)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(painterResource(R.drawable.ic_guide),
                        null) },
                    label = { Text("Guide") },
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            
        }
    }
}

@Composable
fun GuideSection(
    iconRes: Int,
    title: String,
    description: String,
    images: List<Int>,
    lowQualityIndex: Int,
    lowQualityLabelIsHigh: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color(0xFF3ED47A)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                images.forEachIndexed { index, imageRes ->
                    ImageWithLabel(
                        imageRes = imageRes,
                        isLowQuality = (index == lowQualityIndex),
                        isHighQualityLabel = lowQualityLabelIsHigh,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageWithLabel(
    imageRes: Int,
    isLowQuality: Boolean,
    isHighQualityLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val labelText = if (isHighQualityLabel) "High Quality" else "Low Quality"
    val labelColor = if (isHighQualityLabel) Color(0xFF3ED47A) else Color(0xFFFF6B6B)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        if (isLowQuality) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(labelColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = labelText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QualityGuideScreenPreview() {
    QualityGuideScreen()
}
