package im.fdx.v2ex.data.model

import com.google.gson.annotations.SerializedName

data class SearchResult(

        @SerializedName("hits")
    val hits: List<HitsItem>? = null,

        @SerializedName("took")
    val took: Int? = null,

        @SerializedName("total")
    val total: Int? = null,

        @SerializedName("timed_out")
    val timedOut: Boolean? = null
)

data class HitsItem(

        @SerializedName("highlight")
    val highlight: Highlight? = null,

        @SerializedName("_index")
    val index: String? = null,

        @SerializedName("_type")
    val type: String? = null,

        @SerializedName("_source")
    val source: Source? = null,

        @SerializedName("_id")
    val id: String? = null,

        @SerializedName("_score")
    val score: Double? = null
)

data class Source(

    @SerializedName("node")
    val node: Int? = null,

    @SerializedName("replies")
    val replies: Int? = null,

    @SerializedName("created")
    val created: String? = null,

    @SerializedName("member")
    val member: String? = null,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("content")
    val content: String? = null
)
data class Highlight(

    @SerializedName("reply_list.content")
    val replyListContent: List<String?>? = null,

    @SerializedName("title")
    val title: List<String?>? = null,

    @SerializedName("postscript_list.content")
    val postscriptListContent: List<String?>? = null,

    @SerializedName("content")
    val content: List<String?>? = null
)

