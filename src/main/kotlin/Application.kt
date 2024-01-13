import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processor.Processor

class Application: KoinComponent {

    private val processor: Processor by inject()
    suspend fun run() {
        processor.process()
            .onSuccess { message -> logger.debug { message } }
            .onFailure { error -> logger.error { error } }
    }
}