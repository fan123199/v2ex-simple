package im.fdx.v2ex.utils.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by fdx on 2017/7/15.
 * fdx will maintain it
 */
class StringExtKtTest {
    @Test
    fun getPair() {
        val row = " @fdx #23".getPair("fdx")

        val row2 = "@fdx sjfksd".getPair("fdx")
        val row3 = "abcdefsdf".getPair("fdx")

        val row4 = "@fdx sdssds, @ccc #123 , bac \n @3224 #345".getPair("3224")
        assertEquals(row, 23)
        assertEquals(row2, -1)
        assertEquals(row3, -1)
        assertEquals(row4, 345)

    }

}