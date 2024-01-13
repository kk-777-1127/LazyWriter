package response

import common.Relation
import kotlinx.serialization.Serializable

@Serializable
data class DBTag(
    val id: String,
    val has_more: Boolean,
    val relation: List<Relation>,
    val type: String
)