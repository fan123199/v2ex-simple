package im.fdx.v2ex.utils.extensions

/**
 * Created by fdx on 2017/7/3.
 * fdx will maintain it
 */


/**
 * 将链接转成完整链接，一般用 GoodTextView
 */
fun String.fullUrl() = this.replace("href=\"/member/", "href=\"https://www.v2ex.com/member/")
        .replace("href=\"/i/", "href=\"https://i.v2ex.co/")
        .replace("href=\"/t/", "href=\"https://www.v2ex.com/t/")
        .replace("href=\"/go/", "href=\"https://www.v2ex.com/go/")
        .replace("<img src=\"//", "<img src=\"http://")


fun String.getNum(): String {
    var str2 = ""
    if (isNotBlank()) {
        (0 until length)
                .filter { this[it].toInt() in 48..57 }
                .forEach { str2 += this[it] }
    }
    return str2
}


/**
 * 获取 @abcd #rownum
 * return rownum
 */
fun String.getRowNum(name: String): Int {
    val str = """(?<=${name}\s{1,4}#)\d+"""
    val findAll = Regex(str).findAll(this)

    if (findAll.none()) {
        return -1
    }
    return findAll.first().value.toInt()
}