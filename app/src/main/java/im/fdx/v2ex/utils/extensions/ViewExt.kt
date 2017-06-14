package im.fdx.v2ex.utils.extensions

import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import im.fdx.v2ex.R

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */
fun FrameLayout.showNoContent() {
    val child = TextView(this.context)
    child.text = "没有内容"
    child.setTextColor(ContextCompat.getColor(context, R.color.hint))
    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    params.gravity = Gravity.CENTER_HORIZONTAL
    params.topMargin = 80.dp2px()
    this.addView(child, params)
}