package response

import kotlinx.serialization.Serializable

@Serializable
data class Properties(
    val DBTag: DBTag,
)