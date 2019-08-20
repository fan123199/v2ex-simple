package im.fdx.v2ex.utils

import android.content.res.Resources


/**
 * Created by fdx on 2017/4/29.
 *
 *
 * pixel 和 dp 转换
 */

object ViewUtil {
    val screenHeight: Int
        get() {
            val metrics = Resources.getSystem().displayMetrics

            return metrics.heightPixels
        }

    val screenWidth : Int
    get() {

        return Resources.getSystem().displayMetrics.widthPixels
    }
}
