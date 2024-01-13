import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Expect
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import model.QiitaError
import org.junit.Rule
import org.junit.Test

class QiitaClientImplTest {
    @JvmField
    @Rule
    val expect: Expect = Expect.create()

    private fun createTarget(engine: MockEngine) = QiitaClientImpl(
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }
        }
    )

    @Test
    fun `test post Success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondOk()
            }
        )
        expect.that(target.post("")).isEqualTo(Ok(Unit))
    }

    @Test
    fun `test healthCheckLocalPort status not success`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                respondError(status = HttpStatusCode.BadRequest)
            }
        )
        expect.that(target.post("")).isEqualTo(Err(QiitaError.PostError("Failed to post")))
    }

    @Test
    fun `test healthCheckLocalPort throw exception`() = runTest {
        val target = createTarget(
            engine = MockEngine {
                throw Exception("test")
            }
        )
        expect.that(target.post("")).isEqualTo(Err(QiitaError.PostError("test")))
    }
}