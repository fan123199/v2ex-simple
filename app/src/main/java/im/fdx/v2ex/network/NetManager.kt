package im.fdx.v2ex.network

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import im.fdx.v2ex.R
import im.fdx.v2ex.myApp
import okhttp3.*
import org.jetbrains.anko.toast
import java.io.IOException

/**
 * Created by a708 on 15-8-13.
 * 用于对API和网页处理的类
 *
 * 这是最苦力活的一个类，而且很有可能会继续改变.
 * 由于v2ex提供的接口不够实时，采用解析网页方式 。
 */

object NetManager {

    const val HTTPS_V2EX_BASE = "https://www.v2ex.com"

    const val API_TOPIC = "$HTTPS_V2EX_BASE/api/topics/show.json"

    const val DAILY_CHECK = "$HTTPS_V2EX_BASE/mission/daily"

    const val SIGN_UP_URL = "$HTTPS_V2EX_BASE/signup"

    const val SIGN_IN_URL = "$HTTPS_V2EX_BASE/signin"
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    const val API_USER = "$HTTPS_V2EX_BASE/api/members/show.json"

    //相当于首页右侧的 10 大每天的内容。
    const val API_HEATED = "$HTTPS_V2EX_BASE/api/topics/hot.json"

    @Deprecated("不用API，用html解析。虽慢，但统一")
    const val URL_ALL_NODE = "$HTTPS_V2EX_BASE/api/nodes/all.json"

    const val URL_ALL_NODE_WEB = "$HTTPS_V2EX_BASE/planes"

    const val URL_FOLLOWING = "$HTTPS_V2EX_BASE/my/following"

    /**
     * 第三方提供的好用的搜索api, 在app中默认使用最近帖子 todo 以后增加多filter功能
     */
    const val API_SEARCH_HOST = "www.sov2ex.com"

    var myGson = Gson()

    @JvmOverloads
    fun dealError(context: Context?,  errorCode: Int = -1, swipe: SwipeRefreshLayout? = null, errorMsg:String ?= "") {

        if (context is Activity && !context.isFinishing) {
            context.runOnUiThread {
                swipe?.isRefreshing = false
                if(errorMsg != null) {
                    context.toast(errorMsg)
                    return@runOnUiThread
                }

                when (errorCode) {
                    -1 -> context.toast(context.getString(R.string.error_network))
                    302 -> context.toast(context.getString(R.string.error_auth_failure))
                    else -> context.toast(context.getString(R.string.error_network))
                }
            }
        }
    }
}