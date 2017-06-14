package im.fdx.v2ex.view

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import im.fdx.v2ex.MyReceiver
import im.fdx.v2ex.R


/**
 * Created by fdx on 2017/3/23.
 * 无法设置文字颜色
 */
class CustomChrome(private val context: Context) {
    private val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
    private val sendIntent: Intent

    init {
        builder.setShowTitle(true)
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary))

        val icon = getBitmap(context, R.drawable.ic_share_24dp)
        sendIntent = Intent(context, MyReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context, 0,
                sendIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setActionButton(icon, "分享该页面", pendingIntent, true)
    }

    fun load(url: String) {
        sendIntent.data = Uri.parse(url)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {

        vectorDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.DST)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun getBitmap(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is VectorDrawable) {
            return getBitmap(drawable)
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }
}
