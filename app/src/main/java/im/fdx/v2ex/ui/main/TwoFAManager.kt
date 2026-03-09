package im.fdx.v2ex.ui.main

import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.loge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 全局两步验证管理器。
 * 当任意网络请求检测到 302→/2fa 时调用 [requestTwoFA]，
 * 通过 AtomicBoolean 保证只弹出一个对话框。
 * 验证成功后自动触发所有已注册的重试回调。
 */
object TwoFAManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    /** 防止多个请求同时弹出对话框 */
    private val dialogRequested = AtomicBoolean(false)

    /** 验证成功后需要重试的回调列表 */
    private val retryCallbacks = mutableListOf<() -> Unit>()

    /**
     * 当检测到 302→/2fa 时调用。
     * 只有第一次调用会弹出对话框，后续调用仅注册重试回调。
     * @param onRetry 验证成功后执行的重试回调（通常是 refresh()）
     */
    fun requestTwoFA(onRetry: () -> Unit) {
        synchronized(retryCallbacks) {
            retryCallbacks.add(onRetry)
        }
        if (dialogRequested.compareAndSet(false, true)) {
            _errorMsg.value = null
            _showDialog.value = true
        }
    }

    /**
     * 用户提交两步验证码
     */
    fun submitCode(code: String) {
        _isVerifying.value = true
        _errorMsg.value = null
        scope.launch {
            try {
                val twoStepUrl = "https://www.v2ex.com/2fa"

                // 1. GET /2fa 页面，获取 once token
                val noRedirectClient = HttpHelper.OK_CLIENT.newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build()

                val getResponse = HttpHelper.OK_CLIENT.newCall(
                    Request.Builder().url(twoStepUrl).build()
                ).execute()

                if (getResponse.code != 200) {
                    _isVerifying.value = false
                    _errorMsg.value = "无法获取验证页面 (${getResponse.code})"
                    return@launch
                }

                val bodyStr = getResponse.body.string()
                val once = Parser(bodyStr).getOnceNum()

                // 2. POST /2fa 提交验证码
                val postBody = FormBody.Builder()
                    .add("code", code)
                    .add("once", once)
                    .build()

                val postResponse = noRedirectClient.newCall(
                    Request.Builder()
                        .post(postBody)
                        .url(twoStepUrl)
                        .build()
                ).execute()

                _isVerifying.value = false

                if (postResponse.code == 302) {
                    // 验证成功
                    logd("2FA verification successful")
                    setLogin(true)
                    _showDialog.value = false
                    dialogRequested.set(false)

                    // 触发所有等待中的重试
                    val callbacks: List<() -> Unit>
                    synchronized(retryCallbacks) {
                        callbacks = retryCallbacks.toList()
                        retryCallbacks.clear()
                    }
                    callbacks.forEach { it.invoke() }
                } else {
                    _errorMsg.value = "验证码错误，请重试"
                }
            } catch (e: Exception) {
                loge("2FA submit error: ${e.message}")
                _isVerifying.value = false
                _errorMsg.value = "网络错误: ${e.message}"
            }
        }
    }

    /**
     * 用户取消对话框
     */
    fun dismiss() {
        _showDialog.value = false
        _isVerifying.value = false
        _errorMsg.value = null
        dialogRequested.set(false)
        synchronized(retryCallbacks) {
            retryCallbacks.clear()
        }
    }
}
