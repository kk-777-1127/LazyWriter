package notion.request

import kotlinx.serialization.Serializable
import request.Properties

@Serializable
data class PatchDeletingQiitaTag(
    val properties: Properties
)