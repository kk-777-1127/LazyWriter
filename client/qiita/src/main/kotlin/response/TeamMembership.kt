package response

import kotlinx.serialization.Serializable

@Serializable
data class TeamMembership(
    val name: String
)