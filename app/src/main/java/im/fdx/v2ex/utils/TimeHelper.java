package im.fdx.v2ex.utils;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import im.fdx.v2ex.R;

/**
 * Created by a708 on 15-9-9.
 * 获取相对时间
 */
public class TimeHelper {

    public static String getRelativeTime(Context context, long created) {
        created = created * 1000;
        long now = System.currentTimeMillis();
        long difference = now - created;
        CharSequence text = (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS) ?
                context.getString(R.string.just_now) :
                DateUtils.getRelativeTimeSpanString(
                        created,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);

        return text.toString();
    }


    public static String getAbsoluteTime(long created) {
        created *= 1000;

        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.MEDIUM,Locale.CHINA);
        return format.format(created);


    }
}
