package notion.request

import kotlinx.serialization.Serializable
import request.Or

@Serializable
data class Filter(
    val or: List<Or>
)