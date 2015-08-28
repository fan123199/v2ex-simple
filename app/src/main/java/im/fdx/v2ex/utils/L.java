package im.fdx.v2ex.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by fdx on 2015/8/18.
 */
public class L {
    public static void m(String message) {
        Log.d("MY_DEBUG ", "" + message);
    }

    public static void t(Context context, String message) {
        Toast.makeText(context,"" + message, Toast.LENGTH_SHORT).show();
    }
    public static void T(Context context, String message) {
        Toast.makeText(context,"" + message, Toast.LENGTH_LONG).show();
    }
}
