package im.fdx.v2ex.utils.extensions

import android.app.Activity
import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import org.jetbrains.anko.toast

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */

fun Context.dealError(errorCode: Int = -1, swipe: SwipeRefreshLayout? = null) {

    when {
        this is Activity -> runOnUiThread {
            swipe?.isRefreshing = false
            when (errorCode) {
                -1 -> toast(getString(R.string.error_network))
                302 -> toast(getString(R.string.error_auth_failure))
                else -> toast(getString(R.string.error_network))
            }
        }
    }
}


fun Any.logv(msg: Any?) {
    XLog.tag("v+" + this::class.java.simpleName).v(msg)
}

fun Any.logd(msg: Any?) {
    XLog.tag("v+" + this::class.java.simpleName).d(msg)
}

fun Any.logi(msg: Any?) {
    XLog.tag("v+" + this::class.java.simpleName).i(msg)
}

fun Any.logw(msg: Any?) {
    XLog.tag("v+" + this::class.java.simpleName).w(msg)
}

fun Any.loge(msg: Any?) {
    XLog.tag("v+" + this::class.java.simpleName).e(msg)
}