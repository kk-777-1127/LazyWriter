package io.github.kk777.request

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String,
    val content: String
)
