package com.melon.meloscan.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

import com.melon.meloscan.R
import com.melon.meloscan.data.supabase.SupabaseClientProvider
import com.melon.meloscan.model.Resource
import com.melon.meloscan.ui.navigation.AppBottomBar
import com.melon.meloscan.ui.viewmodel.ResourceViewModel
import io.github.jan.supabase.storage.storage


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
        family = "Glomerellaceae",
        description = "A destructive fungal disease affecting leaves, stems, and fruit, especially under warm and wet conditions.",
        damage = "Dark brown to black leaf lesions, stem lesions, and sunken brown-to-black fruit spots that may crack and decay into the flesh.",
        cause = "Fungal pathogen spread through infected seed, crop residue, rain splash, wind-driven rain, tools, workers, and wet plant contact.",
        management = "Use disease-free seed and resistant varieties when available. Practice crop rotation, sanitation, and avoid working plants when wet.",
        type = "Fungal",
        partAffected = "WHOLE",
        imageRes = R.drawable.anthracnose_of_watermelon_7,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Bacterial Fruit Blotch",
        scientificName = "Acidovorax citrulli",
        family = "Comamonadaceae",
        description = "A serious bacterial disease of watermelon that is particularly damaging to developing fruit.",
        damage = "Dark, water-soaked lesions develop on the fruit surface, enlarge rapidly, crack, and may progress to fruit breakdown.",
        cause = "Seedborne bacterial pathogen spread by rain splash, irrigation water, workers, equipment, and wet conditions.",
        management = "Use pathogen-free tested seed and healthy transplants. Sanitize greenhouse facilities, rotate crops, remove volunteers, and avoid overhead irrigation.",
        type = "Bacterial",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.bacterialblotch,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Black Root Rot",
        scientificName = "Thielaviopsis basicola",
        family = "Ceratocystidaceae",
        description = "A soilborne root disease primarily affecting seedlings and young transplants.",
        damage = "Roots and lower hypocotyl become black, followed by stunting, poor growth, wilting, and possible seedling death.",
        cause = "Soilborne fungal pathogen favored by susceptible seedlings and unfavorable soil conditions.",
        management = "Use clean transplant media, sanitize equipment, improve drainage, and avoid planting into contaminated media or soil.",
        type = "Fungal",
        partAffected = "ROOT",
        imageRes = R.drawable.black_root_rot_3,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/root-rots-damping-off/"
    ),
    WatermelonDisease(
        name = "Blossom-End Rot",
        scientificName = "Calcium/Water-Balance Disorder",
        family = "N/A",
        description = "A physiological fruit disorder associated with inadequate calcium delivery to developing fruit.",
        damage = "The blossom end becomes pale green, brown, or black and develops a sunken, dry or rotting area.",
        cause = "Calcium deficiency in developing fruit commonly associated with irregular soil moisture, drought stress, or fluctuating wet and dry conditions.",
        management = "Maintain uniform soil moisture, properly manage irrigation, soil-test and lime according to soil requirements, and avoid severe water stress.",
        type = "Physiological",
        partAffected = "FRUIT",
        imageRes = R.drawable.blossom_end_rot_watermelon,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Cercospora Leaf Spot",
        scientificName = "Cercospora citrullina",
        family = "Mycosphaerellaceae",
        description = "A fungal foliar disease that can cause significant leaf spotting and defoliation.",
        damage = "Small round dark-brown to black leaf spots with pale or white centers and surrounding yellow halos.",
        cause = "Fungal pathogen surviving on old crop debris and spreading by windborne and rain-splashed spores.",
        management = "Maintain field sanitation, remove infected residue, rotate crops, and use appropriate protective fungicides when needed.",
        type = "Fungal",
        partAffected = "LEAF",
        imageRes = R.drawable.cercospora_spot_1,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Charcoal Rot",
        scientificName = "Macrophomina phaseolina",
        family = "Botryosphaeriaceae",
        description = "A soilborne fungal disease that becomes more severe when watermelon plants experience heat or water stress.",
        damage = "Yellowing and death of crown leaves, water-soaked stem lesions, amber gum, dry brown stem tissue, and black microsclerotia.",
        cause = "Soilborne fungus surviving as microsclerotia and favored by high temperatures, water stress, heavy fruit load, and repeated melon production.",
        management = "Avoid drought stress, rotate with nonhost crops, manage soil salinity, destroy infected plant residue, and maintain good field sanitation.",
        type = "Fungal",
        partAffected = "STEM/ROOT/LEAF",
        imageRes = R.drawable.charcoal_rot_watermelon,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/charcoal-rot/"
    ),
    WatermelonDisease(
        name = "Chimera",
        scientificName = "Genetic Mutation",
        family = "N/A",
        description = "A spontaneous genetic mutation causing different sectors of the plant to have different genetic characteristics.",
        damage = "Cosmetic yellow or white variegation may appear on leaves or fruit rind.",
        cause = "Random somatic mutation during plant cell division.",
        management = "No disease management is required.",
        type = "Physiological",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.chimera_3,
        source = "https://extension.okstate.edu/fact-sheets/watermelon-diseases"
    ),
    WatermelonDisease(
        name = "Cross-Stitch",
        scientificName = "Physiological Disorder",
        family = "N/A",
        description = "A rind disorder characterized by lesions occurring perpendicular to the long axis of the fruit.",
        damage = "Small elliptical rind lesions that may become more visible as the fruit develops and may predispose the rind to cracking.",
        cause = "Associated with environmental stress during fruit development.",
        management = "Maintain consistent irrigation and reduce environmental stress during fruit development.",
        type = "Physiological",
        partAffected = "FRUIT",
        imageRes = R.drawable.cross_stitch_of_watermelon,
        source = "https://growhow.eastwestseed.com/crop-guide-template/watermelon"
    ),
    WatermelonDisease(
        name = "Cucurbit Aphid-Borne Yellows",
        scientificName = "Cucurbit aphid-borne yellows virus",
        family = "Solemoviridae",
        description = "A virus disease transmitted primarily by aphids that causes yellowing and reduced plant performance.",
        damage = "Older leaves become yellow, thick, and leathery while major veins remain green. Growth and yield may decline.",
        cause = "Persistent virus transmitted efficiently by the melon aphid and less efficiently by the green peach aphid.",
        management = "Reduce aphid populations and virus reservoirs, manage weeds, use reflective mulches where appropriate, and remove heavily infected plants.",
        type = "Viral",
        partAffected = "LEAF/WHOLE",
        imageRes = R.drawable.cucurbit_aphid_borne_yellows,
        source = "https://ipm.ucanr.edu/legacy_assets/pdf/pmg/pmgcucurbits.pdf"
    ),
    WatermelonDisease(
        name = "Cucurbit Downy Mildew",
        scientificName = "Pseudoperonospora cubensis",
        family = "Peronosporaceae",
        description = "A rapidly developing oomycete disease primarily affecting watermelon foliage.",
        damage = "Yellow to brown leaf lesions that expand and coalesce, causing leaf curling and a burned or defoliated appearance.",
        cause = "Oomycete pathogen spread by airborne and rain-splashed spores and favored by humid conditions.",
        management = "Monitor fields regularly, improve airflow, avoid prolonged leaf wetness, and apply appropriate fungicides when disease risk is high.",
        type = "Oomycete",
        partAffected = "LEAF",
        imageRes = R.drawable.downymildew,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Cucurbit Leaf Crumple Virus",
        scientificName = "Cucurbit leaf crumple virus (CuLCrV)",
        family = "Geminiviridae",
        description = "A whitefly-transmitted virus that affects watermelon and other cucurbits.",
        damage = "Yellowing, leaf distortion, curling, crumpling, stunting, and reduced fruit quality.",
        cause = "Virus transmitted by Bemisia tabaci whiteflies.",
        management = "Control whitefly populations, remove infected plants and crop residues, manage weed hosts, and use healthy transplants.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.cucurbit_leaf_crumple,
        source = "https://ask.ifas.ufl.edu/publication/IN871"
    ),
    WatermelonDisease(
        name = "Damping-Off",
        scientificName = "Pythium spp. / Fusarium spp. / Rhizoctonia spp.",
        family = "Pythiaceae / Nectriaceae / Ceratobasidiaceae",
        description = "A seedling disease complex that causes collapse before or shortly after emergence.",
        damage = "Water-soaked or discolored tissue at or below the soil line followed by seedling collapse and death.",
        cause = "Soilborne fungi and oomycetes favored by cool, wet soil and excessive moisture.",
        management = "Use treated disease-free seed, warm and well-drained planting media, proper irrigation, and clean transplant facilities.",
        type = "Fungal/Oomycete",
        partAffected = "STEM/ROOT",
        imageRes = R.drawable.damping_off_4,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Fusarium Wilt",
        scientificName = "Fusarium oxysporum f. sp. niveum",
        family = "Nectriaceae",
        description = "A persistent soilborne vascular disease that can cause severe wilt and plant death.",
        damage = "Progressive wilting, runner-by-runner collapse, brown to red vascular tissue, root discoloration, and sometimes crown rot.",
        cause = "Fungal pathogen surviving in soil and spreading through contaminated soil, water, equipment, seed, and plant material.",
        management = "Use resistant varieties, long crop rotations, clean seed, sanitation, and avoid moving infested soil between fields.",
        type = "Fungal",
        partAffected = "STEM/ROOT/LEAF",
        imageRes = R.drawable.fusarium_wilt_2,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Gummy Stem Blight",
        scientificName = "Stagonosporopsis cucurbitacearum",
        family = "Didymellaceae",
        description = "A major fungal disease complex of watermelon affecting foliage, stems, seedlings, and occasionally fruit.",
        damage = "Brown to black leaf spots, stem cankers, amber gummy exudate, seedling blight, and occasional fruit rot.",
        cause = "Stagonosporopsis fungi surviving on infected crop debris and seed and spreading by rain splash, runoff, and airborne spores.",
        management = "Use clean treated seed, rotate crops, destroy infected residue, avoid working wet vines, improve drainage, and rotate fungicide modes of action.",
        type = "Fungal",
        partAffected = "STEM/LEAF/FRUIT",
        imageRes = R.drawable.gummy_stem_blight_11,
        source = "https://content.ces.ncsu.edu/gummy-stem-blight-and-phoma-blight-on-cucurbits"
    ),
    WatermelonDisease(
        name = "Watermelon Mosaic Virus",
        scientificName = "Watermelon mosaic virus (WMV)",
        family = "Potyviridae",
        description = "A widespread viral disease of watermelon that can cause severe leaf distortion, stunting, and fruit abnormalities.",
        damage = "Light and dark green mosaic patterns, mottled and distorted leaves, reduced leaf size, shortened internodes, plant stunting, and malformed or bumpy fruit.",
        cause = "Virus transmitted primarily by aphids, including the melon aphid and green peach aphid. The virus can also be maintained in infected weed hosts.",
        management = "Control aphid populations, remove infected plants and weed hosts, use reflective mulch where appropriate, maintain field sanitation, and use healthy planting material.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.mosaicvirus_1,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/potyviruses/"
    ),
    WatermelonDisease(
        name = "Measles",
        scientificName = "Guttation/Water-Soaking Disorder",
        family = "N/A",
        description = "An abiotic disorder of cucurbits associated with water-soaking injury and excessive soil moisture.",
        damage = "Small 2–4 mm water-soaked lesions develop on leaves, stems, and fruit and later develop tan centers.",
        cause = "Guttation and water-soaking injury, particularly under wet soil conditions.",
        management = "Avoid excessive irrigation and reduce irrigation during conditions favorable for the disorder.",
        type = "Physiological",
        partAffected = "LEAF/STEM/FRUIT",
        imageRes = R.drawable.measles_watermelon,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/measles/"
    ),
    WatermelonDisease(
        name = "Papaya Ringspot Virus-W",
        scientificName = "Papaya ringspot virus type W (PRSV-W)",
        family = "Potyviridae",
        description = "An aphid-transmitted potyvirus that infects watermelon and produces mosaic symptoms.",
        damage = "Mosaic mottling, leaf distortion, reduced growth, shortened internodes, and mottled or bumpy fruit.",
        cause = "Virus transmitted rapidly by winged aphids after feeding on infected plants or weed hosts.",
        management = "Control virus reservoirs and weeds, use reflective mulch where appropriate, remove infected plants, and reduce aphid movement into the crop.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.papaya_ringspot_watermelon,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/potyviruses/"
    ),
    WatermelonDisease(
        name = "Phytophthora Blight and Fruit Rot",
        scientificName = "Phytophthora capsici",
        family = "Peronosporaceae",
        description = "A destructive oomycete disease favored by excessive moisture and prolonged wet conditions.",
        damage = "Rapid wilting, crown and stem infection, greasy fruit lesions, fruit rot, and white mold-like growth on infected tissue.",
        cause = "Phytophthora capsici spread through wet soil, standing water, rain splash, and contaminated irrigation water.",
        management = "Improve drainage, avoid standing water, use long rotations, avoid contaminated fields, and use appropriate fungicide programs when necessary.",
        type = "Oomycete",
        partAffected = "WHOLE",
        imageRes = R.drawable.phytophthora_blight_41,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Powdery Mildew",
        scientificName = "Podosphaera xanthii",
        family = "Erysiphaceae",
        description = "A fungal foliar disease that is less common on watermelon than on some other cucurbits but can reduce canopy function.",
        damage = "Pale yellow areas followed by white powdery fungal growth, leaf chlorosis, browning, and papery leaves.",
        cause = "Powdery mildew fungi producing wind-dispersed spores and developing under favorable environmental conditions.",
        management = "Use resistant varieties when available, monitor foliage, maintain good plant growth, and apply appropriate fungicides when needed.",
        type = "Fungal",
        partAffected = "LEAF/STEM",
        imageRes = R.drawable.powdery_mildew_2,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/powdery-mildew/"
    ),
    WatermelonDisease(
        name = "Rind Necrosis",
        scientificName = "Several bacterial species",
        family = "N/A",
        description = "A fruit disorder characterized by internal necrosis of the watermelon rind.",
        damage = "Light-brown, dry, corky areas develop inside the rind and may enlarge and merge without usually extending into the flesh.",
        cause = "The exact cause and dissemination process remain incompletely understood; several bacterial species have been associated with the condition.",
        management = "Use less susceptible watermelon varieties and maintain good crop conditions that reduce plant stress.",
        type = "Bacterial/Physiological",
        partAffected = "FRUIT",
        imageRes = R.drawable.rind_necrosis_2,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Root Knot Nematode",
        scientificName = "Meloidogyne spp.",
        family = "Meloidogynidae",
        description = "A major soilborne nematode pest attacking watermelon roots.",
        damage = "Root galls or knots, reduced root function, stunting, chlorosis, wilting, and reduced fruit yield and quality.",
        cause = "Plant-parasitic Meloidogyne nematodes invading roots and establishing feeding sites.",
        management = "Use crop rotation with suitable nonhosts, resistant cultivars when available, sanitation, soil solarization where practical, and nematode monitoring.",
        type = "Nematode",
        partAffected = "ROOT",
        imageRes = R.drawable.root_knot_nematode_3,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/nematodes/"
    ),
    WatermelonDisease(
        name = "Speckle or Moonspots",
        scientificName = "Unknown",
        family = "N/A",
        description = "A noninfectious watermelon condition characterized by small circular spots on leaves and fruit.",
        damage = "Small white or yellow circular spots on leaves and fruit that may reduce cosmetic quality.",
        cause = "Unknown; the condition does not appear to be caused by a typical infectious pathogen and is considered heritable.",
        management = "No established disease control is available.",
        type = "Physiological",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.speckle_moonspots,
        source = "https://ask.ifas.ufl.edu/publication/PG060"
    ),
    WatermelonDisease(
        name = "Verticillium Wilt",
        scientificName = "Verticillium dahliae",
        family = "Plectosphaerellaceae",
        description = "A soilborne vascular wilt disease that can affect cucurbits.",
        damage = "Progressive yellowing and wilting beginning on crown leaves, followed by runner decline and possible plant death.",
        cause = "Verticillium dahliae survives in soil as microsclerotia and infects roots before colonizing water-conducting tissue.",
        management = "Use tolerant or resistant varieties where available, rotate crops, avoid highly infested fields, and manage soilborne inoculum.",
        type = "Fungal",
        partAffected = "STEM/ROOT/LEAF",
        imageRes = R.drawable.verticillium_wilt_watermelon,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/verticillium-wilt/"
    ),
    WatermelonDisease(
        name = "Watermelon Mosaic Virus",
        scientificName = "Watermelon mosaic virus (WMV)",
        family = "Potyviridae",
        description = "A widespread aphid-transmitted virus causing mosaic symptoms and plant growth abnormalities.",
        damage = "Light and dark green mosaic patterns, leaf distortion, reduced leaf size, stunting, and malformed or bumpy fruit.",
        cause = "Virus transmitted by multiple aphid species, including the melon aphid and green peach aphid.",
        management = "Manage aphid vectors and weed hosts, use reflective mulch where appropriate, remove infected plants, and use healthy planting material.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.mosaicvirus_1,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/potyviruses/"
    ),
    WatermelonDisease(
        name = "White Mold",
        scientificName = "Sclerotinia sclerotiorum",
        family = "Sclerotiniaceae",
        description = "A fungal disease capable of infecting cucurbit stems and fruit under moist conditions.",
        damage = "Water-soaked tissue followed by white cottony fungal growth, black sclerotia, stem death, and fruit rot.",
        cause = "Soilborne fungus producing long-lived sclerotia and favored by prolonged moisture.",
        management = "Improve air circulation, avoid excessive irrigation, maintain field sanitation, rotate crops, and reduce prolonged plant wetness.",
        type = "Fungal",
        partAffected = "STEM/FRUIT",
        imageRes = R.drawable.white_mold_1,
        source = "https://ipm.ucanr.edu/home-and-landscape/white-mold/"
    ),
    WatermelonDisease(
        name = "Zucchini Yellow Mosaic Virus",
        scientificName = "Zucchini yellow mosaic virus (ZYMV)",
        family = "Potyviridae",
        description = "A rapidly aphid-transmitted potyvirus affecting watermelon and other cucurbits.",
        damage = "Yellow mosaic patterns, leaf distortion and narrowing, shortened internodes, stunting, and knobby or malformed fruit.",
        cause = "Virus transmitted by several aphid species.",
        management = "Manage aphid vectors and weed hosts, use reflective mulches, remove infected plants, and use healthy planting material.",
        type = "Viral",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.zucchini_yellow_mosaic_wam_4,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/potyviruses/"
    ),
    WatermelonDisease(
        name = "Beet Armyworm",
        scientificName = "Spodoptera exigua",
        family = "Noctuidae",
        description = "A caterpillar pest that feeds on watermelon foliage and can also damage fruit surfaces.",
        damage = "Irregular holes and skeletonized leaves, defoliation, and feeding injury or scarring on developing fruit.",
        cause = "Larvae of the beet armyworm moth feeding on plant tissue.",
        management = "Scout leaves and fruit regularly, conserve natural enemies, remove heavily infested plant material, and use selective insect control when thresholds are reached.",
        type = "Insect",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.beet_armyworm,
        source = "https://ask.ifas.ufl.edu/publication/IN168"
    ),
    WatermelonDisease(
        name = "Cabbage Looper",
        scientificName = "Trichoplusia ni",
        family = "Noctuidae",
        description = "A foliage-feeding caterpillar that can occur on watermelon and other cucurbits.",
        damage = "Large irregular holes in leaves, defoliation, and reduced canopy development.",
        cause = "Larvae feed on leaves and young plant tissue.",
        management = "Scout foliage, conserve parasitoids and predators, and use selective insect control when economically justified.",
        type = "Insect",
        partAffected = "LEAF",
        imageRes = R.drawable.cabbage_looper,
        source = "https://ask.ifas.ufl.edu/publication/IN168"
    ),
    WatermelonDisease(
        name = "Cucumber Beetle",
        scientificName = "Diabrotica spp. / Acalymma spp.",
        family = "Chrysomelidae",
        description = "A beetle pest attacking watermelon seedlings, foliage, flowers, stems, roots, and fruit surfaces.",
        damage = "Leaf and cotyledon feeding, stem injury, root feeding, fruit scarring, and stand loss in young plants.",
        cause = "Adult beetles feed above ground while larvae of some species feed on roots.",
        management = "Monitor seedlings closely, control weeds and volunteer hosts, use row covers before flowering, and manage beetles when damaging populations occur.",
        type = "Insect",
        partAffected = "ROOT/LEAF/STEM/FRUIT",
        imageRes = R.drawable.cucumber_beetle,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/cucumber-beetles/"
    ),
    WatermelonDisease(
        name = "Cutworm",
        scientificName = "Agrotis spp. / Other Noctuidae",
        family = "Noctuidae",
        description = "Soil-associated caterpillars that can damage young watermelon plants.",
        damage = "Seedlings may be cut at or near the soil line, resulting in missing plants and poor stands.",
        cause = "Night-feeding cutworm larvae hiding in soil or plant debris during the day.",
        management = "Maintain field sanitation, remove weeds before planting, monitor young seedlings, and manage populations when damaging levels occur.",
        type = "Insect",
        partAffected = "STEM/ROOT",
        imageRes = R.drawable.cutworm,
        source = "https://ask.ifas.ufl.edu/publication/IN168"
    ),
    WatermelonDisease(
        name = "Fall Armyworm",
        scientificName = "Spodoptera frugiperda",
        family = "Noctuidae",
        description = "A highly mobile caterpillar pest capable of feeding on watermelon foliage and fruit surfaces.",
        damage = "Irregular leaf feeding, defoliation, and rind feeding or scarring on developing fruit.",
        cause = "Larvae of the fall armyworm feeding on plant tissue.",
        management = "Scout young foliage and fruit, conserve natural enemies, remove heavily infested tissue when practical, and use appropriate selective insect management.",
        type = "Insect",
        partAffected = "LEAF/FRUIT",
        imageRes = R.drawable.fall_armyworm,
        source = "https://ask.ifas.ufl.edu/publication/PI031"
    ),
    WatermelonDisease(
        name = "Flea Beetle",
        scientificName = "Systena spp. / Other Chrysomelidae",
        family = "Chrysomelidae",
        description = "Small jumping beetles that can feed on young watermelon foliage.",
        damage = "Numerous small shot-hole-like feeding wounds on leaves, particularly on young plants.",
        cause = "Adult flea beetles chew small holes in leaf tissue.",
        management = "Monitor young plants, control weeds around fields, maintain vigorous crop growth, and protect seedlings when feeding becomes severe.",
        type = "Insect",
        partAffected = "LEAF",
        imageRes = R.drawable.flea_beetle,
        source = "https://ask.ifas.ufl.edu/publication/PI031"
    ),
    WatermelonDisease(
        name = "Green Peach Aphid",
        scientificName = "Myzus persicae",
        family = "Aphididae",
        description = "A sap-feeding aphid that attacks watermelon and can transmit several important viruses.",
        damage = "Leaf curling, yellowing, stunting, honeydew production, sooty mold, and indirect virus damage.",
        cause = "Aphid feeding on plant sap and transmission of viruses between plants.",
        management = "Monitor leaf undersides, conserve natural enemies, manage weed hosts, use reflective mulch where appropriate, and avoid unnecessary broad-spectrum insecticides.",
        type = "Insect",
        partAffected = "LEAF/FRUIT/WHOLE",
        imageRes = R.drawable.green_peach_aphid,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/green-peach-aphid/"
    ),
    WatermelonDisease(
        name = "Leafhopper",
        scientificName = "Cicadellidae spp.",
        family = "Cicadellidae",
        description = "Sap-feeding insects occasionally found on watermelon foliage.",
        damage = "Feeding can cause stippling, chlorosis, reduced plant vigor, and possible transmission of some plant pathogens depending on species.",
        cause = "Leafhoppers puncture plant tissue and suck plant sap.",
        management = "Monitor foliage and weeds, maintain field sanitation, and manage damaging populations while conserving beneficial insects.",
        type = "Insect",
        partAffected = "LEAF",
        imageRes = R.drawable.leafhopper,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/"
    ),
    WatermelonDisease(
        name = "Leafminer",
        scientificName = "Liriomyza sativae / Liriomyza trifolii",
        family = "Agromyzidae",
        description = "Small fly larvae that tunnel within watermelon leaves.",
        damage = "Winding whitish mines and feeding punctures reduce photosynthetic area; severe infestations can cause defoliation and expose fruit to sunscald.",
        cause = "Larvae feed between the upper and lower leaf surfaces.",
        management = "Preserve parasitoid natural enemies, maintain field sanitation, scout leaves, and avoid unnecessary broad-spectrum insecticides.",
        type = "Insect",
        partAffected = "LEAF",
        imageRes = R.drawable.leafminer,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/leafminers/"
    ),
    WatermelonDisease(
        name = "Melon Aphid",
        scientificName = "Aphis gossypii",
        family = "Aphididae",
        description = "One of the most important aphid pests of watermelon because of direct feeding and virus transmission.",
        damage = "Leaves curl and crumple, plants become stunted, honeydew accumulates, sooty mold develops, and viruses may spread rapidly.",
        cause = "Sap-feeding aphid colonies, especially on the underside of leaves and growing shoots.",
        management = "Monitor growing points and leaf undersides, conserve predators and parasitoids, manage weeds, and use reflective mulch where appropriate.",
        type = "Insect",
        partAffected = "LEAF/FRUIT/WHOLE",
        imageRes = R.drawable.melon_aphid,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/melon-aphid/"
    ),

    WatermelonDisease(
        name = "Spider Mites",
        scientificName = "Tetranychus spp.",
        family = "Tetranychidae",
        description = "Tiny plant-feeding mites that can rapidly increase under hot and dry conditions.",
        damage = "Leaves become pale and stippled, then bronze, dry, and die under heavy infestations. Yield and fruit quality may decline.",
        cause = "Mites remove chlorophyll while feeding on leaf cells.",
        management = "Monitor leaf undersides, maintain adequate plant moisture, conserve predatory mites and insects, and avoid practices that unnecessarily destroy natural enemies.",
        type = "Mite",
        partAffected = "LEAF",
        imageRes = R.drawable.spider_mites,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/spider-mites/"
    ),
    WatermelonDisease(
        name = "Squash Bug",
        scientificName = "Anasa tristis",
        family = "Coreidae",
        description = "A sap-feeding bug that attacks melons and other cucurbit crops.",
        damage = "Leaves become speckled, yellow, and brown; heavy feeding causes wilting, brittle tissue, runner loss, and injury to young fruit.",
        cause = "Adults and nymphs suck plant sap using piercing-sucking mouthparts.",
        management = "Remove crop residues, scout leaf undersides and stems for eggs and nymphs, and manage populations when damage is significant.",
        type = "Insect",
        partAffected = "LEAF/STEM/FRUIT",
        imageRes = R.drawable.squash_bug,
        source = "https://ipm.ucanr.edu/agriculture/cucurbits/squash-bug/"
    ),
    WatermelonDisease(
        name = "Thrips",
        scientificName = "Thrips palmi / Frankliniella spp.",
        family = "Thripidae",
        description = "Tiny rasping and sucking insects that damage watermelon leaves, growing points, and occasionally developing fruit.",
        damage = "Bronzing or silvering of foliage, curled leaves, damaged vine tips, scarring, deformation, and reduced canopy development.",
        cause = "Adults and immature thrips rasp and puncture plant cells while feeding.",
        management = "Monitor leaves and growing points, conserve predatory insects, control weeds before flowering, and use selective insect management where necessary.",
        type = "Insect",
        partAffected = "LEAF/STEM/FRUIT",
        imageRes = R.drawable.thrips,
        source = "https://ask.ifas.ufl.edu/publication/IN168"
    ),
    WatermelonDisease(
        name = "Whitefly",
        scientificName = "Bemisia tabaci",
        family = "Aleyrodidae",
        description = "A major sap-feeding pest of watermelon that can also transmit several important viruses.",
        damage = "Heavy feeding reduces plant vigor, produces honeydew and sooty mold, and may cause severe virus-related disease and fruit-quality losses.",
        cause = "Adults and nymphs feed on phloem sap. Adults transmit several whitefly-borne viruses.",
        management = "Monitor leaf undersides, manage weeds and crop residues, use clean transplants, conserve natural enemies, and implement integrated whitefly management.",
        type = "Insect",
        partAffected = "LEAF/FRUIT/WHOLE",
        imageRes = R.drawable.whitefly,
        source = "https://ask.ifas.ufl.edu/publication/IN871"
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
        shopLink = "N/A",
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
        shopLink = "N/A",
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
        shopLink = "N/A",
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
        shopLink = "N/A",
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
        shopLink = "N/A",
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
        shopLink = "N/A",
        localInfo = "Available at most major agrivet stores in Tagbilaran.",
        imageRes = R.drawable.ridomel
    ),
    Medicine(
        name = "Orondis",
        type = "Systemic / Translaminar (Oxathiapiprolin)",
        description = "A highly specialized oomycete fungicide used as part of a resistance-managed program against destructive water-mold diseases.",
        targetDisease = "Cucurbit Downy Mildew, Phytophthora Blight and Fruit Rot",
        application = "Preventive foliar spray or according to the registered product label. Use with an effective fungicide partner when required by the label.",
        dose = "Follow the current product label and locally registered rate. Do not exceed the maximum number of applications allowed for the crop.",
        shopLink = "N/A",
        localInfo = "Verify current Philippine FPA registration and watermelon label before purchase or application.",
        imageRes = R.drawable.orondis
    ),
    Medicine(
        name = "Revus",
        type = "Systemic / Translaminar (Mandipropamid)",
        description = "An oomycete fungicide used primarily as a preventive component of disease-management programs.",
        targetDisease = "Cucurbit Downy Mildew, Phytophthora Blight and Fruit Rot",
        application = "Foliar spray with thorough canopy coverage. Rotate with fungicides from different modes of action.",
        dose = "Follow the current registered product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Check current FPA registration and the product label for watermelon use.",
        imageRes = R.drawable.revus
    ),
    Medicine(
        name = "Zampro",
        type = "Systemic / Contact (Ametoctradin + Dimethomorph)",
        description = "Dual-action oomycete fungicide intended for use in resistance-managed programs against downy mildew and related water-mold diseases.",
        targetDisease = "Cucurbit Downy Mildew, Phytophthora Blight",
        application = "Foliar application. Use preventively and rotate with fungicides having different modes of action.",
        dose = "Follow the current registered product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Verify current Philippine FPA registration, crop label, and availability before use.",
        imageRes = R.drawable.zampro
    ),
    Medicine(
        name = "Agrimek 1.8 EC",
        type = "Insecticide / Miticide (Abamectin)",
        description = "A translaminar insecticide and miticide useful against several difficult pests, particularly leafminers, thrips, and mites.",
        targetDisease = "Leafminer, Thrips, Spider Mites",
        application = "Foliar spray with good coverage of leaf surfaces, especially where pests are feeding. Avoid unnecessary repeated applications.",
        dose = "For Philippine watermelon use, follow the registered product label. Syngenta Philippines lists watermelon targets including leafminer and thrips.",
        shopLink = "N/A",
        localInfo = "Syngenta Philippines lists Agrimek 1.8 EC with FPA Registration No. 011-234-0791 and watermelon use for leafminer, thrips, and melonworm.",
        imageRes = R.drawable.agrimek
    ),
    Medicine(
        name = "Bt Insecticide",
        type = "Biological Insecticide (Bacillus thuringiensis)",
        description = "A biological insecticide particularly useful against young caterpillar larvae when applied before severe defoliation occurs.",
        targetDisease = "Beet Armyworm, Cabbage Looper, Fall Armyworm, Cutworm",
        application = "Foliar spray targeting young larvae. Thorough coverage is important because larvae must consume treated plant tissue.",
        dose = "Follow the specific registered Bt product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Use only a product registered for the intended crop and pest in the Philippines.",
        imageRes = R.drawable.bt_insecticide
    ),
    Medicine(
        name = "Chlorantraniliprole",
        type = "Systemic / Translaminar Insecticide",
        description = "A selective insecticide used primarily against caterpillar pests and useful in an integrated resistance-management program.",
        targetDisease = "Beet Armyworm, Fall Armyworm, Cutworm, Cabbage Looper",
        application = "Foliar spray when damaging caterpillar populations are detected. Target young larvae and follow the registered label.",
        dose = "Use only the rate specified on the current registered watermelon product label.",
        shopLink = "N/A",
        localInfo = "Verify the exact Philippine product registration, watermelon crop registration, target pest, dose, and pre-harvest interval.",
        imageRes = R.drawable.chlorantraniliprole
    ),
    Medicine(
        name = "Insecticidal Soap",
        type = "Contact Insecticide",
        description = "A contact treatment that can suppress soft-bodied insect pests when spray coverage directly reaches the insects.",
        targetDisease = "Melon Aphid, Green Peach Aphid, Whitefly, Thrips",
        application = "Foliar spray directed at insects on leaf undersides and growing points. Good direct contact and coverage are essential.",
        dose = "Follow the registered product label. Do not use concentrated household detergent as a substitute.",
        shopLink = "N/A",
        localInfo = "Confirm that the specific agricultural product is registered and labeled for watermelon.",
        imageRes = R.drawable.insecticidal_soap
    ),
    Medicine(
        name = "Horticultural Oil",
        type = "Contact / Suffocating Insecticide",
        description = "A petroleum-based horticultural oil that can suppress certain soft-bodied insects and mites through direct contact.",
        targetDisease = "Melon Aphid, Green Peach Aphid, Whitefly, Spider Mites",
        application = "Thorough foliar coverage, particularly on leaf undersides. Avoid spraying under conditions that increase the risk of crop injury.",
        dose = "Follow the exact agricultural product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Use only an agricultural horticultural oil labeled for watermelon.",
        imageRes = R.drawable.horticultural_oil
    ),
    Medicine(
        name = "Velum Prime",
        type = "Systemic Nematicide / Fungicide (Fluopyram)",
        description = "A nematicide/fungicide containing fluopyram that can be used against root-knot nematodes and certain fungal diseases where the product is registered.",
        targetDisease = "Root Knot Nematode, Fusarium Wilt, Powdery Mildew",
        application = "Soil-directed application through the root zone according to the registered label. It should be integrated with crop rotation and other nematode-management practices.",
        dose = "Follow the current local label exactly. Do not use a rate from another country or crop.",
        shopLink = "N/A",
        localInfo = "Watermelon use for nematodes and powdery mildew is documented for some Velum Prime labels, but Philippine FPA registration must be verified before use.",
        imageRes = R.drawable.velum_prime
    ),
    Medicine(
        name = "Trichoderma Biological Fungicide",
        type = "Biological Fungicide",
        description = "A biological disease-management option that can help suppress soilborne pathogens as part of an integrated crop protection program.",
        targetDisease = "Damping-Off, Root Diseases, Fusarium Wilt",
        application = "Apply to seed, transplant media, soil, or root zone according to the registered product label.",
        dose = "Follow the specific registered product label.",
        shopLink = "N/A",
        localInfo = "Choose a registered agricultural Trichoderma product and verify its watermelon use.",
        imageRes = R.drawable.trichoderma
    ),
    Medicine(
        name = "Potassium Phosphite",
        type = "Systemic / Plant Defense Support",
        description = "A phosphite-based product used as part of integrated management programs for certain oomycete diseases.",
        targetDisease = "Phytophthora Blight and Fruit Rot, Downy Mildew",
        application = "Foliar or root-zone application according to the registered product label.",
        dose = "Follow the exact product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Verify Philippine FPA registration and the exact crop/disease claim before application.",
        imageRes = R.drawable.potassium_phosphite
    ),
    Medicine(
        name = "Copper Hydroxide",
        type = "Contact Bactericide / Fungicide",
        description = "A copper-based protective treatment useful for suppressing certain bacterial and fungal diseases when applied preventively.",
        targetDisease = "Bacterial Fruit Blotch, Angular Leaf Spot, Cercospora Leaf Spot, Anthracnose",
        application = "Preventive foliar spray with complete coverage. Avoid excessive copper accumulation and follow label restrictions.",
        dose = "Follow the registered product label for watermelon.",
        shopLink = "N/A",
        localInfo = "Your existing Kocide 2000 and Nordox already provide copper-based protection; use an additional copper product only when justified and according to its label.",
        imageRes = R.drawable.copper_hydroxide
    ),
    Medicine(
        name = "Sulfur Fungicide",
        type = "Contact Fungicide",
        description = "A protective fungicide particularly useful for powdery mildew and some mite-management programs.",
        targetDisease = "Powdery Mildew",
        application = "Foliar spray with good coverage. Avoid application during conditions that can cause sulfur injury.",
        dose = "Follow the registered product label for watermelon and environmental restrictions.",
        shopLink = "N/A",
        localInfo = "Verify the product is registered for watermelon and follow the label temperature and application restrictions.",
        imageRes = R.drawable.sulfur
    ),
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