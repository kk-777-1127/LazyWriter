package model

data class NotionRetrievingPageIdData(
        val pageIds: List<String>,
        val relationIds: List<RelationData>
)

data class RelationData(
        val pageId: String,
        val relationIds: List<String>
)

fun Map<String, List<String>>.toRelationDataList(): List<RelationData> {
    return this.map { (pageId, relationIds) ->
        RelationData(pageId, relationIds)
    }
}