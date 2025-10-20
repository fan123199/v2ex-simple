package im.fdx.v2ex.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.getColorFromAttr


/**
 * Created by fdx on 2017/3/23.
 * 无法设置文字颜色
 */
class CustomChrome(private val context: Context) {
    private val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

    init {
        val typedValue = TypedValue()
        val resolved = context.theme.resolveAttribute(R.attr.toolbar_background, typedValue, true)
        val colorBackground: Int
        if (resolved) {
            if (typedValue.resourceId != 0) {
                colorBackground = ContextCompat.getColor(context, typedValue.resourceId)
            } else {
                colorBackground = typedValue.data
            }
        } else {
            colorBackground = ContextCompat.getColor(context, android.R.color.white)
        }
        builder.setShowTitle(true)
        builder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(context.getColorFromAttr(R.attr.toolbar_background, R.color.chrome_tab)).build()
        )
        builder.setShareState(CustomTabsIntent.SHARE_STATE_ON)
    }

    fun load(url: String) {

        val customTabsIntent = builder.build()
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}
