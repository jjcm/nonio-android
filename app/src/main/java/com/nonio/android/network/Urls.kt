package com.nonio.android.network

import com.nonio.android.model.CommentModel
import com.nonio.android.model.CommentVotesModel
import com.nonio.android.model.CommentsModel
import com.nonio.android.model.LinkModel
import com.nonio.android.model.NotificationsModel
import com.nonio.android.model.PostCreateParam
import com.nonio.android.model.PostModel
import com.nonio.android.model.PostsModel
import com.nonio.android.model.TagsModel
import com.nonio.android.model.UserCommentsModel
import com.nonio.android.model.UserInfoModel
import com.nonio.android.model.UserModel
import com.nonio.android.model.VoteModel
import com.nonio.android.model.VotesModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * api doc
 * https://api.non.io/
 */
object Urls {
    const val BASE_URL = "https://api.non.io/"
    const val POST_URL_PREFIX = "https://non.io/"

    private const val BASE_IMAGE_URL = "https://image.non.io/"
    private const val BASE_THUMBNAIL_URL = "https://thumbnail.non.io/"
    private const val BASE_AVATAR_URL = "https://avatar.non.io/"
    private const val BASE_VIDEO_URL = "https://video.non.io/"
    private const val VIDEO_ENCODE_URL = "wss://video.non.io/encode?file="

    // posts
    // Returns 100 posts.
    // ?offset=NUMBER
    const val POSTS = "posts"

    const val POST_VIEW_URL = "/post/view/{url}"

    const val POST = "/posts/{url}"

    const val TAGS = "tags"
    const val TAG_QUERY = "tags/{query}"

    const val LOGIN = "/user/login"

    const val REGISTER = "/user/register"

    const val USERNAME_CHECK = "/user/username-is-available/{userName}"

    const val REGISTER_WEB = "https://non.io/admin/create-account"

    const val USER_INFO = "/users/{userName}"

    const val COMMENTS_POST = "/comments"
    const val COMMENTS_USER = "/comments/user/{user}"
    const val Notifications = "/notifications"
    const val NOTIFICATION_MARK_READ = "/notification/mark-read"
    const val ADD_COMMENT = "/comment/create"

    const val ADD_TAG_VOTE = "/posttag/add-vote"
    const val REMOVE_TAG_VOTE = "/posttag/remove-vote"

    const val ADD_COMMENT_VOTE = "/comment/add-vote"
    const val GET_COMMENT_VOTE = "/comment-votes"
    const val GET_VOTES = "votes"
    const val PARSE_EXTERNAL_URL = "/post/parse-external-url"
    const val URL_IS_AVAILABLE = "/post/url-is-available/{url}"
    const val POST_CREATE = "/post/create"
    const val UPLOAD_IMAGE = "https://image.non.io/upload"
    const val UPLOAD_VIDEO = "https://video.non.io/upload"
    const val MOVE_IMAGE = "https://image.non.io/move"
    const val MOVE_VIDEO = "https://video.non.io/move"
    const val CREATE_POST_TAG = "/posttag/create"
    const val REFRESH_TOKEN = "/user/refresh-access-token"

    fun thumbnailImageURL(path: String): String = "$BASE_THUMBNAIL_URL$path.webp"

    fun avatarImageURL(user: String): String = "$BASE_AVATAR_URL$user.webp"

    fun videoURL(
        path: String,
        isAddSuffix: Boolean = true,
    ): String =
        if (isAddSuffix) {
            "$BASE_VIDEO_URL$path.mp4"
        } else {
            "$BASE_VIDEO_URL$path"
        }

    fun imageURL(path: String): String = "$BASE_IMAGE_URL$path.webp"

    fun videoEncodeURL(file: String): String = "$VIDEO_ENCODE_URL$file"
}

interface ApiService {
    @GET(Urls.TAGS)
    suspend fun tags(): TagsModel?

    @GET(Urls.TAG_QUERY)
    suspend fun tagQuery(
        @Path("query") query: String,
    ): TagsModel?

    @GET(Urls.POST_VIEW_URL)
    suspend fun postViewByUrl(
        @Path("url") url: String,
    ): PostModel?

