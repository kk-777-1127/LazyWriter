package response

import kotlinx.serialization.Serializable
import qiita.response.Group

@Serializable
data class UsersPostItem(
    val body: String,
    val coediting: Boolean,
    val comments_count: Int,
    val created_at: String,
    val group: Group?,
    val id: String,
    val likes_count: Int,
    val organization_url_name: String?,
    val page_views_count: Int?,
    val `private`: Boolean,
    val reactions_count: Int,
    val rendered_body: String,
    val slide: Boolean,
    val stocks_count: Int,
    val tags: List<Tag>,
    val team_membership: TeamMembership?,
    val title: String,
    val updated_at: String,
    val url: String,
    val user: User
)