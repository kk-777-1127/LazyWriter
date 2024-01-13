package model

sealed interface AppError {
    val message: String
}

sealed interface NotionError: AppError {
    data class HealthCheckLocalPortError(override val message: String) : NotionError
    data class RetrievePageIdsError(override val message: String) : NotionError

    data class NoQiitaTagPageError(override val message: String) : NotionError

    data class GetMarkDownContentError(override val message: String) : NotionError
    data class DeleteTagError(override val message: String) : NotionError
}

sealed interface OpenAiError: AppError {
    data class FileUploadError(override val message: String) : OpenAiError

    data class FileDownloadError(override val message: String) : OpenAiError

    data class MessageCreationError(override val message: String) : OpenAiError

    data class RunningError(override val message: String) : OpenAiError

    data class RunningStatusNotComplete(override val message: String) : OpenAiError

    data class RetrieveMessageError(override val message: String) : OpenAiError

    data class DeleteError(override val message: String) : OpenAiError

}

sealed interface QiitaError: AppError {
    data class PostError(override val message: String) : QiitaError
}


data class UnKnownError(override val message: String): AppError


