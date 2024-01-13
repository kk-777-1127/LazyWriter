
import kotlinx.serialization.Serializable

@Serializable
data class DBTag(
    val relation: List<QueryRelationForTagDelete>
)