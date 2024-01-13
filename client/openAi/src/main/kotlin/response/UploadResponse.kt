package io.github.kk777.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val id: String,
    @SerialName("object") val obj: String,
    val bytes: Int,
    val created_at: Long,
    val filename: String,
    val purpose: String,
    val status: String,
    val status_details: String?
)
