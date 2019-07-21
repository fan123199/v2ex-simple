package im.fdx.v2ex.utils.extensions

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import im.fdx.v2ex.R
import im.fdx.v2ex.ui.LoginActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */

fun Context.dealError(errorCode: Int = -1, swipe: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null) {

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

fun Fragment.toast(message : CharSequence): Toast = Toast
        .makeText(activity, message, Toast.LENGTH_SHORT)
        .apply {
            show()
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


fun Activity.showLoginHint(view: View,message :String ="您还未登录，请登录后再试") {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction("登录") {
                startActivity<LoginActivity>()
            }.show()
}

fun Fragment.showLoginHint(view: View, message :String ="您还未登录，请登录后再试") {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction("登录") {
                activity?.startActivity<LoginActivity>()
            }.show()
}