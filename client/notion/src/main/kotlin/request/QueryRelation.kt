import kotlinx.serialization.Serializable

@Serializable
data class QueryRelation(
    val contains: String
)

@Serializable
data class QueryRelationForTagDelete(
    val id: String
)