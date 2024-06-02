package im.fdx.v2ex.utils.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.health.connect.datatypes.units.Length
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.elvishew.xlog.XLog
import com.esafirm.imagepicker.features.ImagePicker
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */


/**
 * 在中间位置显示"没有内容"信息
 */
fun FrameLayout.showNoContent(content: String = "没有内容") {
    val tagName = "no"
    var alreadyHasNoContent = false
    this.forEach { view ->
        when (tagName) {
            view.tag -> alreadyHasNoContent = true
        }
    }
    if (!alreadyHasNoContent) {
        val child = TextView(this.context)
        child.tag = tagName
        child.text = content
        child.setTextColor(ContextCompat.getColor(context, R.color.hint))
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL
        params.topMargin = 120.dp2px()
        this.addView(child, -1, params)
    }
}

fun FrameLayout.hideNoContent() {
    val tagName = "no"
    this.forEach { view ->
        when (tagName) {
            view.tag -> removeView(view)
        }
    }
}


/**
 * 为每个Activity设置基本toolbar，简化代码
 */
fun AppCompatActivity.setUpToolbar(title: String? = ""): Toolbar {

    val toolbar: Toolbar = findViewById(R.id.toolbar)
    toolbar.title = title
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener { onBackPressed() }
    return toolbar
}

/**
 * Alpha : 0，solid, 255->transparent
 */
fun Activity.setStatusBarColor(@ColorRes colorRes: Int?, @IntRange(from = 0L, to = 255L) statusBarAlpha: Int = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = this.window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        this.window.decorView.systemUiVisibility = flags
    }
    val color = colorRes?.let { ContextCompat.getColor(this, it) } ?: 0
    this.window.statusBarColor = calculateStatusColor(color, statusBarAlpha)
}


fun Context.toast(content:String) {
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
}

fun Activity.setStatusBarLight() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = window.decorView.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = flags
        this.window.statusBarColor = Color.WHITE
    } else {
        this.window.statusBarColor = Color.BLACK
    }

}

fun Activity.clearLightStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = this.window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        this.window.decorView.systemUiVisibility = flags
    }
}


private fun calculateStatusColor(@ColorInt color: Int, alpha: Int): Int {
    if (alpha == 0) {
        return color
    }
    val a = 1 - alpha / 255f
    var red = color shr 16 and 0xff
    var green = color shr 8 and 0xff
    var blue = color and 0xff
    red = (red * a + 0.5).toInt()
    green = (green * a + 0.5).toInt()
    blue = (blue * a + 0.5).toInt()
    return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
}

fun SwipeRefreshLayout.initTheme() {
    setColorSchemeResources(R.color.accent_orange)
//    val typedValue = TypedValue()
//    context.theme.resolveAttribute(R.attr.bg_refresh, typedValue, true)
//    val color = typedValue.data
//    setProgressBackgroundColorSchemeResource(color)
}

fun ImageView.load(url: Any?) {
    Glide.with(this)
        .load(url)
        .into(this)
}


fun handleAlphaOnTitle(view: View, divider: View, percentage: Float) {
    XLog.tag("collapse").d(percentage)
    when (percentage) {
        in 0.0..1.0 -> view.alpha = 1 - percentage
    }
    when (percentage) {
        1f -> divider.alpha = 0f
        else -> divider.alpha = 1f
    }
}


fun openImagePicker(activity: Activity) {

    if (pref.getBoolean(Keys.KEY_WARN_IMAGE_UPLOAD, true)) {
        AlertDialog.Builder(activity, R.style.AppTheme_Simple)
            .setPositiveButton(activity.getString(R.string.iknow)) { _, _ ->
                pref.edit().putBoolean(Keys.KEY_WARN_IMAGE_UPLOAD, false).apply()
                ImagePicker.create(activity)
                    .theme(R.style.Theme_V2ex)
                    .multi()
                    .limit(10)
                    .start()
            }
            .setTitle(activity.getString(R.string.tips))
            .setMessage(
                """
                    1. 本app使用图床来自https://sm.ms
                    2. 上传的图片为公开图片，且暂无删除功能，请谨慎使用
                    3. 图片内容需遵守当地法律法规
                    4. 每次最多选择10张图片
                """.trimIndent()
            ).show()
    } else {
        ImagePicker.create(activity)
            .theme(R.style.Theme_V2ex)
            .multi()
            .limit(10)
            .start()
    }

}

inline fun <reified T: Activity> Context.startActivity(vararg params: Pair<String, Any?>) {
            this.startActivity(Intent(this, T::class.java).apply {
                putExtras(bundleOf(*params))
            })
}

fun Context.shareText(
    text: String,
    subject: String = "",
): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(Intent.createChooser(intent, null))
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}
