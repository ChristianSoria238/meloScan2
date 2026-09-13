package com.melon.meloscan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.melon.meloscan.R
import com.melon.meloscan.ui.navigation.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Library", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentScreen = "Library")
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Leaf Diseases Card
            LibraryCard(
                title = "Leaf Diseases",
                subtitle = "Browse common watermelon leaf diseases",
                iconRes = R.drawable.watermelon,
                onClick = { navController.navigate("library_diseases_list") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Medicines Card
            LibraryCard(
                title = "Medicines",
                subtitle = "Treatment and management options",
                iconRes = R.drawable.book,
                onClick = { navController.navigate("library_medicines_list") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Resources", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            ResourceItem(title = "Research Documentation", link = "https://example.com/research")
            ResourceItem(title = "Watermelon Care YouTube", link = "https://youtube.com/watermelon")
            ResourceItem(title = "Pest Management Guide", link = "https://example.com/pests")
        }
    }
}

@Composable
fun LibraryCard(title: String, subtitle: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F9))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ResourceItem(title: String, link: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Open link: $link */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF3ED47A), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFF3ED47A),
            textDecoration = TextDecoration.Underline
        )
    }
}

// --- Leaf Diseases List Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafDiseasesListScreen(navController: NavController) {
    val diseases = listOf("Anthracnose", "Downy Mildew", "Mosaic Disease", "Fusarium Wilt")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaf Diseases") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(diseases) { disease ->
                ListItem(
                    headlineContent = { Text(disease) },
                    modifier = Modifier.clickable { navController.navigate("library_disease_detail?name=$disease") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    }
}

// --- Leaf Disease Detail Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafDiseaseDetailScreen(navController: NavController, diseaseName: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(diseaseName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            item {
                Image(
                    painter = painterResource(R.drawable.watermelon),
                    contentDescription = diseaseName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Image Source: https://bit.ly/disease-image-ref",
                    fontSize = 11.sp,
                    color = Color(0xFF3ED47A),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* TODO: Open Link */ }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailSection("Name", diseaseName)
                DetailSection("Other Name", "N/A")
                DetailSection("Scientific Name", "Colletotrichum orbiculare (Example)")
                DetailSection("Description", "A destructive disease affecting leaves, stems, and fruits of watermelons.")
                DetailSection("Damage Characteristics", "Circular to irregular brown spots on leaves.")
                DetailSection("Cause", "Fungal pathogen spread by splashing water.")
                DetailSection("Management Practice", "Crop rotation and fungicide application.")
            }
        }
    }
}

// --- Medicines List Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesListScreen(navController: NavController) {
    val medicines = listOf("Mancozeb", "Chlorothalonil", "Benomyl", "Copper Fungicide")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicines") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(medicines) { medicine ->
                ListItem(
                    headlineContent = { Text(medicine) },
                    modifier = Modifier.clickable { navController.navigate("library_medicine_detail?name=$medicine") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    }
}

// --- Medicine Detail Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(navController: NavController, medicineName: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medicineName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            item {
                Image(
                    painter = painterResource(R.drawable.book),
                    contentDescription = medicineName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailSection("Name", medicineName)
                DetailSection("Description", "Effective for controlling various fungal diseases.")
                DetailSection("Target Disease", "Anthracnose, Downy Mildew")
                DetailSection("Application Pattern", "Spray evenly on upper and lower leaf surfaces.")
                DetailSection("Dose & Duration", "2g/Liter every 7-10 days.")
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Where to Buy:", fontWeight = FontWeight.Bold)
                Text("Available at Agrivet supplies or online stores like Shopee/Lazada.", fontSize = 14.sp)
                Text(
                    text = "Link to Shop",
                    fontSize = 14.sp,
                    color = Color(0xFF3ED47A),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun DetailSection(label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        Text(text = content, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LibraryScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun LeafDiseasesListScreenPreview() {
    LeafDiseasesListScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun LeafDiseaseDetailScreenPreview() {
    LeafDiseaseDetailScreen(rememberNavController(), "Anthracnose")
}

@Preview(showBackground = true)
@Composable
fun MedicinesListScreenPreview() {
    MedicinesListScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun MedicineDetailScreenPreview() {
    MedicineDetailScreen(rememberNavController(), "Mancozeb")
}
