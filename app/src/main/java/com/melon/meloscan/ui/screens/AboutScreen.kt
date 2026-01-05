package com.melon.meloscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About MelonVision",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { /*  BottomNavigationBar here */ },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp)
        ) {
            
            item {
                Text("MelonVision Project", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Developed as a Computer Science Thesis", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MelonVision is an innovative AI-powered mobile application dedicated to revolutionizing watermelon quality detection. Leveraging advanced image analysis, the app provides instant, reliable quality assessments, empowering users to make informed choices. Our mission is to combine cutting-edge technology with user-friendly design to enhance daily consumer experiences and promote better food quality standards.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Technologies Section
            item {
                Text("Technologies Used", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Using accompanist-flowlayout for the tags. Make sure to add the dependency.
                // implementation "com.google.accompanist:accompanist-flowlayout:0.27.0"
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    TechnologyChip("Computer Vision")
                    TechnologyChip("Image Processing")
                    TechnologyChip("Machine Learning")
                    TechnologyChip("Deep Learning")
                    TechnologyChip("Mobile Development")
                    TechnologyChip("AI Inference")
                    TechnologyChip("Data Analysis")
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Credits Section
            item {
                Text("Contact & Credits", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Team Email: christian3.soria@bisu.edu.ph", fontSize = 14.sp, color = Color.DarkGray)
                Text("                        aljuneRhyle.pancho@bisu.edu.ph", fontSize = 14.sp, color = Color.DarkGray)
                Text("                        ellengrace.calboero@bisu.edu.ph", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("© 2024 MelonVision Team. All rights reserved.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TechnologyChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0F2E6)) // Light green background
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color(0xFF3ED47A), fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
