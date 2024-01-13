package response

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val name: String,
    val versions: List<String>
)