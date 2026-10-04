package com.melon.meloscan.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.melon.meloscan.R
import com.melon.meloscan.model.RecommendedMedicineData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
    scanType: String,
    result: String,
    confidence: Int,
    medicine: String?,
    imageUri: String?
) {
    val context = LocalContext.current

    val recommendedMedicine =
        if (scanType == "Leaf Disease") {
            RecommendedMedicineData.getByDisease(result)
        } else {
            null
        }

    val scanRoute =
        if (scanType == "Leaf Disease") {
            "leaf_scan"
        } else {
            "fruit_scan"
        }

    val resultColor =
        if (scanType == "Leaf Disease") {
            Color(0xFFC62828)
        } else {
            Color(0xFF2E7D32)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (scanType == "Leaf Disease") {
                                "Disease Detection"
                            } else {
                                "Quality Evaluation"
                            },
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124)
                        )

                        Text(
                            text = "Scan Result",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!navController.popBackStack(scanRoute, false)) {
                                navController.navigate(scanRoute)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to scan",
                            tint = Color(0xFF202124)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF6F8F7)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        ) {

            // ================================================================
            // SCANNED IMAGE
            // ================================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                if (imageUri != null) {

                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Scanned watermelon leaf",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp
                                )
                            ),
                        contentScale = ContentScale.Fit
                    )

                } else {

                    Image(
                        painter = painterResource(
                            id = if (scanType == "Leaf Disease") {
                                R.drawable.watermelon
                            } else {
                                R.drawable.fruity
                            }
                        ),
                        contentDescription = "Scanned watermelon",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp
                                )
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================================================================
            // DETECTION RESULT
            // ================================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Detection Result",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = result,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = resultColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (scanType == "Leaf Disease") {
                            Color(0xFFFFEBEE)
                        } else {
                            Color(0xFFE8F5E9)
                        }
                    ) {
                        Text(
                            text = "$confidence% Confidence",
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================================================================
            // RECOMMENDED MANAGEMENT
            // ================================================================

            if (
                scanType == "Leaf Disease" &&
                recommendedMedicine != null
            ) {

                Text(
                    text = "Recommended Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(10.dp))

                /*
                 * One card containing all recommendation information.
                 */

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {

                        // ----------------------------------------------------
                        // MEDICINE
                        // ----------------------------------------------------

                        Text(
                            text = "Medicine",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.medicineName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // TYPE
                        // ----------------------------------------------------

                        Text(
                            text = "Type",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.type,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // DESCRIPTION
                        // ----------------------------------------------------

                        Text(
                            text = "Description",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.description,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // TARGET DISEASE
                        // ----------------------------------------------------

                        Text(
                            text = "Target Disease",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.targetDisease,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            color = Color(0xFFE5E7E5),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // OTHER MEDICINE OPTIONS
                        // ----------------------------------------------------

                        Text(
                            text = "Other Medicine Options",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.otherMedicine,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // LOW-COST MANAGEMENT
                        // ----------------------------------------------------

                        Text(
                            text = "Manual / Low-Cost Management",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.manualSolution,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            color = Color(0xFFE5E7E5),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ----------------------------------------------------
                        // AGRICULTURAL SOURCE
                        // ----------------------------------------------------

                        Text(
                            text = "Agricultural Source",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = recommendedMedicine.sourceOrganization,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = recommendedMedicine.sourceDescription,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ----------------------------------------------------
                        // MORE INFORMATION
                        // ----------------------------------------------------

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        recommendedMedicine.informationLink
                                    )
                                )

                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF2E7D32)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Text(
                                text = "More Information",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

            } else {

                // ============================================================
                // FALLBACK / QUALITY INFORMATION
                // ============================================================

                Text(
                    text = if (scanType == "Leaf Disease") {
                        "Recommended Management"
                    } else {
                        "Quality Analysis"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Text(
                        text = medicine
                            ?: "No specific recommendation available.",
                        modifier = Modifier.padding(18.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 21.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================================================================
            // SCAN ANOTHER
            // ================================================================

            Button(
                onClick = {
                    if (!navController.popBackStack(scanRoute, false)) {
                        navController.navigate(scanRoute)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3ED47A)
                )
            ) {
                Text(
                    text = "Scan Another",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ScanResultScreenPreview() {
    ScanResultScreen(
        navController = rememberNavController(),
        scanType = "Leaf Disease",
        result = "Downy Mildew",
        confidence = 92,
        medicine = "Fungicide with Mancozeb",
        imageUri = null
    )
}