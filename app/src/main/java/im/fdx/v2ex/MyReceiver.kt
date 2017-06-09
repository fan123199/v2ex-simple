package im.fdx.v2ex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 * 这是我见过最曲线救国的方法了。
 */
class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val url = intent.dataString ?: return
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_to))
        chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooserIntent)
    }
}