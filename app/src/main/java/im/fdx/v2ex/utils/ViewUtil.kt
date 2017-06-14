package im.fdx.v2ex.utils

import android.app.Activity
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.dp2px

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

            XLog.i("height $height, width $width")
            return intArrayOf(height, width)
        }

    fun showNoContent(activity: Activity, container: FrameLayout) {
        val child = TextView(activity)
        child.text = "没有内容"
        child.setTextColor(ContextCompat.getColor(activity, R.color.hint))
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL
        params.topMargin = 80.dp2px()
        container.addView(child, params)
    }
}
