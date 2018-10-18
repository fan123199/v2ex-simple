package im.fdx.v2ex.ui.main

import com.google.gson.annotations.SerializedName

data class SearchResult(

    @field:SerializedName("hits")
    val hits: List<HitsItem>? = null,

    @field:SerializedName("took")
    val took: Int? = null,

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("timed_out")
    val timedOut: Boolean? = null
)

data class HitsItem(

    @field:SerializedName("highlight")
    val highlight: Highlight? = null,

    @field:SerializedName("_index")
    val index: String? = null,

    @field:SerializedName("_type")
    val type: String? = null,

    @field:SerializedName("_source")
    val source: Source? = null,

    @field:SerializedName("_id")
    val id: String? = null,

    @field:SerializedName("_score")
    val score: Double? = null
)

data class Source(

    @field:SerializedName("node")
    val node: Int? = null,

    @field:SerializedName("replies")
    val replies: Int? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("member")
    val member: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("content")
    val content: String? = null
)
data class Highlight(

    @field:SerializedName("reply_list.content")
    val replyListContent: List<String?>? = null,

    @field:SerializedName("title")
    val title: List<String?>? = null,

    @field:SerializedName("postscript_list.content")
    val postscriptListContent: List<String?>? = null,

    @field:SerializedName("content")
    val content: List<String?>? = null
)