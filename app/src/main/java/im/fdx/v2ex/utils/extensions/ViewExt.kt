package im.fdx.v2ex.utils.extensions

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.esafirm.imagepicker.features.ImagePicker
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.R
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import org.jetbrains.anko.forEachChild

/**
 * Created by fdx on 2017/6/14.
 * fdx will maintain it
 */


/**
 * 在中间位置显示"没有内容"信息
 */
fun FrameLayout.showNoContent(boolean: Boolean = true) {
    if (!boolean) {
        this.forEachChild { view ->
            when {
                view.tag == "no" -> removeView(view)
            }
        }
    } else {
        var b = false
        this.forEachChild { view ->
            when {
                view.tag == "no" -> b = true
            }
        }
        if (!b) {
            val child = TextView(this.context)
            child.tag = "no"
            child.text = "没有内容"
            child.setTextColor(ContextCompat.getColor(context, R.color.hint))
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.CENTER_HORIZONTAL
            params.topMargin = 120.dp2px()
            this.addView(child, -1, params)
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

fun SwipeRefreshLayout.initTheme() {
    setColorSchemeResources(R.color.accent_orange)
    setProgressBackgroundColorSchemeResource(R.color.bg_refresh)
}

/**
 * Created by fdx on 2017/7/3.
 * fdx will maintain it
 */

fun ImageView.load(url: Any?) {
    GlideApp.with(context)
            .load(url)
            .into(this)
}


fun Activity.openImagePicker() {

    if (pref.getBoolean(Keys.KEY_NEED_WARN, true)) {
        AlertDialog.Builder(this, R.style.AppTheme_Simple)
                .setTitle("您开启了两步验证")
                .setPositiveButton("知道了") { _, _ ->
                    pref.edit().putBoolean(Keys.KEY_NEED_WARN, false).apply()
                    ImagePicker.create(this)
                            .theme(R.style.Theme_V2ex)
                            .multi()
                            .limit(10)
                            .start()
                }
                .setTitle("使用须知")
                .setMessage("""
                    1. 本app使用图床来自https://sm.ms。
                    2. 上传的图片为公开图片，且暂无删除功能，请谨慎使用。
                    3. 图片内容需遵守当地法律法规。
                    4. 每次最多选择10张图片
                """.trimIndent()).show()
    } else {
        ImagePicker.create(this)
                .theme(R.style.Theme_V2ex)
                .multi()
                .limit(10)
                .start()
    }

}
//
//ImagePicker.create(this)
//.returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
//.folderMode(true) // folder mode (false by default)
//.toolbarFolderTitle("Folder") // folder selection title
//.toolbarImageTitle("Tap to select") // image selection title
//.toolbarArrowColor(Color.BLACK) // Toolbar 'up' arrow color
//.single() // single mode
//.multi() // multi mode (default mode)
//.limit(10) // max images can be selected (99 by default)
//.showCamera(true) // show camera or not (true by default)
//.imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
//.origin(images) // original selected images, used in multi mode
//.exclude(images) // exclude anything that in image.getPath()
//.theme(R.style.CustomImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
//.imageLoader(new GrayscaleImageLoder()) // custom image loader, must be serializeable
//.start(); // start image picker activity with request code