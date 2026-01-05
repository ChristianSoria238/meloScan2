package com.melon.meloscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melon.meloscan.R

@Composable
fun ScanScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6EBE0)) // Light green-gray background
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Navigate back */ }) {
                Icon(
                    painter = painterResource(R.drawable.close), 
                    contentDescription = "Close",
                    modifier = Modifier.size(20.dp)
                )
            }
            // You can add the magic wand icon here if needed
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // "Align the item" prompt
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Text(
                    text = "Align the item within\nthe frame",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            // Framing Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .border(3.dp, Color(0xFF8BC34A), RoundedCornerShape(24.dp)) // Green border
            ) {
                // The grid can be implemented using a series of Box dividers
                // This is a simplified representation. For a real camera, this would be an overlay.
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Icon Button
                IconButton(onClick = { /* TODO: Open gallery */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.gallary), 
                        contentDescription = "Upload from Gallery",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Camera Shutter Button
                Button(
                    onClick = { /* TODO: Take picture */ },
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ED47A)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Take Picture",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Spacer to balance the layout
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ScanScreen()
}

