package im.fdx.v2ex.utils

import android.text.format.DateUtils
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.getNum
import java.text.ParseException
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
        if (created <= 0) {
            return ""
        }

        val now = System.currentTimeMillis()
        val diff = (now - created) / 1000   //second
        val day = diff / (24 * 60 * 60)
        val hour = diff % (24 * 60 * 60) / 3600
        val minute = diff % (60 * 60) / 60
        val second = diff
        var timeStr = ""
        if (day in 1..365) {
            timeStr = day.toString() + "天前";
            return timeStr
        }
        if (day > 365) {
            timeStr = SimpleDateFormat.getDateTimeInstance().format(created)
            return timeStr
        }

        if (hour == 0L && minute == 0L) {
            if (second < 15)
                return "刚刚" else return "几秒前"
        }

        if (hour > 0) {
            timeStr = hour.toString() + "小时前";
        }
        if (minute > 0) {
            timeStr = timeStr.removeSuffix("前") + minute.toString() + "分钟前";
        }
        return timeStr
    }


    /**
     * created : 这个是来自于 v2ex api 的变量
     */
    fun getAbsoluteTime(created: String): String {
        val createdNum = created.toLongOrNull() ?: return ""
        val obj = 1000 * createdNum
        val format1 = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        format1.timeZone = TimeZone.getTimeZone("GMT+8:00")
        return format1.format(obj)
    }

    /**
     * 遗憾的是只能通过这样得到一个不准确的时间。
     * 坑爹的char，让我卡了好久
     * 算出绝对时间是为了保存如缓存，不然可以直接用得到的时间展示。

     * @param timeStr 两个点中间的字符串，包括空格
     * *
     * @return long value
     */
    fun toUtcTime(timeStr: String?): Long {
        if (timeStr == null) return 0L
        var theTime = timeStr

        theTime = theTime.trim()
        //       String theTime = time.replace("&nbsp", "");
        //        44 分钟前用 iPhone 发布
        //         · 1 小时 34 分钟前 · 775 次点击
        //         · 100 天前 · 775 次点击
        //       1992.02.03 12:22:22 +0800
        //       2017-09-26 22:27:57 PM
        //      刚刚
        //其中可能出现一些奇怪的字符，你可能以为是空格。
        var created = System.currentTimeMillis() / 1000 // ms -> second

        val second = theTime.indexOf("秒")
        val hour = theTime.indexOf("小时")
        val minute = theTime.indexOf("分钟")
        val day = theTime.indexOf("天")
        val now = theTime.indexOf("刚刚")

        try {
            when {
//                theTime.isEmpty() -> return System.currentTimeMillis()/1000
                second != -1 -> return created
                hour != -1 -> created -= theTime.substring(0, hour).getNum().toLong() * 60 * 60 +
                        theTime.substring(hour + 2, minute).getNum().toLong() * 60
                day != -1 -> created -= theTime.substring(0, day).getNum().toLong() * 60 * 60 * 24
                minute != -1 -> created -= theTime.substring(0, minute).getNum().toLong() * 60
                now != -1 -> return created
                else -> {
                    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss +08:00", Locale.getDefault())
                    val date = sdf.parse(theTime.trim())
                    created = date?.time?.div(1000) ?: 0
                }
            }
        } catch (e1: NumberFormatException) {
            XLog.tag("TimeUtil").e("NumberFormatException error: $theTime, $timeStr")
        } catch (e2: StringIndexOutOfBoundsException) {
            XLog.tag("TimeUtil").e(" StringIndexOutOfBoundsException error: $theTime, $timeStr")
        } catch (e2: ParseException) {
            try {
                val ccc = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US)
                val date = ccc.parse(theTime.trim())
                created = date?.time?.div(1000) ?: 0
            } catch (ignore: ParseException) {
                try {
                    val ccc = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US)
                    val date = ccc.parse(theTime.trim())
                    created = date?.time?.div(1000) ?: 0
                } catch (ignre: ParseException) {
                    XLog.tag("TimeUtil").e("time str parse error: $theTime")
                }
            }
        }

        return created
    }


    fun toUtcTime2(timeStr: String?): Long {
        if (timeStr == null) return 0L
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return try {
            val date = format.parse(timeStr)
            date!!.time
        } catch (e: Exception) {
            0L
        }
    }
}
