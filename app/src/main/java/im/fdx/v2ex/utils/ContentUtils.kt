package im.fdx.v2ex.utils

/**
 * Created by a708 on 15-8-18.
 * 来自yaoyumeng的开源
 */

object ContentUtils {

    fun format(content: String) = content.replace("href=\"/member/", "href=\"https://www.v2ex.com/member/")
            .replace("href=\"/i/", "href=\"https://i.v2ex.co/")
            .replace("href=\"/t/", "href=\"https://www.v2ex.com/t/")
            .replace("href=\"/go/", "href=\"https://www.v2ex.com/go/")
            .replace("<img src=\"//", "<img src=\"http://")

}