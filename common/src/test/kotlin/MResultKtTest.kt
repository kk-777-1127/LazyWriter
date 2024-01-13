import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Expect
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MResultKtTest {

    @get:Rule
    val expect: Expect = Expect.create()
    @Test
    fun orElseIntervalActionTest_Success() = runTest {
        var retryCount = 0
        val result = Ok("test").orElseIntervalActionAsync(
            action = {
                retryCount++
                Ok("test")
            }
        )
        expect.that(result).isEqualTo(Ok("test"))
        expect.that(retryCount).isEqualTo(0)
    }

    @Test
    fun orElseIntervalActionTest_Failure() = runTest {
        var retryCount = 0
        val result = Err("").orElseIntervalActionAsync(
            action = {
                retryCount++
                Err("")
            }
        )
        expect.that(result).isEqualTo( Err(""))
        expect.that(retryCount).isEqualTo(2)
    }

    @Test
    fun orElseIntervalActionTest_Retry1() = runTest {
        var retryCount = 0
        val result = Err("first").orElseIntervalActionAsync(
            action = {
                if (retryCount == 1) {
                    Ok("test")
                } else {
                    retryCount++
                    Err("second")
                }
            }
        )
        expect.that(result).isEqualTo(Ok("test"))
        expect.that(retryCount).isEqualTo(1)
    }
}