package im.fdx.v2ex.utils.extensions

import android.widget.ImageView
import com.squareup.picasso.Picasso
import im.fdx.v2ex.MyApp

/**
 * Created by fdx on 2017/7/3.
 * fdx will maintain it
 */

fun ImageView.load(url: String?) {
    Picasso.with(MyApp.get()).load(url).into(this)
}