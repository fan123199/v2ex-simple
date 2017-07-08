package im.fdx.v2ex.view

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import im.fdx.v2ex.R


/**
 * Created by fdx on 2017/3/23.
 * 无法设置文字颜色
 */
class CustomChrome(private val context: Context) {
    private val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

    init {
        builder.setShowTitle(true)
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.chrome_tab))
        builder.addDefaultShareMenuItem()
    }

    fun load(url: String) {
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
