package notion.request
import kotlinx.serialization.Serializable

@Serializable
data class Sort(
    val direction: String,
    val `property`: String
)