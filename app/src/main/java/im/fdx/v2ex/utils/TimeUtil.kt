package im.fdx.v2ex.utils

import android.text.format.DateUtils
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.utils.extensions.getNum
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
        var _created = created
        if (_created == -1L) {
            return ""
        }

        _created *= 1000
        val now = System.currentTimeMillis()
        val difference = now - _created
        val text = if (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS)
            MyApp.get().getString(R.string.just_now)
        else
            DateUtils.getRelativeTimeSpanString(
                    _created,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE)

        return text.toString()
    }


    fun getAbsoluteTime(created: Long): String {
        var _created = created
        _created *= 1000

        //        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.MEDIUM, Locale.CHINA);
        val format1 = SimpleDateFormat("yyyy/MM/dd")
        return format1.format(_created)
    }

    /**
     * 遗憾的是只能通过这样得到一个不准确的时间。
     * 坑爹的char，让我卡了好久
     * 算出绝对时间是为了保存如缓存，不然可以直接用得到的时间展示。

     * @param timeStr 两个点中间的字符串，包括空格
     * *
     * @return long value
     */
    fun toLong(timeStr: String): Long {
        var theTime = timeStr

        theTime = theTime.trim()
        //       String theTime = time.replace("&nbsp", "");
        //        44 分钟前用 iPhone 发布
        //         · 1 小时 34 分钟前 · 775 次点击
        //         · 100 天前 · 775 次点击
        //       1992.02.03 12:22:22 +0800
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
                    created = date.time / 1000
                }
            }
        } catch (ignored: Exception) {
            XLog.tag("TimeUtil").e("time str error: $theTime, $timeStr")
        }

        return created
    }


    /**
     * 在一堆奇怪的字符串里获取数字。（前后不一定都是空格）
     * @param str
     * *
     * @return
     */
    //    fun getNum(str: String): String {
//        var str2 = ""
//        if (str.isNotBlank()) {
//            (0..str.length - 1)
//                    .filter { str[it].toInt() in 48..57 }
//                    .forEach { str2 += str[it] }
//        }
//        return str2
//    }
}
