package request
import QueryRelation
import kotlinx.serialization.Serializable

@Serializable
data class Or(
    val `property`: String,
    val relation: QueryRelation
)