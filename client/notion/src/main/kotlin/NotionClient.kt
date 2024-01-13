import com.github.michaelbull.result.*
import com.github.michaelbull.result.coroutines.runSuspendCatching
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.NotionError
import model.NotionRetrievingPageIdData
import model.RelationData
import model.toRelationDataList
import notion.request.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import request.Or
import request.Properties
import response.NotionDatabaseResponse

interface NotionClient {
    suspend fun healthCheckLocalPort(): MResult<Unit, NotionError>
    suspend fun retrievePageIds(): MResult<NotionRetrievingPageIdData, NotionError>
    suspend fun getMarkDownContent(pageId: String): MResult<ByteString, NotionError>
    suspend fun deleteTag(relationData: RelationData): MResult<Unit, NotionError>
}

class NotionClientImpl(
    private val  ktorClient: HttpClient
): NotionClient {
    override suspend fun healthCheckLocalPort(): MResult<Unit, NotionError> {
        return runSuspendCatching {
            ktorClient.get(LocalEndPoint + HealthCheck) { }
        }.mapError {
            NotionError.HealthCheckLocalPortError(it.message ?: "Notion local port is not available")
        }.toErrorIf(predicate = { !it.status.isSuccess() }) {
            NotionError.HealthCheckLocalPortError("Notion local port is not available")
        }.map {  }
    }

    override suspend fun retrievePageIds(): MResult<NotionRetrievingPageIdData, NotionError> {
        return runSuspendCatching {
            ktorClient.post(NotionEndPoint + DatabaseQuery) {
                contentType(ContentType.Application.Json)
                setBody(notionDataBaseWithQiitaQuery)
            }.body<NotionDatabaseResponse>()
        }.mapEither(
            success = { resp ->
                NotionRetrievingPageIdData(
                    pageIds = resp.results.map { it.id },
                    relationIds = resp.results.associate { result ->
                        result.id to result.properties.DBTag.relation.map { it.id }
                    }.toRelationDataList()
                )
            },
            failure = { NotionError.RetrievePageIdsError(it.message ?: "Failed to retrieve page ids") }
        )
    }

    override suspend fun getMarkDownContent(pageId: String): MResult<ByteString, NotionError> {
        return runSuspendCatching {
            ktorClient.get(LocalEndPoint + notionToMd) {
                header("Content-Type", "text/markdown")
                parameter("pageId", pageId)
            }.readBytes().toByteString()
        }.mapError { NotionError.GetMarkDownContentError(it.message ?: "Failed to get markdown content") }
    }

    override suspend fun deleteTag(relationData: RelationData): MResult<Unit, NotionError> {
        return runSuspendCatching {
            val queryRelation = relationData.relationIds.filter { it != QiitaTadId }.map { QueryRelationForTagDelete(it) }
            val patch = ktorClient.patch(NotionEndPoint + "pages/${relationData.pageId}") {
                contentType(ContentType.Application.Json)
                setBody(
                    PatchDeletingQiitaTag(
                        properties = Properties(
                            DBTag = DBTag(
                                relation = queryRelation
                            )
                        )
                    )
                )
            }
            println(patch.bodyAsText())

            patch
        }.mapError {
            NotionError.DeleteTagError(it.message ?: "Failed to delete tag")
        }.toErrorIf( predicate = { !it.status.isSuccess() }) {
            NotionError.DeleteTagError("Failed to delete tag")
        }.map { }
    }

    companion object {
        const val LocalEndPoint = "http://localhost:3000/"
        const val HealthCheck = "healthCheck"
        const val notionToMd = "notionToMd"
        const val NotionEndPoint = "https://api.notion.com/v1/"
        const val DatabaseId = "79f9920be0074c07af4b2816d9a20c7c"
        const val DatabaseQuery = "databases/$DatabaseId/query"
        const val QiitaTadId = "84cf048f-fff4-4702-aefa-a5f19d754372"

        val notionDataBaseWithQiitaQuery = NotionDataBaseWithQiitaTagQuery(
            filter = Filter(
                or = listOf(
                    Or(
                        property = "DBTag",
                        relation = QueryRelation(
                            contains = QiitaTadId
                        )
                    )
                )
            ),
            sorts = listOf(
                Sort(
                    property = "Created time",
                    direction = "descending"
                )
            )
        )
    }
}