    @GET(Urls.POST)
    suspend fun postByUrl(
        @Path("url") url: String,
    ): PostModel?

    @GET(Urls.COMMENTS_POST)
    suspend fun commentPost(
        @Query("post") url: String,
    ): CommentsModel?

    @GET(Urls.COMMENTS_USER)
    suspend fun commentUser(
        @Path("user") user: String,
        // 可选参数//offset=NUMBER
        @Query("offset")
        offset: Int? = null,
        // 可选参数 sort=popular|top|new
        @Query("sort")
        sort: String? = null,
    ): UserCommentsModel?

    @GET(Urls.POSTS)
    suspend fun posts(
        // 可选参数//offset=NUMBER
        @Query("offset")
        offset: Int? = null,
        // 可选参数 sort=popular|top|new
        @Query("sort")
        sort: String? = null,
        // all|day|week|month|year
        @Query("time")
        time: String? = null,
        @Query("tag")
        tag: String? = null,
        @Query("user")
        user: String? = null,
    ): PostsModel?

    // 参数 email  password
    @POST(Urls.LOGIN)
    suspend fun login(
        @Body body: RequestBody,
    ): UserModel?

    // 参数 email username password
    @POST(Urls.REGISTER)
    suspend fun register(
        @Body body: RequestBody,
    ): UserModel?

    // username check
    @GET(Urls.USERNAME_CHECK)
    suspend fun userNameCheck(
        @Path("userName") userName: String,
    ): Boolean?

    @GET(Urls.USER_INFO)
    suspend fun userInfo(
        @Path("userName") userName: String,
    ): UserInfoModel?

    @GET(Urls.URL_IS_AVAILABLE)
    suspend fun postUrlAvailable(
        @Path("url") url: String,
    ): Boolean?

    @GET(Urls.Notifications)
    suspend fun notifications(
        @Query("unread") unread: String,
    ): NotificationsModel?

    @POST(Urls.NOTIFICATION_MARK_READ)
    suspend fun markRead(
        @Body body: RequestBody,
    )

    @POST(Urls.ADD_COMMENT)
    suspend fun addComment(
        @Body body: RequestBody,
    ): CommentModel?

    @POST(Urls.ADD_TAG_VOTE)
    suspend fun addTagVote(
        @Body body: RequestBody,
    ): VoteModel?

    @POST(Urls.REMOVE_TAG_VOTE)
    suspend fun removeTagVote(
        @Body body: RequestBody,
    ): Boolean?

    @POST(Urls.PARSE_EXTERNAL_URL)
    suspend fun parseExternalUrl(
        @Body body: RequestBody,
    ): LinkModel?

    @POST(Urls.POST_CREATE)
    suspend fun postCreate(
        @Body body: PostCreateParam,
    ): PostModel?

    @POST(Urls.UPLOAD_IMAGE)
    @Multipart
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
    ): String?

    @POST(Urls.UPLOAD_VIDEO)
    @Multipart
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part,
    ): String?

    @POST(Urls.MOVE_IMAGE)
    @FormUrlEncoded
    suspend fun moveImage(
        @Field("oldUrl") oldUrl: String,
        @Field("url") url: String,
    ): String?

    @POST(Urls.MOVE_VIDEO)
    @FormUrlEncoded
    suspend fun moveVideo(
        @Field("oldUrl") oldUrl: String,
        @Field("url") url: String,
    ): String?

    @GET(Urls.GET_VOTES)
    suspend fun getVotes(): VotesModel?

    @GET(Urls.GET_COMMENT_VOTE)
    suspend fun getCommentVotes(
        @Query("post") post: String,
    ): CommentVotesModel?

    @POST(Urls.ADD_COMMENT_VOTE)
    suspend fun addCommentVote(
        @Body body: RequestBody,
    ): Boolean?

    @POST(Urls.CREATE_POST_TAG)
    suspend fun createPostTag(
        @Body body: RequestBody,
    ): VoteModel?

    @POST(Urls.REFRESH_TOKEN)
    suspend fun refreshToken(
        @Body body: RequestBody,
    ): UserModel?
}
