package im.fdx.v2ex.utils.extensions

import android.widget.ImageView

/**
 * Created by fdx on 2017/7/3.
 * fdx will maintain it
 */

fun ImageView.load(url: String?) {
    GlideApp.with(context).load(url).into(this)
}