package im.fdx.v2ex.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.utils.extensions.await
import im.fdx.v2ex.utils.extensions.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class DailyViewModel : ViewModel() {

    private val _toastMsg = MutableSharedFlow<String>()
    val toastMsg: SharedFlow<String> = _toastMsg.asSharedFlow()

    fun startDailyCheck() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    HttpHelper.OK_CLIENT.newCall(Request.Builder().url(NetManager.DAILY_CHECK).build()).await()
                }
                if (response.code == 302) {
                    _toastMsg.emit("Need Login first")
                    return@launch
                }
                val body = response.body?.string() ?: ""

                if (body.contains("每日登录奖励已领取")) {
                    _toastMsg.emit("已领取, 明日再来")
                    return@launch
                }

                val parser = Parser(body)
                val once = parser.parseDailyOnce()

                if (once != null) {
                    checkIn(once)
                } else {
                    _toastMsg.emit("Parsing error, cannot find 'once'")
                }

            } catch (e: Exception) {
                _toastMsg.emit(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun checkIn(once: String) {
        try {
            val url = "${NetManager.DAILY_CHECK}/redeem?once=$once"
            withContext(Dispatchers.IO) {
                HttpHelper.OK_CLIENT.newCall(Request.Builder().url(url)
                    .header("Referer", NetManager.DAILY_CHECK)
                    .build()).await()
            }
            _toastMsg.emit("每日登录奖励领取成功")
        } catch (e: Exception) {
            _toastMsg.emit("Check-in failed: ${e.message}")
        }
    }
}
