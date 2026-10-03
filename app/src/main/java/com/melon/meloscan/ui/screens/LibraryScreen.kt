package com.melon.meloscan.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import com.melon.meloscan.utils.isInternetAvailable

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import com.melon.meloscan.R
import com.melon.meloscan.data.supabase.SupabaseClientProvider
import com.melon.meloscan.model.Resource
import com.melon.meloscan.ui.navigation.AppBottomBar
import com.melon.meloscan.ui.viewmodel.ResourceViewModel
import io.github.jan.supabase.storage.storage
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities


data class WatermelonDisease(
    val name: String,
    val scientificName: String,
    val family: String,
    val description: String,
    val damage: String,
    val cause: String,
    val management: String,
    val type: String,
    val partAffected: String,
    val imageRes: Int,
    val source: String
)

data class Medicine(
    val name: String,
    val type: String, // Systemic or Contact
    val description: String,
    val targetDisease: String,
    val application: String,
    val dose: String,
    val shopLink: String,
    val localInfo: String,
    val imageRes: Int
)

val WatermelonDiseases = listOf(
    WatermelonDisease(
        name = "Anthracnose",
        scientificName = "Colletotrichum orbiculare",
        family = "Pleosporaceae",
        description = "Common during warm, rainy seasons affecting all parts.",
        damage = "Small brown spots that create a 'shot-hole' appearance.",
        cause = "Fungal pathogen spread by rain and tools.",
        management = "Use disease-free seeds. 3-year rotation.",
        type = "Fungal",
        partAffected = "WHOLE",
        imageRes = R.drawable.anthracnose_of_watermelon_7,
        source = "https://philippine-agriculture.com/watermelon-pests"
    ),
    WatermelonDisease(
        name = "Watermelon Mosaic Virus",
        scientificName = "WMV (Potyvirus)",
        family = "Potyviridae",
        description = "A viral disease spread by aphids that causes stunting and severe leaf distortion.",
        damage = "Mosaic patterns (light and dark green mottling), crinkling or narrowing of leaves, and stunted growth.",
        cause = "Viral infection spread primarily by aphids (Aphis gossypii).",
        management = "Control aphid populations. Use reflective silver mulches. Eliminate weed hosts.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.mosaicvirus_1,
        source = "https://extension.upenn.edu/watermelon-mosaic-virus"
    ),
    WatermelonDisease(
        name = "Cucurbit Downy Mildew",
        scientificName = "Pseudoperonospora cubensis",
        family = "Peronosporaceae",
        description = "A highly destructive oomycete disease affecting cucurbits including watermelon.",
        damage = "Pale green to yellow angular spots on upper leaf surfaces. Purplish/grayish fuzzy growth on the underside during humid periods.",
        cause = "Water mold pathogen spread by wind-borne spores. Thrives in cool, wet, and humid conditions.",
        management = "Improve airflow, avoid overhead irrigation. Apply protectant and systemic fungicides.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.downey_mildew_1,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    WatermelonDisease(
        name = "Angular Leaf Spot",
        scientificName = "Pseudomonas syringae pv. lachrymans",
        family = "Pseudomonadaceae",
        description = "A bacterial disease causing angular lesions and tattered holes.",
        damage = "Small water-soaked gray spots confined by veins. Fruit may develop internal soft rot.",
        cause = "Bacterial pathogen spread by splashing water and tools.",
        management = "Use certified seeds. Avoid overhead irrigation.",
        type = "Bacterial",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.angular_leaf_spot,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    
    WatermelonDisease(
        name = "Bacterial Fruit Blotch",
        scientificName = "Acidovorax citrulli",
        family = "Comamonadaceae",
        description = "A devastating bacterial disease that can cause near-total yield loss in watermelon.",
        damage = "Large, dark-green water-soaked 'stains' on the fruit surface. Fruit eventually collapses into a soft rot.",
        cause = "Bacterial pathogen highly seed-borne.",
        management = "Use pathogen-free seeds. Greenhouse sanitation.",
        type = "Bacterial",
        partAffected = "FRUIT",
        imageRes = R.drawable.bacterialblotch,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Black Root Rot",
        scientificName = "Thielaviopsis basicola",
        family = "Ceratocystidaceae",
        description = "Soil-borne disease primarily affecting seedlings and transplants.",
        damage = "Blackened base of stem and roots. Wilting and stunted growth.",
        cause = "Fungal pathogen producing resilient resting spores in soil.",
        management = "Sanitize greenhouse equipment. Use clean transplant media.",
        type = "Fungal",
        partAffected = "ROOT",
        imageRes = R.drawable.black_root_rot_3,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    WatermelonDisease(
        name = "Brassica Downy Mildew",
        scientificName = "Hyaloperonospora brassicae",
        family = "Peronosporaceae",
        description = "Affects cabbage, broccoli, and other brassicas.",
        damage = "Small, angular yellow lesions on upper leaf surfaces. White-to-gray fuzzy growth on undersides.",
        cause = "Oomycete pathogen favored by cool and wet weather.",
        management = "Eliminate brassica weeds. Use treated seeds. Rotate crops.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.brassica,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Cercospora Leaf Spot",
        scientificName = "Cercospora citrullina",
        family = "Mycosphaerellaceae",
        description = "Foliar disease causing premature defoliation.",
        damage = "Circular spots with tan centers and dark borders.",
        cause = "Fungal pathogen favored by high humidity.",
        management = "Maintain field sanitation. Copper fungicides.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.cercospora_spot_1,
        source = "https://da.gov.ph/watermelon-production-guide"
    ),
    WatermelonDisease(
        name = "Chimera",
        scientificName = "Genetic Mutation",
        family = "N/A",
        description = "A spontaneous genetic mutation where different sectors of the plant have different genetic makeup.",
        damage = "Cosmetic variegation on leaves (yellow/white patches) or rind.",
        cause = "Random somatic mutation during cell division.",
        management = "No management required.",
        type = "Physiological",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.chimera_3,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Cross-stitch",
        scientificName = "Physiological Disorder",
        family = "N/A",
        description = "Rind lesions perpendicular to the fruit axis.",
        damage = "Small elliptical rind lesions. Cosmetic but can lead to cracking.",
        cause = "Likely related to environmental stress during fruit growth.",
        management = "Consistent irrigation. Reduce plant stress.",
        type = "Physiological",
        partAffected = "FRUIT",
        imageRes = R.drawable.cross_stitch_of_watermelon,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Damping-off",
        scientificName = "Pythium spp. / Rhizoctonia solani",
        family = "Pythiaceae / Ceratobasidiaceae",
        description = "Seedling collapse in cool, poorly drained soils.",
        damage = "Thinning and water-soaking of stem at soil line causing collapse.",
        cause = "Soil-borne pathogens favored by high moisture.",
        management = "Use treated seeds. Improve drainage.",
        type = "Fungal",
        partAffected = "STEM/ROOT",
        imageRes = R.drawable.damping_off_4,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    WatermelonDisease(
        name = "Dodder",
        scientificName = "Cuscuta spp.",
        family = "Convolvulaceae",
        description = "Parasitic leafless plant twining around vines.",
        damage = "Yellow stems wrap around hosts extracting nutrients, stunting growth.",
        cause = "Parasitic plant infestation via soil or seed.",
        management = "Scout early and destroy infested plants.",
        type = "Parasitic",
        partAffected = "STEM",
        imageRes = R.drawable.dodder_3,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Fusarium Wilt",
        scientificName = "Fusarium oxysporum f. sp. niveum",
        family = "Nectriaceae",
        description = "Soil-borne fungus clogging water-conducting vessels.",
        damage = "Dull green leaves wilting during the day. Reddish-brown stem interior.",
        cause = "Fungal pathogen surviving years in soil as spores.",
        management = "Use resistant varieties. Long rotations.",
        type = "Fungal",
        partAffected = "STEM/ROOT",
        imageRes = R.drawable.fusarium_wilt_2,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Grape Downy Mildew",
        scientificName = "Plasmopara viticola",
        family = "Plasmoparaceae",
        description = "A serious disease of grapevines, often compared with cucurbit downy mildew.",
        damage = "Yellow, translucent 'oil spots' on upper leaf surfaces. White, cottony growth on leaf undersides.",
        cause = "Oomycete pathogen requiring free water for infection. Overwinters in fallen leaves.",
        management = "Pruning for airflow. Remove leaf litter. Apply copper-based fungicides.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.cucumber_leaf_mildew,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Gummy Blight",
        scientificName = "Stagonosporopsis citrulli",
        family = "Pleosporaceae",
        description = "Affects the entire plant. Recognizable by the amber-colored gummy ooze from stem lesions.",
        damage = "Brown, wrinkled spots on leaf margins. Stems develop dry cankers that leak amber-colored gummy exudate.",
        cause = "Fungus that survives in soil and crop residue.",
        management = "Manage soil drainage to prevent waterlogging. Use certified clean seeds.",
        type = "Fungal",
        partAffected = "STEM/LEAF",
        imageRes = R.drawable.gummy_stem_blight_11,
        source = "https://plantpathology.ph/diseases/watermelon"
    ),
    WatermelonDisease(
        name = "Lightning Damage",
        scientificName = "Environmental Event",
        family = "N/A",
        description = "Sudden death of plants in a defined circular patch after a storm.",
        damage = "Scorched plants in a defined circular zone. Does not spread.",
        cause = "Direct or nearby lightning strike.",
        management = "No management possible.",
        type = "Environmental",
        partAffected = "WHOLE",
        imageRes = R.drawable.lightning_damage_5,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Manganese Toxicity",
        scientificName = "Nutritional Disorder",
        family = "N/A",
        description = "Rapid deterioration in highly acidic soils.",
        damage = "Tiny brown speckles on older leaves. Sudden wilting.",
        cause = "Soil pH below 5.5 making manganese toxic.",
        management = "Apply lime to raise soil pH.",
        type = "Nutritional",
        partAffected = "LEAF",
        imageRes = R.drawable.manganese_toxicity_watermelon_2,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    WatermelonDisease(
        name = "Onion Downy Mildew",
        scientificName = "Peronospora destructor",
        family = "Peronosporaceae",
        description = "A significant disease in onion production, included for study.",
        damage = "Pale yellow, elongated patches on leaves covered with grayish-violet fuzzy mold.",
        cause = "Oomycete pathogen requiring cool temperatures and over 95% humidity.",
        management = "Use disease-free sets. 4-5 year crop rotation.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.watermelon,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),
    WatermelonDisease(
        name = "Phytophthora Blight",
        scientificName = "Phytophthora capsici",
        family = "Peronosporaceae",
        description = "Oomycete disease causing rapid collapse and fruit rot.",
        damage = "Rapid wilting. Fruit covered in white 'powdered sugar' mold.",
        cause = "Oomycete spread by splashing rain and standing water.",
        management = "Improve drainage. Rotate with non-hosts.",
        type = "Fungal",
        partAffected = "WHOLE",
        imageRes = R.drawable.phytophthora_blight_41,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Powdery Mildew",
        scientificName = "Podosphaera xanthii",
        family = "Erysiphaceae",
        description = "Prevalent foliar disease causing white patches on leaves.",
        damage = "White talcum-powder-like patches. Leaves turn brown and papery. Causes sunscald.",
        cause = "Fungal pathogen spread by wind. Thrives in high humidity.",
        management = "Use resistant varieties. Apply systemic fungicides.",
        type = "Fungal",
        partAffected = "LEAF/STEM",
        imageRes = R.drawable.powdery_mildew_2,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Rind Necrosis",
        scientificName = "Bacterial/Physiological",
        family = "N/A",
        description = "Internal rind disorder rarely visible from the outside.",
        damage = "Hard, reddish-brown corky patches within the rind.",
        cause = "Triggered by stress and bacteria like Erwinia spp.",
        management = "Use less susceptible varieties. Avoid stress.",
        type = "Physiological",
        partAffected = "FRUIT",
        imageRes = R.drawable.rind_necrosis_2,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Root Knot Nematode",
        scientificName = "Meloidogyne spp.",
        family = "Meloidogynidae",
        description = "Parasitic roundworms attacking the root system.",
        damage = "Formation of galls (swollen knots) on roots. Stunted growth.",
        cause = "Nematode infestation prevalent in sandy soils.",
        management = "Crop rotation. Use resistant varieties.",
        type = "Parasitic",
        partAffected = "ROOT",
        imageRes = R.drawable.root_knot_nematode_3,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Sooty Mold",
        scientificName = "Capnodium spp.",
        family = "Capnodiaceae",
        description = "Black fungus growing on insect honeydew.",
        damage = "Black velvety coating on leaves blocking sunlight and photosynthesis.",
        cause = "Fungal growth on honeydew from aphids or whiteflies.",
        management = "Control sap-sucking insects. Wash mold off with water.",
        type = "Fungal",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.watermelon,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Spinach Downy Mildew",
        scientificName = "Peronospora effusa",
        family = "Peronosporaceae",
        description = "A destructive spinach disease, included for oomycete pattern comparison.",
        damage = "Irregular yellow patches on upper leaf surfaces. Blue-to-purple downy growth on the undersides.",
        cause = "Oomycete pathogen favored by cool, humid environments.",
        management = "Use resistant varieties. Practice crop rotation. Use drip irrigation.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.alternarialeaf,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Stagonosporopsis caricae",
        scientificName = "Stagonosporopsis caricae",
        family = "Didymellaceae",
        description = "Fungus associated with GSB that also infects papaya.",
        damage = "Leaf blighting and greasy, water-soaked 'Black Rot' on fruit.",
        cause = "Fungal pathogen with a wider host range including papaya.",
        management = "Strict sanitation. Avoid cross-contamination with papaya fields.",
        type = "Fungal",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.watermelon,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Stagonosporopsis citrulli",
        scientificName = "Stagonosporopsis citrulli",
        family = "Didymellaceae",
        description = "Primary fungus causing Gummy Stem Blight on watermelon.",
        damage = "Circular tan-to-brown spots on leaf margins. Stem lesions leak amber gummy fluid.",
        cause = "Fungal pathogen spread by rain-splashed conidia and wind ascospores.",
        management = "Manage drainage. Rotate crops. Use systemic fungicides.",
        type = "Fungal",
        partAffected = "STEM/LEAF/FRUIT",
        imageRes = R.drawable.gummy_stem_blight_11,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Stagonosporopsis cucurbitacearum",
        scientificName = "Stagonosporopsis cucurbitacearum",
        family = "Didymellaceae",
        description = "A causal agent of Gummy Stem Blight in temperate regions.",
        damage = "Brown spots on leaves and gummy exudate from stem cankers.",
        cause = "Fungal pathogen host-specific to cucurbits.",
        management = "Plow under crop residue. Use drip irrigation.",
        type = "Fungal",
        partAffected = "STEM/LEAF",
        imageRes = R.drawable.watermelon,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Target Cluster (Target Spot)",
        scientificName = "Corynespora cassiicola",
        family = "Corynesporascaceae",
        description = "Fungal disease known for concentric rings on lesions.",
        damage = "Dark circular spots with concentric rings. Canopy defoliation.",
        cause = "Fungal pathogen favored by high humidity.",
        management = "Improve drainage. Apply protective fungicides.",
        type = "Fungal",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.target_cluster_4,
        source = "https://plantvillage.psu.edu/topics/watermelon/infos"
    ),

    WatermelonDisease(
        name = "White Mold",
        scientificName = "Sclerotinia sclerotiorum",
        family = "Sclerotiniaceae",
        description = "Causes a watery soft rot and characteristic white cottony growth.",
        damage = "White fluffy growth with black sclerotia. Plant collapse.",
        cause = "Fungal pathogen favored by cool, moist conditions.",
        management = "Deep plowing. Wide plant spacing.",
        type = "Fungal",
        partAffected = "STEM/FRUIT",
        imageRes = R.drawable.white_mold_1,
        source = "https://ag.purdue.edu/department/arge/swpap/watermelon-diseases.html"
    ),
    WatermelonDisease(
        name = "Zucchini Yellow Mosaic Virus",
        scientificName = "ZYMV (Potyvirus)",
        family = "Potyviridae",
        description = "Viral disease spread by aphids causing distortion.",
        damage = "Yellow mosaic patterns, leaf distortion (shoestring effect), and knobby/warty unmarketable fruit.",
        cause = "Potyvirus transmitted rapidly by aphids.",
        management = "Use reflective mulches. Eliminate weed hosts.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.zucchini_yellow_mosaic_wam_4,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    )
).sortedBy { it.name }




val MedicinesData = listOf(
    Medicine(
        name = "Amistar Top",
        type = "Systemic",
        description = "A premium systemic fungicide containing Azoxystrobin and Difenoconazole for curative action.",
        targetDisease = "Anthracnose, Gummy Stem Blight, Powdery Mildew, Target Spot",
        application = "Foliar spray. Absorbed by the plant to provide internal protection.",
        dose = "10-15ml per 16L of water. Rotate with contact fungicides.",
        shopLink = "https://shopee.ph/search?keyword=amistar%20top",
        localInfo = "Commonly stocked at Breeders Agrivet (Tagbilaran).",
        imageRes = R.drawable.amistar
    ),
    Medicine(
        name = "Dithane M-45 (Mancozeb)",
        type = "Contact",
        description = "A broad-spectrum protective fungicide effective against a wide range of fungal diseases.",
        targetDisease = "Downy Mildew, Anthracnose, Leaf Spots",
        application = "Foliar spray. Ensure thorough coverage of both leaf surfaces.",
        dose = "30-50g per 16L of water every 7-10 days.",
        shopLink = "https://shopee.ph/search?keyword=dithane%20m-45",
        localInfo = "Available at Fortune Agrivet (Tagbilaran) and Pacifica Agrivet (Cogon).",
        imageRes = R.drawable.dithane
    ),
    Medicine(
        name = "Kocide 2000",
        type = "Contact (Copper Hydroxide)",
        description = "A powerful bactericide and fungicide used to suppress bacterial outbreaks.",
        targetDisease = "Bacterial Fruit Blotch, Angular Leaf Spot",
        application = "Preventative foliar spray starting before first female bloom.",
        dose = "2.0-2.5 tbsp per 16L of water.",
        shopLink = "https://shopee.ph/search?keyword=kocide%202000",
        localInfo = "Pacifica Agrivet and Breeders Agrivet (Tagbilaran).",
        imageRes = R.drawable.kocide
    ),
    Medicine(
        name = "Luna Experience",
        type = "Systemic (Fluopyram + Tebuconazole)",
        description = "Advanced dual-action fungicide for comprehensive control of tough fungal diseases.",
        targetDisease = "Gummy Stem Blight, Powdery Mildew, Anthracnose",
        application = "Foliar spray with good canopy penetration.",
        dose = "10-12ml per 16L of water.",
        shopLink = "https://shopee.ph/search?keyword=luna%20experience",
        localInfo = "Check Fortune Agrivet or Nutrimart for availability.",
        imageRes = R.drawable.luna
    ),
    Medicine(
        name = "Nordox 50 WP",
        type = "Contact (Copper Oxide)",
        description = "Natural copper-based fungicide and bactericide. Provides long-lasting protection.",
        targetDisease = "Bacterial Leaf Spot, Anthracnose, Fruit Blotch",
        application = "Spray early morning or late afternoon to avoid leaf burn.",
        dose = "1.5-2.0 tbsp per 16L of water.",
        shopLink = "https://shopee.ph/search?keyword=nordox%2050%20wp",
        localInfo = "Sold at V-Agri and Pacifica Agrivet (Tagbilaran).",
        imageRes = R.drawable.nordox
    ),
    Medicine(
        name = "Ridomil Gold",
        type = "Systemic (Metalaxyl-M)",
        description = "Highly effective systemic fungicide specifically for oomycete diseases like Downy Mildew.",
        targetDisease = "Downy Mildew, Phytophthora Blight, Damping-off",
        application = "Foliar spray or soil drench for damping-off.",
        dose = "40-50g per 16L of water.",
        shopLink = "https://shopee.ph/search?keyword=ridomil%20gold",
        localInfo = "Available at most major agrivet stores in Tagbilaran.",
        imageRes = R.drawable.ridomel
    )
).sortedBy { it.name }

// LEAF DISEASES = LOCAL
// MEDICINES     = LOCAL
// RESOURCES     = SUPABASE


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {

    val context = LocalContext.current

    val resourceViewModel: ResourceViewModel = viewModel()

    val resources by resourceViewModel.resources.collectAsState()

    val isLoading by resourceViewModel.isLoading.collectAsState()

    val error by resourceViewModel.error.collectAsState()

    var isOnline by remember {
        mutableStateOf(
            isInternetAvailable(context)
        )
    }

    var displayedResources by remember {
        mutableStateOf(emptyList<Resource>())
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(isLoading, resources, refreshKey) {

        if (!isLoading && resources.isNotEmpty()) {
            displayedResources = resources
                .shuffled()
                .take(5)
        }
    }

    LaunchedEffect(Unit) {

        isOnline = isInternetAvailable(context)

        if (isOnline) {
            resourceViewModel.loadResources()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Library",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentScreen = "Library"
            )
        },
        containerColor = Color.White
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp)
        ) {
            
            // LEAF DISEASES

            item {
                LibraryCard(
                    title = "Leaf Diseases",
                    subtitle = "Browse common watermelon leaf diseases",
                    iconRes = R.drawable.leaflogo,
                    onClick = {
                        navController.navigate("library_diseases_list")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // MEDICINES

            item {
                LibraryCard(
                    title = "Medicines",
                    subtitle = "Treatment and management options",
                    iconRes = R.drawable.medlogo,
                    onClick = {
                        navController.navigate("library_medicines_list")
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // RESOURCES

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Resources",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {

                            isOnline = isInternetAvailable(context)

                            if (isOnline && !isLoading) {
                                refreshKey++
                                resourceViewModel.loadResources()
                            }
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Resources"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
// RESOURCE LOADING

            if (!isOnline) {

                // OFFLINE

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = "No Internet connection",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Connect to the Internet to view the latest Resources.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(
                                onClick = {

                                    isOnline =
                                        isInternetAvailable(context)

                                    if (isOnline) {
                                        resourceViewModel.loadResources()
                                    }
                                }
                            ) {

                                Text("Try Again")
                            }
                        }
                    }
                }

            } else if (isLoading) {

                // LOADING

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

            } else if (error != null) {

                // SUPABASE ERROR

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = "Unable to load resources",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "The resources could not be loaded. Please try again.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(
                                onClick = {

                                    isOnline =
                                        isInternetAvailable(context)

                                    if (isOnline) {
                                        resourceViewModel.loadResources()
                                    }
                                }
                            ) {

                                Text("Try Again")
                            }
                        }
                    }
                }

            } else if (resources.isEmpty()) {

                // NO RESOURCES

                item {

                    Text(
                        text = "No resources are currently available.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(
                            vertical = 20.dp
                        )
                    )
                }

            } else {

                // SUPABASE RESOURCES

                items(
                    items = displayedResources,
                    key = { resource -> resource.id }
                ) { resource ->

                    ResourceCard(
                        resource = resource
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }
        }
    }
}

// LIBRARY CARD

// LIBRARY CARD


@Composable
fun LibraryCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7F9)
        )
    ) {

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column {

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// RESOURCE CARD

@Composable
fun ResourceCard(resource: Resource) {

    val context = LocalContext.current

    val imageUrl = if (resource.imagePath.isNullOrBlank()) {
        null
    } else {
        SupabaseClientProvider.client
            .storage
            .from("resource-images")
            .publicUrl(resource.imagePath!!)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(resource.resourceUrl)
                )

                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFEEEEEE)
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (imageUrl != null) {

                AsyncImage(
                    model = imageUrl,
                    contentDescription = resource.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )

            } else {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            Color(0xFFF5F5F5)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = resource.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3ED47A)
                )

                Text(
                    text = resource.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Text(
                    text = "Visit Resource",
                    fontSize = 11.sp,
                    color = Color(0xFF3ED47A),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// LEAF DISEASES LIST SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafDiseasesListScreen(navController: NavController) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    var isSearchActive by remember {
        mutableStateOf(false)
    }

    val filteredDiseases = if (searchQuery.isEmpty()) {
        WatermelonDiseases
    } else {
        WatermelonDiseases.filter {
            it.name.contains(
                searchQuery,
                ignoreCase = true
            )
        }
    }

    Scaffold(
        topBar = {

            TopAppBar(

                title = {

                    if (isSearchActive) {

                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            placeholder = {
                                Text("Search diseases...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true
                        )

                    } else {

                        Text(
                            "Leaf Diseases",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            if (isSearchActive) {

                                isSearchActive = false
                                searchQuery = ""

                            } else {

                                navController.popBackStack()
                            }
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    if (isSearchActive) {

                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {

                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }

                    } else {

                        IconButton(
                            onClick = {
                                isSearchActive = true
                            }
                        ) {

                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9)),

            contentPadding = PaddingValues(
                bottom = 20.dp
            )
        ) {

            items(filteredDiseases) { disease ->

                LibraryListItem(
                    name = disease.name,
                    subtitle = disease.scientificName,
                    category = disease.partAffected,
                    tag = disease.type,
                    imageRes = disease.imageRes,
                    onClick = {
                        navController.navigate(
                            "library_disease_detail?name=${disease.name}"
                        )
                    }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )
            }
        }
    }
}



// LIBRARY LIST ITEM

@Composable
fun LibraryListItem(
    name: String,
    subtitle: String,
    category: String,
    tag: String,
    imageRes: Int,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                onClick()
            }
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Gray,
                modifier = Modifier.padding(
                    vertical = 4.dp
                )
            )

            Row {

                TagBadge(
                    text = category,
                    color = Color(0xFF4CAF50)
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                TagBadge(
                    text = tag.uppercase(),
                    color = Color.Black
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(75.dp)
                .clip(
                    RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )
    }
}


// TAG BADGE

@Composable
fun TagBadge(
    text: String,
    color: Color
) {

    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(4.dp)
            )
            .background(color)
            .padding(
                horizontal = 6.dp,
                vertical = 2.dp
            )
    ) {

        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// LEAF DISEASE DETAIL SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafDiseaseDetailScreen(
    navController: NavController,
    diseaseName: String
) {

    val disease =
        WatermelonDiseases.find {
            it.name == diseaseName
        } ?: WatermelonDiseases[0]

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        disease.name.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {

            item {

                Image(
                    painter = painterResource(
                        disease.imageRes
                    ),
                    contentDescription = disease.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Image source: ${disease.source}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.padding(
                            bottom = 20.dp
                        )
                    )

                    DetailInfoSection(
                        "COMMON NAME",
                        disease.name
                    )

                    DetailInfoSection(
                        "SCIENTIFIC NAME",
                        disease.scientificName,
                        isItalic = true
                    )

                    DetailInfoSection(
                        "ORDER OR FAMILY",
                        disease.family
                    )

                    DetailInfoSection(
                        "DESCRIPTION",
                        disease.description
                    )

                    DetailInfoSection(
                        "DAMAGE CHARACTERISTICS",
                        disease.damage
                    )

                    DetailInfoSection(
                        "CAUSE",
                        disease.cause
                    )

                    DetailInfoSection(
                        "MANAGEMENT PRACTICE",
                        disease.management
                    )

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}



// DETAIL INFO SECTION

@Composable
fun DetailInfoSection(
    label: String,
    content: String,
    isItalic: Boolean = false
) {

    Column(
        modifier = Modifier.padding(
            bottom = 20.dp
        )
    ) {

        Box(
            modifier = Modifier
                .background(
                    Color(0xFFE8F5E9),
                    RoundedCornerShape(4.dp)
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 2.dp
                )
        ) {

            Text(
                text = label,
                color = Color(0xFF2E7D32),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = content,
            fontSize = 15.sp,
            color = Color.Black,
            fontStyle =
                if (isItalic)
                    FontStyle.Italic
                else
                    FontStyle.Normal,
            lineHeight = 22.sp
        )

        HorizontalDivider(
            modifier = Modifier.padding(
                top = 16.dp
            ),
            thickness = 0.5.dp,
            color = Color(0xFFEEEEEE)
        )
    }
}



// MEDICINES LIST SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesListScreen(navController: NavController) {

    var searchQuery by remember {
        mutableStateOf("")
    }

    var isSearchActive by remember {
        mutableStateOf(false)
    }

    val filteredMedicines = if (searchQuery.isEmpty()) {

        MedicinesData

    } else {

        MedicinesData.filter {
            it.name.contains(
                searchQuery,
                ignoreCase = true
            )
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    if (isSearchActive) {

                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            placeholder = {
                                Text("Search medicines...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true
                        )

                    } else {

                        Text(
                            "Medicines",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            if (isSearchActive) {

                                isSearchActive = false
                                searchQuery = ""

                            } else {

                                navController.popBackStack()
                            }
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    if (isSearchActive) {

                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {

                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }

                    } else {

                        IconButton(
                            onClick = {
                                isSearchActive = true
                            }
                        ) {

                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F9F9))
        ) {

            items(filteredMedicines) { medicine ->

                LibraryListItem(
                    name = medicine.name,
                    subtitle = medicine.description,
                    category = "MEDICINE",
                    tag = medicine.type,
                    imageRes = medicine.imageRes,
                    onClick = {
                        navController.navigate(
                            "library_medicine_detail?name=${medicine.name}"
                        )
                    }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )
            }
        }
    }
}



// MEDICINE DETAIL SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    navController: NavController,
    medicineName: String
) {

    val medicine =
        MedicinesData.find {
            it.name == medicineName
        } ?: MedicinesData[0]

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        medicine.name.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {

            item {

                Image(
                    painter = painterResource(
                        medicine.imageRes
                    ),
                    contentDescription = medicine.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    DetailInfoSection(
                        "NAME",
                        medicine.name
                    )

                    DetailInfoSection(
                        "TYPE",
                        medicine.type
                    )

                    DetailInfoSection(
                        "DESCRIPTION",
                        medicine.description
                    )

                    DetailInfoSection(
                        "TARGET DISEASE",
                        medicine.targetDisease
                    )

                    DetailInfoSection(
                        "APPLICATION PATTERN",
                        medicine.application
                    )

                    DetailInfoSection(
                        "DOSE & DURATION",
                        medicine.dose
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        "WHERE TO BUY:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = medicine.localInfo,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row {

                        TextButton(
                            onClick = {
                                /* TODO */
                            }
                        ) {

                            Text(
                                "Shopee Link",
                                color = Color(0xFF3ED47A),
                                textDecoration =
                                    TextDecoration.Underline
                            )
                        }

                        TextButton(
                            onClick = {
                                /* TODO */
                            }
                        ) {

                            Text(
                                "Lazada Link",
                                color = Color(0xFF3ED47A),
                                textDecoration =
                                    TextDecoration.Underline
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}


// PREVIEWS

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LibraryScreen(
        rememberNavController()
    )
}

@Preview(showBackground = true)
@Composable
fun LeafDiseasesListScreenPreview() {
    LeafDiseasesListScreen(
        rememberNavController()
    )
}

@Preview(showBackground = true)
@Composable
fun LeafDiseaseDetailScreenPreview() {
    LeafDiseaseDetailScreen(
        rememberNavController(),
        "Downy Mildew"
    )
}

@Preview(showBackground = true)
@Composable
fun MedicinesListScreenPreview() {
    MedicinesListScreen(
        rememberNavController()
    )
}

@Preview(showBackground = true)
@Composable
fun MedicineDetailScreenPreview() {
    MedicineDetailScreen(
        rememberNavController(),
        "Mancozeb"
    )
}