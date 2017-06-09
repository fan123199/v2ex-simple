package im.fdx.v2ex.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import im.fdx.v2ex.MyReceiver;
import im.fdx.v2ex.R;

/**
 * Created by fdx on 2017/3/23.
 */
public class CustomChrome {

    private Context context;
    private final CustomTabsIntent.Builder builder;
    private final Intent sendIntent;

    public CustomChrome(Context context) {
        this.context = context;
        builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary));

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_share_white_24dp);
        sendIntent = new Intent(context, MyReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setActionButton(icon, "分享该页面", pendingIntent, true);
    }

    public void load(String url) {
        sendIntent.setData(Uri.parse(url));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
