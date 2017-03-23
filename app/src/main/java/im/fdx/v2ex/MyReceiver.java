package im.fdx.v2ex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 这是我见过最曲线救国的方法了。
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getDataString();
        if (url == null) {
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        Intent chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_to));
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooserIntent);
    }
}