package im.fdx.v2ex.utils

import android.content.res.Resources

/**
 * Created by fdx on 2017/4/29.
 *
 *
 * pixel 和 dp 转换
 */

object ViewUtil {
    val screenSize: IntArray
        get() {
            val metrics = Resources.getSystem().displayMetrics

            val height = metrics.heightPixels
            val width = metrics.widthPixels

            return intArrayOf(height, width)
        }
}
