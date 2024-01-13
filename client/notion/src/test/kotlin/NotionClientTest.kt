import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Expect
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import model.NotionError
import model.NotionRetrievingPageIdData
import model.RelationData
import okio.ByteString.Companion.toByteString
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.junit.Rule
import org.junit.Test
import kotlin.io.path.Path

class NotionClientTest {

    @JvmField
    @Rule
    val expect: Expect = Expect.create()

    private fun createTarget(engine: MockEngine) = NotionClientImpl(
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }
        }
    )

    @Test
    fun `test healthCheckLocalPort Success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondOk()
            }
        )
        expect.that(target.healthCheckLocalPort()).isEqualTo(Ok(Unit))
    }

    @Test
    fun `test healthCheckLocalPort status not success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondError(status = HttpStatusCode.BadRequest)
            }
        )
        expect.that(target.healthCheckLocalPort()).isEqualTo(Err(NotionError.HealthCheckLocalPortError("Notion local port is not available")))
    }

    @Test
    fun `test healthCheckLocalPort throw exception`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                throw Exception("test")
            }
        )
        expect.that(target.healthCheckLocalPort()).isEqualTo(Err(NotionError.HealthCheckLocalPortError("test")))
    }

    @Test
    fun `test retrievePageIds Success`() = runTest {
        val json = FileSystem.RESOURCES.read(Path(NOTION_DATABASE_RESPONSE_PATH).toOkioPath()) {
            readUtf8()
        }
        val target = createTarget(
            engine = MockEngine {
                respond(
                    content = ByteReadChannel(json),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )
        expect.that(target.retrievePageIds()).isEqualTo(Ok(
            NotionRetrievingPageIdData(
                pageIds = listOf(
                    "0c03bac5-8104-4180-a0e8-82e735025e32",
                    "f6ac9fe9-dcd2-4fd2-80f7-caa34bbbd924",
                ),
                relationIds = listOf(
                    RelationData(
                        pageId = "0c03bac5-8104-4180-a0e8-82e735025e32",
                        relationIds = listOf(
                            "0d41beec-30a1-44f8-926b-9ac1130b2207",
                            "84cf048f-fff4-4702-aefa-a5f19d754372",
                        )
                    ),
                    RelationData(
                        pageId = "f6ac9fe9-dcd2-4fd2-80f7-caa34bbbd924",
                        relationIds = listOf(
                            "eeb81602-9ac4-494f-81bd-47efa4b13944",
                            "84cf048f-fff4-4702-aefa-a5f19d754372",
                        )
                    ),
                )
            )
        ))
    }

    @Test
    fun `test retrievePageIds throw exception`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                throw Exception("test")
            }
        )
        expect.that(target.retrievePageIds()).isEqualTo(Err(NotionError.RetrievePageIdsError("test")))
    }

    @Test
    fun `test getMarkDownContent Success`() = runTest {
        val markDown = FileSystem.RESOURCES.read(Path(MOCK_MARKDOWN).toOkioPath()) {
            readByteArray()
        }
        val target = createTarget(
            engine = MockEngine {
                respond(
                    content = markDown,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "text/markdown")
                )
            }
        )
        expect.that(target.getMarkDownContent("test")).isEqualTo(
            Ok(markDown.toByteString())
        )
    }

    @Test
    fun `test getMarkDownContent throw exception`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                throw Exception("test")
            }
        )
        expect.that(target.getMarkDownContent("test")).isEqualTo(Err(NotionError.GetMarkDownContentError("test")))
    }

    @Test
    fun `test deleteTag Success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondOk()
            }
        )
        expect.that(target.deleteTag(RelationData("test", listOf("test")))).isEqualTo(Ok(Unit))
    }

    @Test
    fun `test deleteTag status not success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondError(status = HttpStatusCode.BadRequest)
            }
        )
        expect.that(target.deleteTag(RelationData("test", listOf("test")))).isEqualTo(Err(NotionError.DeleteTagError("Failed to delete tag")))
    }

    @Test
    fun `test deleteTag throw exception`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                throw Exception("test")
            }
        )
        expect.that(target.deleteTag(RelationData("test", listOf("test")))).isEqualTo(Err(NotionError.DeleteTagError("test")))
    }

    companion object {
        const val NOTION_DATABASE_RESPONSE_PATH = "notionDatabaseResponse.json"
        const val MOCK_MARKDOWN = "markdown.md"

    }
}