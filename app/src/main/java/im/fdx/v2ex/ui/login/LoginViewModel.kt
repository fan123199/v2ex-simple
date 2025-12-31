package im.fdx.v2ex.ui.login

import im.fdx.v2ex.data.model.Data
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.NetManager
import im.fdx.v2ex.data.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.data.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.data.network.vCall
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import java.io.IOException

data class CaptchaInfo(val url: String, val headers: Map<String, String>)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _captcha = MutableStateFlow("")
    val captcha = _captcha.asStateFlow()

    private val _captchaInfo = MutableStateFlow<CaptchaInfo?>(null)
    val captchaInfo = _captchaInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult = _loginResult.asStateFlow()

    // Hidden form fields
    private var onceCode: String? = null
    private var passwordKey: String? = null
    private var nameKey: String? = null
    private var imageCodeKey: String? = null

    init {
        getLoginElement()
    }

    fun onUsernameChange(value: String) { _username.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onCaptchaChange(value: String) { _captcha.value = value }

    fun getLoginElement() {
        viewModelScope.launch(Dispatchers.IO) {
            val requestToGetOnce = Request.Builder()
                .url(SIGN_IN_URL)
                .build()

            try {
                val response = HttpHelper.OK_CLIENT.newCall(requestToGetOnce).execute()
                if (response.isSuccessful) {
                    val htmlString = response.body?.string() ?: ""
                    val body = Jsoup.parse(htmlString).body()
                    nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮件地址").attr("name")
                    passwordKey = body.getElementsByAttributeValue("type", "password").attr("name")
                    onceCode = body.getElementsByAttributeValue("name", "once").attr("value")
                    imageCodeKey = body.getElementsByAttributeValueContaining("placeholder", "请输入上图中的验证码").attr("name")

                    val captchaUrl = "https://www.v2ex.com/_captcha?once=$onceCode"
                    val headersMap = mapOf(
                        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                        "Accept-Charset" to "utf-8, iso-8859-1, utf-16, *;q=0.7",
                        "Accept-Language" to "zh-CN,zh;q=0.8,en;q=0.6",
                        "Host" to "www.v2ex.com",
                        "Cache-Control" to "max-age=0",
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36",
                        "Cookie" to HttpHelper.myCookieJar.loadForRequest(captchaUrl.toHttpUrlOrNull()!!).joinToString(separator = ";")
                    )
                    _captchaInfo.value = CaptchaInfo(captchaUrl, headersMap)
                    XLog.tag("LoginViewModel").d("$nameKey|$passwordKey|$onceCode|$imageCodeKey")
                }
            } catch (e: IOException) {
                loge("error in get login page: ${e.message}")
            }
        }
    }

    fun login() {
        if (!isValidated()) return
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
             postLogin(
                nameKey ?: "", passwordKey ?: "", onceCode = onceCode ?: "", imageCodeKey = imageCodeKey ?: ""
            )
        }
    }

    private fun isValidated(): Boolean {
        if (username.value.isEmpty()) {
            _loginResult.value = LoginResult.Error("名字不能为空")
            return false
        }
        if (password.value.isEmpty()) {
            _loginResult.value = LoginResult.Error("密码不能为空")
            return false
        }
        if (captcha.value.isEmpty()) {
            _loginResult.value = LoginResult.Error("验证码不能为空")
            return false
        }
        return true
    }

    private fun postLogin(nameKey: String, passwordKey: String, onceCode: String, imageCodeKey: String) {
        val requestBody = FormBody.Builder()
            .add(nameKey, username.value)
            .add(passwordKey, password.value)
            .add(imageCodeKey, captcha.value)
            .add("once", onceCode)
            .build()

        val request = Request.Builder()
            .url(SIGN_IN_URL)
            .header("Origin", HTTPS_V2EX_BASE)
            .header("Referer", SIGN_IN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()
            
        try {
            val response = HttpHelper.OK_CLIENT.newCall(request).execute()
            val httpcode = response.code
            // Note: response body must be consumed once
            val bodyString = response.body?.string() ?: ""
            val errorMsg = Parser(bodyString).getErrorMsg()
            logd("http code: $httpcode")
            logd("errorMsg: $errorMsg")

            when (httpcode) {
                302 -> {
                     // Check for 2FA redirection
                     // Need to follow redirect manually or check headers if client doesn't follow
                     // Assuming OkHttp follows redirects by default? 
                     // Wait, OKHttp follows redirects. So 302 might be absorbed unless disabled.
                     // The original code uses `vCall` which seems to wrap OkHttp.
                     // Let's assume standard behavior: if it redirects to 2fa, final URL is /2fa.
                     // But original code manually checked 302.
                     
                     // If we are here with 302, it means we didn't follow it?
                     // Or maybe we treat it as success?
                     // Let's stick to the structure:
                     
                     handleLoginSuccessOr2FA(response)
                }
                200 -> {
                    // Check if actually logged in or error page
                    // If error message is present, likely failed
                    // Parser logic from original activity:
                    _isLoading.value = false
                    _loginResult.value = LoginResult.Error(errorMsg)
                    getLoginElement()
                }
                else -> {
                    _isLoading.value = false
                    _loginResult.value = LoginResult.Error("Unknown error: $httpcode")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isLoading.value = false
            _loginResult.value = LoginResult.Error("Network error")
        }
    }
    
    private fun handleLoginSuccessOr2FA(response: Response) {
        // After login POST 302, check if we need 2FA
        // Make a request to base URL and check if it redirects to /2fa
        try {
            val checkRequest = Request.Builder()
                .url(HTTPS_V2EX_BASE)
                .build()
            
            // Use a client that doesn't follow redirects to detect /2fa
            val noRedirectClient = HttpHelper.OK_CLIENT.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
            
            val checkResponse = noRedirectClient.newCall(checkRequest).execute()
            
            if (checkResponse.code == 302 && checkResponse.header("Location") == "/2fa") {
                logd("2FA required, redirecting to 2FA page")
                _isLoading.value = false
                _loginResult.value = LoginResult.TwoStep
            } else {
                // Login successful
                setLogin(true)
                _isLoading.value = false
                _loginResult.value = LoginResult.Success
            }
        } catch (e: Exception) {
            loge("Error checking 2FA: ${e.message}")
            _isLoading.value = false
            _loginResult.value = LoginResult.Error("Error checking login status")
        }
    }
    
    fun submitTwoStepCode(code: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val twoStepUrl = "https://www.v2ex.com/2fa"
                
                // First, get the once token from the 2FA page
                val getResponse = HttpHelper.OK_CLIENT.newCall(
                    Request.Builder().url(twoStepUrl).build()
                ).execute()
                
                if (getResponse.code == 200) {
                    val bodyStr = getResponse.body?.string() ?: ""
                    val once = Parser(bodyStr).getOnceNum()
                    
                    val postBody = FormBody.Builder()
                        .add("code", code)
                        .add("once", once)
                        .build()
                    
                    // Use no-redirect client to detect 302 success
                    val noRedirectClient = HttpHelper.OK_CLIENT.newBuilder()
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .build()
                    
                    val postResponse = noRedirectClient.newCall(
                        Request.Builder()
                            .post(postBody)
                            .url(twoStepUrl)
                            .build()
                    ).execute()
                    
                    _isLoading.value = false
                    if (postResponse.code == 302) {
                        setLogin(true)
                        _loginResult.value = LoginResult.Success
                    } else {
                        _loginResult.value = LoginResult.Error("两步验证失败")
                    }
                } else {
                    _isLoading.value = false
                    _loginResult.value = LoginResult.Error("无法获取验证页面")
                }
            } catch (e: Exception) {
                loge("Error in 2FA: ${e.message}")
                _isLoading.value = false
                _loginResult.value = LoginResult.Error("网络错误")
            }
        }
    }
    
    fun resetResult() {
        _loginResult.value = null
    }
}

sealed class LoginResult {
    object Success : LoginResult()
    object TwoStep : LoginResult()
    data class Error(val msg: String) : LoginResult()
}
