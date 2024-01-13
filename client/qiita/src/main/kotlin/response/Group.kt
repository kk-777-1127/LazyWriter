package qiita.response

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val created_at: String,
    val description: String,
    val name: String,
    val `private`: Boolean,
    val updated_at: String,
    val url_name: String
)