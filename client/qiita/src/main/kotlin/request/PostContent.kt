package request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostContent(
    val body: String,
    val coediting: Boolean,
    @SerialName("private") val privatePost: Boolean,
    val group_url_name: String?,
    val organization_url_name: String?,
    val tags: List<Tag>,
    val title: String,
    val slide: Boolean,
    val tweet: Boolean
)