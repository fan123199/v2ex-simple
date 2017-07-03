package im.fdx.v2ex.utils

import im.fdx.v2ex.utils.extensions.fullUrl
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */
class ContentUtilsTest {
    @Test
    @Throws(Exception::class)
    fun formatContent() {
        val s = "href=\"/go/"
        val out = s.fullUrl()


        assertEquals(out, "href=\"https://www.v2ex.com/go/")
    }


    @Test
    @Throws(Exception::class)
    fun TimeFormat() {
        val s = TimeUtil.getAbsoluteTime(1341262360)
        assertEquals("succeed", "2012/07/03", s)
    }

}
