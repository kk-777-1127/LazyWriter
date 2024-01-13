package response

import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val id: String,
    val properties: Properties
)