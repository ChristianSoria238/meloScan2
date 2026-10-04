package com.melon.meloscan.model

data class RecommendedMedicine(
    val medicineName: String,
    val type: String,
    val description: String,
    val targetDisease: String,

    // Additional recommendation information
    val otherMedicine: String,
    val manualSolution: String,

    // Philippine agricultural source information
    val sourceOrganization: String,
    val sourceDescription: String,
    val sourceLink: String,

    // User-provided additional information link
    val informationLink: String
)

object RecommendedMedicineData {

    // User-provided additional information link
    private const val INFORMATION_LINK =
        "https://share.google/4jLQxwKokc2ft3ngz"

    // Official Philippine Fertilizer and Pesticide Authority
    private const val FPA_SOURCE_LINK =
        "https://fpa.da.gov.ph/resources/reports/registered-products/"

    // Official DA Agricultural Training Institute
    private const val ATI_WATERMELON_GUIDE =
        "https://ati2.da.gov.ph/ati-7/content/sites/default/files/users/user16/Watermelon%20Production%20Guide.pdf"

    val medicines = listOf(

        // ============================================================
        // ANTHRACNOSE
        // ============================================================

        RecommendedMedicine(
            medicineName = "Mancozeb-based fungicide",

            type = "Protectant Fungicide",

            description =
                "A protectant fungicide active ingredient used in " +
                        "watermelon disease-management programs for " +
                        "anthracnose. Use only a pesticide product that " +
                        "is currently registered with the Philippine " +
                        "Fertilizer and Pesticide Authority (FPA) and " +
                        "whose current label specifically permits use " +
                        "on watermelon and the target disease.",

            targetDisease = "Anthracnose",

            otherMedicine =
                "Azoxystrobin-based fungicide or another FPA-registered " +
                        "fungicide whose current product label lists " +
                        "watermelon and anthracnose.",

            manualSolution =
                "Low-cost management: remove and properly dispose of " +
                        "severely infected leaves and plant debris; keep " +
                        "the field clean; provide good air movement between " +
                        "plants; avoid prolonged leaf wetness and excessive " +
                        "moisture; maintain good drainage; and practice " +
                        "crop rotation. These practices help reduce " +
                        "disease spread but do not guarantee a cure for " +
                        "an already infected plant.",

            sourceOrganization =
                "Philippine Department of Agriculture - " +
                        "Agricultural Training Institute (DA-ATI) / " +
                        "Fertilizer and Pesticide Authority (FPA)",

            sourceDescription =
                "The DA-ATI Watermelon Production Guide provides " +
                        "Philippine watermelon production and pest/disease " +
                        "management guidance. The FPA maintains the official " +
                        "list of registered agricultural pesticide products " +
                        "in the Philippines.",

            sourceLink = FPA_SOURCE_LINK,

            informationLink = INFORMATION_LINK
        ),

        // ============================================================
        // DOWNY MILDEW
        // ============================================================

        RecommendedMedicine(
            medicineName = "Mancozeb-based fungicide",

            type = "Protectant Fungicide",

            description =
                "A protectant fungicide active ingredient that can be " +
                        "used in disease-management programs for downy " +
                        "mildew. Use only a pesticide product that is " +
                        "currently registered with the Philippine " +
                        "Fertilizer and Pesticide Authority (FPA) and " +
                        "whose current label specifically permits use " +
                        "on watermelon and downy mildew.",

            targetDisease = "Downy Mildew",

            otherMedicine =
                "Azoxystrobin-based fungicide or another FPA-registered " +
                        "fungicide whose current product label lists " +
                        "watermelon and downy mildew.",

            manualSolution =
                "Low-cost management: remove severely infected leaves " +
                        "when practical; improve air circulation around " +
                        "plants; avoid prolonged moisture on foliage; " +
                        "maintain good field drainage; keep the planting " +
                        "area clean; and monitor plants regularly for new " +
                        "symptoms. Manual management helps reduce disease " +
                        "pressure but does not guarantee a cure.",

            sourceOrganization =
                "Philippine Department of Agriculture - " +
                        "Agricultural Training Institute (DA-ATI) / " +
                        "Fertilizer and Pesticide Authority (FPA)",

            sourceDescription =
                "The DA-ATI provides Philippine watermelon production " +
                        "guidance, while the FPA provides the official " +
                        "registration information for agricultural " +
                        "pesticides.",

            sourceLink = FPA_SOURCE_LINK,

            informationLink = INFORMATION_LINK
        ),

        // ============================================================
        // MOSAIC DISEASE
        // ============================================================

        RecommendedMedicine(
            medicineName = "No Curative Fungicide",

            type = "Viral Disease Management",

            description =
                "Mosaic disease is caused by a plant virus. Fungicides " +
                        "are not a cure for a virus-infected plant. " +
                        "Management should focus on preventing the spread " +
                        "of the disease, removing infected plants, " +
                        "controlling insect vectors such as aphids when " +
                        "appropriate, and maintaining clean planting " +
                        "materials and field sanitation.",

            targetDisease = "Mosaic Disease",

            otherMedicine =
                "No fungicide can cure an already virus-infected plant. " +
                        "If insect vectors are present, consult a local " +
                        "agriculturist or follow an appropriate, currently " +
                        "FPA-registered vector-control product label.",

            manualSolution =
                "Low-cost management: remove and properly dispose of " +
                        "infected plants; keep weeds and volunteer plants " +
                        "under control because they may contribute to " +
                        "disease spread; use healthy planting materials; " +
                        "maintain field sanitation; regularly inspect " +
                        "plants for aphids and other possible vectors; " +
                        "and avoid moving contaminated plant material " +
                        "between healthy and infected areas.",

            sourceOrganization =
                "Philippine Department of Agriculture - " +
                        "Agricultural Training Institute (DA-ATI) / " +
                        "Philippine agricultural disease-management guidance",

            sourceDescription =
                "Philippine agricultural guidance for cucurbit crops " +
                        "emphasizes sanitation, management of insect vectors, " +
                        "and removal of virus-infected plants. Mosaic disease " +
                        "should not be presented as curable with a fungicide.",

            sourceLink = ATI_WATERMELON_GUIDE,

            informationLink = INFORMATION_LINK
        )
    )

    fun getByDisease(
        diseaseName: String
    ): RecommendedMedicine? {

        return medicines.firstOrNull {

            it.targetDisease.equals(
                diseaseName,
                ignoreCase = true
            )
        }
    }
}