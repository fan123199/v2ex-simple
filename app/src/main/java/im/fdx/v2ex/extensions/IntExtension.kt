package im.fdx.v2ex.extensions

import android.content.res.Resources

/**
 * Created by fdx on 2017/6/8.
 * fdx will maintain it
 */


fun Int.dp2px(): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = this * 1.0f * (metrics.densityDpi / 160f)
    return Math.round(px)
}

fun Int.px2dp(): Int {
    val metrics = Resources.getSystem().displayMetrics
    val dp = this * 1.0f / (metrics.densityDpi / 160f)
    return Math.round(dp)
}