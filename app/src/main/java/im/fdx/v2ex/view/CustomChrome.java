package im.fdx.v2ex.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;

import im.fdx.v2ex.MyReceiver;
import im.fdx.v2ex.R;

/**
 * Created by fdx on 2017/3/23.
 */
public class CustomChrome {

    private Context context;
    static CustomChrome customChrome;

    public CustomChrome(Context context) {
        this.context = context;
    }


    public static synchronized CustomChrome getInstance(Context context) {
        if (customChrome == null) {
            customChrome = new CustomChrome(context);
        }
        return customChrome;
    }

    public void load(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        builder.setShowTitle(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setToolbarColor(context.getColor(R.color.primary));
        } else {
            builder.setToolbarColor(context.getResources().getColor(R.color.primary));
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_share_white_24dp);
        Intent sendIntent = new Intent(context, MyReceiver.class);
        sendIntent.setData(Uri.parse(url));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setActionButton(icon, "分享该页面", pendingIntent, true);


        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
