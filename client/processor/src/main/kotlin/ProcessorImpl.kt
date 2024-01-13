import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.coroutines.binding.binding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import io.github.kk777.OpenAiClientCoordinator
import model.AppError
import model.NotionError
import model.NotionRetrievingPageIdData
import model.RelationData
import okio.ByteString
import processor.Processor

data class SuccessInfo (
    val pageId: String,
) {
    fun getSuccessMessage(): String {
        return "Process succeed. pageId: $pageId"
    }
}

class ProcessorImpl(
    private val openAI: OpenAiClientCoordinator,
    private val qiita: QiitaClient,
    private val notion: NotionClient,
): Processor {
    override suspend fun process(): MResult<String, AppError> {
        return binding {
            notion.healthCheckLocalPort().orElseIntervalActionAsync(
                predicate = { error -> error is NotionError.HealthCheckLocalPortError },
                action = { notion.healthCheckLocalPort() }
            ).bind()
            val pageData = notion.retrievePageIds().bind()
            val pageId = pageData.firstPage().bind()
            val byteMarkDown = notion.getMarkDownContent(pageId).bind()
            val assistantResponse = openAI.coordinate(byteMarkDown).bind()
            qiita.post(assistantResponse).bind()
            val qiitaTagRelation = pageData.extractRelation(pageId).bind()
            notion.deleteTag(qiitaTagRelation).bind()
            Ok(SuccessInfo(pageId).getSuccessMessage()).bind()
        }
    }

    private fun NotionRetrievingPageIdData.firstPage(): MResult<String, NotionError> {
        return runCatching { this.pageIds.first() }.mapError {
            NotionError.NoQiitaTagPageError("No Qiita tag page")
        }
    }

    private fun NotionRetrievingPageIdData.extractRelation(pageId: String): MResult<RelationData, NotionError> {
        return runCatching { this.relationIds.first { it.pageId == pageId } }.mapError {
            NotionError.NoQiitaTagPageError("No Qiita tag page")
        }
    }
}