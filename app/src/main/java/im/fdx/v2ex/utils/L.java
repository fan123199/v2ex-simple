package im.fdx.v2ex.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by fdx on 2015/8/18.
 * 简化调试的工具类
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

    public static void s(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
    }

    public static void S(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    }
}
