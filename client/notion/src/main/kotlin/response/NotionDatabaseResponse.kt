package response

import kotlinx.serialization.Serializable

@Serializable
data class NotionDatabaseResponse(
    val results: List<Result>,
)