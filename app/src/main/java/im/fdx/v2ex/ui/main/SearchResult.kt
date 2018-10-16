package im.fdx.v2ex.ui.main

import com.google.gson.annotations.SerializedName

data class SearchResult(
    val hits: MutableList<HitsItem>? = null,
    val took: Int? = null,
    val total: Int? = null,
    val timedOut: Boolean? = null
)

data class HitsItem(
    val highlight: Highlight? = null,
    val index: String? = null,
    val type: String? = null,
    @SerializedName("_source")
    val source: Source? = null,
    @SerializedName("_id")
    val id: String? = null,
    val score: Double? = null
)


data class Source(
    val node: Int? = null,
    val replies: Int? = null,
    val created: String? = null,
    val member: String? = null,
    val id: Int? = null,
    val title: String? = null,
    val content: String? = null
)


data class Highlight(
    val replyListContent: List<String?>? = null,
    val title: List<String?>? = null,
    val postscriptListContent: List<String?>? = null,
    val content: List<String?>? = null
)
