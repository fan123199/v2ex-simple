package im.fdx.v2ex.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import im.fdx.v2ex.R
import org.jetbrains.anko.browse


/**
 * Created by fdx on 2017/3/23.
 * 无法设置文字颜色
 */
class CustomChrome(private val context: Context) {
    private val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

    init {
        builder.setShowTitle(true)
        builder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.chrome_tab)).build()
        )
        builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)
    }

    fun load(url: String) {

        val customTabsIntent = builder.build()
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            context.browse(url)
        }
    }
}
