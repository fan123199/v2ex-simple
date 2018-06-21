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

            val width = metrics.widthPixels
            val height = metrics.heightPixels

            return intArrayOf(width, height)
        }
}
