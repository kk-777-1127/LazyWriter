package io.github.kk777

import MResult
import com.aallam.openai.api.BetaOpenAI
import com.github.michaelbull.result.coroutines.binding.binding
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import logger
import model.AppError
import model.OpenAiError
import okio.ByteString
import orElseIntervalActionAsync

interface OpenAiClientCoordinator {
    suspend fun coordinate(byteString: ByteString): MResult<String, AppError>
}

// 実質Processor
class OpenAiClientCoordinatorImpl(
    private val client: OpenAiAppClient,
): OpenAiClientCoordinator {
    @OptIn(BetaOpenAI::class)
    override suspend fun coordinate(byteString: ByteString): MResult<String, AppError> {
        return binding {
            val fileId = client.uploadFile(byteString).bind()
            client.createMessage(fileId).bind()
            val runId = client.run().bind()
            client.retrieveRun(runId).orElseIntervalActionAsync(
                    predicate = { error -> error is OpenAiError.RunningStatusNotComplete },
                    action = { client.retrieveRun(runId) },
                    interval = 30000L, // 最大3min
                    retryCount = 5
                ).bind()
            val message = client.retrieveMessage().bind()
            message.text.value
        }.onSuccess {
            logger.debug { "delete thread onCoordinateSuccess : context :$it" }
            client.onCompletion()
        }.onFailure {
            logger.debug { "delete thread onCoordinateFailure : id:$it" }
            client.onCompletion()
        }
    }
}