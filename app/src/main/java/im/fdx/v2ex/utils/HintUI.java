package im.fdx.v2ex.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by fdx on 2015/8/18.
 * Toast或Snackbar的封装
 */
public class HintUI {

    public static void t(Context context, String message) {
        Toast.makeText(context,"" + message, Toast.LENGTH_SHORT).show();
    }
    public static void T(Context context, String message) {
        Toast.makeText(context,"" + message, Toast.LENGTH_LONG).show();
    }

    public static void s(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void S(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
