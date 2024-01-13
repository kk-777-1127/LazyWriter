@file:OptIn(BetaOpenAI::class)

package io.github.kk777

import MResult
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.file.FileId
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.client.OpenAI
import com.github.michaelbull.result.*
import com.github.michaelbull.result.coroutines.runSuspendCatching
import kotlinx.coroutines.*
import logger
import model.OpenAiError
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.FileSystem
import okio.Path.Companion.toPath


interface OpenAiAppClient {
    suspend fun uploadFile(byteString: ByteString): MResult<FileId, OpenAiError>

    suspend fun getFiles(fileId: FileId): MResult<ByteString, OpenAiError.FileDownloadError>
    suspend fun createMessage(
        fileId: FileId,
        context: String = "事前に与えられた指示を、与えられたファイルでこなしてください",
    ): MResult<MessageId, OpenAiError>
    suspend fun run(): MResult<RunId, OpenAiError>
    suspend fun retrieveRun(
        runId: RunId,
    ): MResult<Unit, OpenAiError>
    suspend fun retrieveMessage(): MResult<MessageContent.Text, OpenAiError>

    suspend fun retryRetrievingRunIfNeeded(
        status: Status,
        runId: RunId
    ): Result<Unit, OpenAiError>

    suspend fun onCompletion(): Result<String, OpenAiError>

}
@OptIn(BetaOpenAI::class)
class OpenAiAppClientImpl(
    private val openAI: OpenAI,
    private var latestThreadId: ThreadId = ThreadId(""),
    private var latestFileId: FileId = FileId("")
): OpenAiAppClient {

    override suspend fun uploadFile(byteString: ByteString): MResult<FileId, OpenAiError> {
        return runSuspendCatching {
            FileSystem.SYSTEM.write(FILENAME.toPath()) { write(byteString) }
            val fileSource = FileSource(path = FILENAME.toPath(), fileSystem = FileSystem.SYSTEM)
            latestFileId = openAI.file(request = FileUpload(file = fileSource, purpose = Purpose(ASSISTANTS))).id
            latestFileId
        }.mapError { OpenAiError.FileUploadError(it.message ?: "Failed to upload file") }
    }

    override suspend fun getFiles(fileId: FileId): MResult<ByteString, OpenAiError.FileDownloadError> {
        return runSuspendCatching {
            openAI.download(fileId).toByteString()
        }.mapError { OpenAiError.FileDownloadError(it.message ?: "Failed to upload file") }
    }

    override suspend fun createMessage(
        fileId: FileId,
        context: String,
    ): MResult<MessageId, OpenAiError> {
        return runSuspendCatching {
            latestThreadId = openAI.thread().id
            logger.debug { "createThread id : ${latestThreadId.id}" }
            openAI.message(
                threadId = latestThreadId,
                request = MessageRequest(
                    role = Role.User,
                    content = context,
                    fileIds = listOf(fileId),
                )
            ).id
        }.mapError { OpenAiError.MessageCreationError(it.message ?: "Failed to create message") }
    }

    override suspend fun run(): MResult<RunId, OpenAiError> {
        return runSuspendCatching {
            openAI.createRun(
                threadId = latestThreadId,
                request = RunRequest(
                    assistantId = AssistantId(ASSISTANT_ID),
                    model = ModelId(AI_MODEL)
                ),
            ).id
        }.mapError { OpenAiError.RunningError(it.message ?: "Failed to run") }
    }

    override suspend fun retrieveRun(
        runId: RunId
    ): MResult<Unit, OpenAiError> {
        return runSuspendCatching {
            openAI.getRun(threadId = latestThreadId, runId = runId)
        }.mapError {
            OpenAiError.RunningError(it.message ?: "Failed to retrieve run")
        }.toErrorIf( predicate = { it.status != Status.Completed }) {
            OpenAiError.RunningStatusNotComplete("Failed to retrieve run")
        }.map {  }
    }

    override suspend fun retrieveMessage(): MResult<MessageContent.Text, OpenAiError> {
        return runSuspendCatching {
            openAI.messages(latestThreadId).first().content.last() as MessageContent.Text
        }.mapError {
            OpenAiError.RetrieveMessageError(it.message ?: "Failed to retrieve messages")
        }
    }

    override suspend fun retryRetrievingRunIfNeeded(
        status: Status,
        runId: RunId
    ): Result<Unit, OpenAiError> {
        return if (status != Status.Completed) {
            delay(1000)
            retrieveRun(runId)
        } else {
            Ok(Unit)
        }
    }

    override suspend fun onCompletion(): Result<String, OpenAiError> {
        return runSuspendCatching {
            openAI.delete(latestFileId)
            openAI.delete(latestThreadId)
        }.mapError { OpenAiError.DeleteError(it.message ?: "thread or file can not deleted") }
            .toErrorIf(predicate = { !it }) { OpenAiError.DeleteError("thread or file can not deleted") }
            .map { "thread id: $latestThreadId , file id: $latestFileId" }
    }

    companion object {
        const val FILENAME = "exported.md"
        const val ASSISTANTS = "assistants"
        const val AI_MODEL = "gpt-4-1106-preview"
        const val ASSISTANT_ID = "asst_Nty0QCxyxLLqO287RENIql71"
    }
}