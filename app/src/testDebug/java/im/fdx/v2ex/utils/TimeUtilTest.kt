package im.fdx.v2ex.utils

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNUSED_VARIABLE")
/**
 * Created by fdx on 2017/7/16.
 * fdx will maintain it
 */
class TimeUtilTest {
    @Test
    fun toLong() {

        val ccc = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US)
        val date = ccc.parse("2014-08-08 08:56:06 AM")
    }

}