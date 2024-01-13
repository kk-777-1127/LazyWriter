import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.file.FileId
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.TextContent
import com.aallam.openai.api.run.RunId
import com.github.michaelbull.result.Ok
import com.google.common.truth.Expect
import io.github.kk777.OpenAiClientCoordinatorImpl
import io.github.kk777.OpenAiAppClient
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import okio.ByteString
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// めんどくさいので異常系はやらない & Client側はライブラリ通してるだけなのでUTしない
@OptIn(BetaOpenAI::class)
class OpenAiClientCoordinatorImplTest {

    @get:Rule
    val expect: Expect = Expect.create()

    @MockK
    lateinit var client: OpenAiAppClient

    @MockK
    lateinit var byteString: ByteString

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(client::uploadFile)
        mockkStatic(client::createMessage)
        mockkStatic(client::run)
        mockkStatic(client::retrieveRun)
        mockkStatic(client::retryRetrievingRunIfNeeded)
        mockkStatic(client::retrieveMessage)
    }

    private fun createTarget() = OpenAiClientCoordinatorImpl(client)

    @Test
    fun testProcess() = runTest {
        val target = createTarget()
        val mockFileId = FileId("test")
        val mockRunId = RunId("test")
        coEvery { client.uploadFile(byteString) } returns Ok(mockFileId)
        coEvery { client.createMessage(mockFileId) } returns Ok(MessageId("test"))
        coEvery { client.run() } returns Ok(mockRunId)
        coEvery { client.retrieveRun(mockRunId) } returns Ok(Unit)
        coEvery { client.retryRetrievingRunIfNeeded(Status.Completed, mockRunId) } returns Ok(Unit)
        coEvery { client.retrieveMessage() } returns Ok(MessageContent.Text(TextContent("value", listOf())))

        expect.that(target.coordinate(byteString)).isEqualTo(Ok("value"))
    }
}