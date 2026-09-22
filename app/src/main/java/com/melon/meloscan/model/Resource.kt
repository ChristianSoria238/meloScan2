package com.melon.meloscan.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resource(

    val id: Long,

    val title: String,

    val description: String,

    val category: String,

    val type: String,

    @SerialName("image_path")
    val imagePath: String?,

    @SerialName("resource_url")
    val resourceUrl: String,

    @SerialName("source_name")
    val sourceName: String?,

    @SerialName("published_date")
    val publishedDate: String?,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)