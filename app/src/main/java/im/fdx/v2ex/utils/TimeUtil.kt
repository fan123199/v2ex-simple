package im.fdx.v2ex.utils

import android.text.format.DateUtils
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by a708 on 15-9-9.
 * 获取相对时间
 */
object TimeUtil {


    /**
     * @param created 若等-1 （目前设定），则为没有回复。
     * *
     * @return
     */
    fun getRelativeTime(created: Long): String {
        var created = created
        if (created == -1L) {
            return ""
        }

        created *= 1000
        val now = System.currentTimeMillis()
        val difference = now - created
        val text = if (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS)
            MyApp.get().getString(R.string.just_now)
        else
            DateUtils.getRelativeTimeSpanString(
                    created,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE)

        return text.toString()
    }


    fun getAbsoluteTime(created: Long): String {
        var created = created
        created *= 1000

        //        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.MEDIUM, Locale.CHINA);
        val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format1.format(created)
    }

    /**
     * 遗憾的是只能通过这样得到一个不准确的时间。
     * 坑爹的char，让我卡了好久
     * 算出绝对时间是为了保存如缓存，不然可以直接用得到的时间展示。

     * @param timeStr 两个点中间的字符串，包括空格
     * *
     * @return long value
     */
    @Throws(NumberFormatException::class)
    fun toLong(timeStr: String): Long {
        var timeStr = timeStr

        timeStr = timeStr.trim { it <= ' ' }
        //       String timeStr = time.replace("&nbsp", "");
        //        44 分钟前用 iPhone 发布
        //         · 1 小时 34 分钟前 · 775 次点击
        //         · 100 天前 · 775 次点击
        //       1992.02.03 12:22:22 +0800
        //      刚刚
        //其中可能出现一些奇怪的字符，你可能以为是空格。
        var created = System.currentTimeMillis() / 1000 // ms -> second

        val second = timeStr.indexOf("秒")
        val hour = timeStr.indexOf("小时")
        val minute = timeStr.indexOf("分钟")
        val day = timeStr.indexOf("天")

        val now = timeStr.indexOf("刚刚")

        try {
            if (second != -1) {
                return created
            } else if (hour != -1) {
                created -= java.lang.Long.parseLong(getNum(timeStr.substring(0, hour))) * 60 * 60 + java.lang.Long.parseLong(getNum(timeStr.substring(hour + 2, minute))) * 60
            } else if (day != -1) {
                created -= java.lang.Long.parseLong(getNum(timeStr.substring(0, day))) * 60 * 60 * 24
            } else if (minute != -1) {
                created -= java.lang.Long.parseLong(getNum(timeStr.substring(0, minute))) * 60
            } else if (now != -1) {
                return created
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss +08:00", Locale.getDefault())
                val date = sdf.parse(timeStr)
                created = date.time / 1000
            }
        } catch (ignored: Exception) {
            XLog.tag("TimeUtil").e("time str error: |" + timeStr)
        }

        return created
    }


    /**
     * 在一堆奇怪的字符串里获取数字。（前后不一定都是空格）
     * @param str
     * *
     * @return
     */
    fun getNum(str: String): String {
        var str2 = ""
        if (str.isNotBlank()) {
            (0..str.length - 1)
                    .filter { str[it].toInt() in 48..57 }
                    .forEach { str2 += str[it] }
        }
        return str2
    }
}
