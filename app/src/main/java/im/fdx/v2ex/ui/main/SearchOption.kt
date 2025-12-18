package im.fdx.v2ex.ui.main

const val SUMUP = "sumup"
const val CREATED = "created"

const val NEW_FIRST = "0"
const val OLD_FIRST = "1"

data class SearchOption(
    val q: String,
    val sort: String = CREATED,
    val order: String = NEW_FIRST,  //0 降序，1 升序
    val gte: String? = null,
    val lte: String? = null,
    val node: String? = null,
    val username: String? = null
)
