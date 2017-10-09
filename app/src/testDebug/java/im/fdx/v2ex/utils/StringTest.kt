package im.fdx.v2ex.utils

import org.junit.Assert
import org.junit.Test

/**
 * Created by fdx on 2017/10/9.
 *
 */
class StringTest {
    @Test
    fun test() {
        Assert.assertEquals("android".contains("abcd"), false)
        Assert.assertEquals("android".contains("anc"), false)
        Assert.assertEquals("android".contains("and"), true)
    }
}