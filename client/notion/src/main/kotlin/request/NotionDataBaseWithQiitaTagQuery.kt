package notion.request

import kotlinx.serialization.Serializable

@Serializable
data class NotionDataBaseWithQiitaTagQuery(
    val filter: Filter,
    val sorts: List<Sort>
)