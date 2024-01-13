import com.github.michaelbull.result.*
import kotlinx.coroutines.delay

typealias MResult<T, E> = com.github.michaelbull.result.Result<T, E>
fun <V> mRunCatching(block: () -> V): MResult<V, Throwable> = com.github.michaelbull.result.runCatching(block)

suspend fun <T, E>  MResult<T, E>.orElseIntervalActionAsync(
    action: suspend () -> MResult<T, E>,
    retryCount: Int = 2,
    interval: Long = 1000L,
    predicate: (E) -> Boolean = { true }
): MResult<T, E> {
    return this.orElse { error ->
        if (retryCount > 0 && predicate(error)) {
            delay(interval)
            action().orElseIntervalActionAsync(action, retryCount - 1, interval, predicate)
        } else {
            this
        }
    }
}
