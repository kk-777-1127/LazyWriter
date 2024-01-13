package processor

import com.github.michaelbull.result.Result
import model.AppError

interface Processor {
    suspend fun process(): Result<String, AppError>
}