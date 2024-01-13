package response

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AuthenticatedUser(
    val description: String,

    @SerialName("facebook_id")
    val facebookId: String,

    @SerialName("followees_count")
    val followeesCount: Int,

    @SerialName("followers_count")
    val followersCount: Int,

    @SerialName("github_login_name")
    val githubLoginName: String,

    val id: String,

    @SerialName("image_monthly_upload_limit")
    val imageMonthlyUploadLimit: Int,

    @SerialName("image_monthly_upload_remaining")
    val imageMonthlyUploadRemaining: Int,

    @SerialName("items_count")
    val itemsCount: Int,

    @SerialName("linkedin_id")
    val linkedinId: String,

    val location: String,
    val name: String,
    val organization: String,

    @SerialName("permanent_id")
    val permanentId: Int,

    @SerialName("profile_image_url")
    val profileImageUrl: String,

    @SerialName("team_only")
    val teamOnly: Boolean,

    @SerialName("twitter_screen_name")
    val twitterScreenName: String?,

    @SerialName("website_url")
    val websiteUrl: String
)
