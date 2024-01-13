import com.github.michaelbull.result.*
import com.github.michaelbull.result.coroutines.runSuspendCatching
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AppError
import model.QiitaError
import request.PostContent
import request.Tag

interface QiitaClient {
    suspend fun post(post: String): MResult<Unit, AppError>
}

class QiitaClientImpl(
    private val ktorClient: HttpClient
): QiitaClient {
    override suspend fun post(post: String): Result<Unit, QiitaError> {
        return runSuspendCatching {
            val post1 = ktorClient.post {
                url(ENDPOINT + ITEMS)
                contentType(ContentType.Application.Json)
                // TODO titleとTagをGPTに提案してもらう
                setBody(
                    PostContent(
                        body = post,
                        tags = listOf(Tag(name = "Test", versions = listOf("1.0.0"))),
                        title = "test",
                        privatePost = true,
                        coediting = false,
                        group_url_name = null,
                        organization_url_name = null,
                        slide = false,
                        tweet = false
                    )
                )
            }
            post1
        }.mapError {
            QiitaError.PostError(it.message ?: "Failed to post")
        }.toErrorIf(
            predicate = { !it.status.isSuccess() }
        ) {
            QiitaError.PostError("Failed to post : $it")
        }.map { }
    }

    companion object {
        const val ENDPOINT = "https://qiita.com/api/v2/"
        const val ITEMS = "items"
    }
}