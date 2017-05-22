package im.fdx.v2ex.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.IntegerRes;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;

import im.fdx.v2ex.R;

/**
 * Created by fdx on 2017/4/29.
 * <p>
 * pixel 和 dp 转换
 */

public class ViewUtil {


    public static int px2dp(int px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }


    public static int[] getScreenSize() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        XLog.i("height " + height + "width" + width);


        return new int[]{height, width};
    }

    public static int dp2px(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static void showNoContent(Activity activity, FrameLayout container) {
        final TextView child = new TextView(activity);
        child.setText("没有内容");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            child.setTextColor(activity.getColor(R.color.hint));
        } else {
            child.setTextColor(activity.getResources().getColor(R.color.hint));
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = dp2px(80);
        container.addView(child, params);
    }

//    /**
//     *  必须在主线程, 不能用在fragment
//     * @param activity
//     * @param resId
//     */
//    public static void showNoContent(final Activity activity, int resId) {
//        RelativeLayout layout = (RelativeLayout) activity.findViewById(resId);
//        showNoContent(activity,layout);
//    }
}
