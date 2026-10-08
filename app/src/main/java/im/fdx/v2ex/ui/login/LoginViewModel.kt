package im.fdx.v2ex.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import im.fdx.v2ex.R
import im.fdx.v2ex.data.network.HttpHelper
import im.fdx.v2ex.data.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.data.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.data.network.Parser
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.utils.extensions.loge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.jsoup.Jsoup

data class CaptchaInfo(val url: String, val headers: Map<String, String>)

data class LoginFieldErrors(
    val username: Boolean = false,
    val password: Boolean = false,
    val captcha: Boolean = false,
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _captcha = MutableStateFlow("")
    val captcha = _captcha.asStateFlow()

    private val _captchaInfo = MutableStateFlow<CaptchaInfo?>(null)
    val captchaInfo = _captchaInfo.asStateFlow()

    private val _isCaptchaLoading = MutableStateFlow(false)
    val isCaptchaLoading = _isCaptchaLoading.asStateFlow()

    private val _captchaError = MutableStateFlow<String?>(null)
    val captchaError = _captchaError.asStateFlow()

    private val _fieldErrors = MutableStateFlow(LoginFieldErrors())
    val fieldErrors = _fieldErrors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult = _loginResult.asStateFlow()

    private data class LoginForm(
        val nameKey: String,
        val passwordKey: String,
        val onceCode: String,
        val imageCodeKey: String,
        val captchaInfo: CaptchaInfo,
    )

    private data class LoginAttempt(val result: LoginResult, val refreshCaptcha: Boolean = false)

    private var loginForm: LoginForm? = null
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
        getLoginElement()
    }

    fun getGoogleSignInUrl(): String {
        val url = remoteConfig.getString("google_sign_in_url").toHttpUrlOrNull()
        return if (url != null && url.isHttps && url.port == 443 &&
            url.username.isEmpty() && url.password.isEmpty() &&
            url.host in setOf("v2ex.com", "www.v2ex.com", "edge.v2ex.com") &&
            url.encodedPath in setOf("/signin", "/auth/google")
        ) url.toString() else SIGN_IN_URL
    }

    fun onUsernameChange(value: String) {
        _username.value = value
        _fieldErrors.value = _fieldErrors.value.copy(username = false)
        clearLoginError()
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _fieldErrors.value = _fieldErrors.value.copy(password = false)
        clearLoginError()
    }

    fun onCaptchaChange(value: String) {
        _captcha.value = value
        _fieldErrors.value = _fieldErrors.value.copy(captcha = false)
        clearLoginError()
    }

    private fun clearLoginError() {
        if (_loginResult.value is LoginResult.Error) _loginResult.value = null
    }

    fun getLoginElement() {
        if (_isLoading.value || _isCaptchaLoading.value) return
        _isCaptchaLoading.value = true
        _captchaError.value = null
        _captcha.value = ""
        _captchaInfo.value = null
        _fieldErrors.value = _fieldErrors.value.copy(captcha = false)
        loginForm = null

        viewModelScope.launch {
            try {
                val form = withContext(Dispatchers.IO) { loadLoginForm() }
                loginForm = form
                _captchaInfo.value = form.captchaInfo
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loge("Error loading login page: ${e.message}")
                _captchaError.value = getApplication<Application>().getString(R.string.login_captcha_load_failed)
            } finally {
                _isCaptchaLoading.value = false
            }
        }
    }

    private fun loadLoginForm(): LoginForm {
        val request = Request.Builder().url(SIGN_IN_URL).build()
        return HttpHelper.OK_CLIENT.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Login page returned ${response.code}" }
            val body = Jsoup.parse(response.body.string()).body()
            val nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮件地址").attr("name")
            val passwordKey = body.getElementsByAttributeValue("type", "password").attr("name")
            val onceCode = body.getElementsByAttributeValue("name", "once").attr("value")
            val imageCodeKey = body.getElementsByAttributeValueContaining("placeholder", "请输入上图中的验证码").attr("name")
            check(listOf(nameKey, passwordKey, onceCode, imageCodeKey).all { it.isNotBlank() }) {
                "Login form is incomplete"
            }

            val captchaUrl = "$HTTPS_V2EX_BASE/_captcha?once=$onceCode"
            val headers = mapOf(
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Accept-Charset" to "utf-8, iso-8859-1, utf-16, *;q=0.7",
                "Accept-Language" to "zh-CN,zh;q=0.8,en;q=0.6",
                "Host" to "www.v2ex.com",
                "Cache-Control" to "max-age=0",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36",
                "Cookie" to HttpHelper.myCookieJar.loadForRequest(captchaUrl.toHttpUrl()).joinToString(separator = ";"),
            )
            LoginForm(nameKey, passwordKey, onceCode, imageCodeKey, CaptchaInfo(captchaUrl, headers))
        }
    }

    fun login() {
        if (_isLoading.value || _isCaptchaLoading.value) return
        if (!isValidated()) return
        val form = loginForm
        if (form == null) {
            _captchaError.value = getApplication<Application>().getString(R.string.login_captcha_load_failed)
            return
        }

        val username = _username.value
        val password = _password.value
        val captcha = _captcha.value
        _isLoading.value = true
        _loginResult.value = null
        viewModelScope.launch {
            val attempt = try {
                withContext(Dispatchers.IO) { postLogin(form, username, password, captcha) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loge("Error logging in: ${e.message}")
                LoginAttempt(LoginResult.Error(getApplication<Application>().getString(R.string.error_network)))
            } finally {
                _isLoading.value = false
            }
            if (attempt.result is LoginResult.Success) setLogin(true)
            _loginResult.value = attempt.result
            if (attempt.refreshCaptcha) getLoginElement()
        }
    }

    private fun isValidated(): Boolean {
        val errors = LoginFieldErrors(
            username = _username.value.isBlank(),
            password = _password.value.isEmpty(),
            captcha = _captcha.value.isBlank(),
        )
        _fieldErrors.value = errors
        return !errors.username && !errors.password && !errors.captcha
    }

    private fun postLogin(form: LoginForm, username: String, password: String, captcha: String): LoginAttempt {
        val requestBody = FormBody.Builder()
            .add(form.nameKey, username)
            .add(form.passwordKey, password)
            .add(form.imageCodeKey, captcha)
            .add("once", form.onceCode)
            .build()
        val request = Request.Builder()
            .url(SIGN_IN_URL)
            .header("Origin", HTTPS_V2EX_BASE)
            .header("Referer", SIGN_IN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()

        return HttpHelper.OK_CLIENT.newCall(request).execute().use { response ->
            when (response.code) {
                302 -> LoginAttempt(handleLoginSuccessOr2FA())
                200 -> {
                    val error = Parser(response.body.string()).getErrorMsg().ifBlank {
                        getApplication<Application>().getString(R.string.login_failed)
                    }
                    LoginAttempt(LoginResult.Error(error), refreshCaptcha = true)
                }
                else -> LoginAttempt(LoginResult.Error(getApplication<Application>().getString(R.string.login_failed)))
            }
        }
    }

    private fun handleLoginSuccessOr2FA(): LoginResult {
        return try {
            val checkRequest = Request.Builder().url(HTTPS_V2EX_BASE).build()
            val noRedirectClient = HttpHelper.OK_CLIENT.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
            noRedirectClient.newCall(checkRequest).execute().use { response ->
                if (response.code == 302 && response.header("Location") == "/2fa") {
                    LoginResult.TwoStep
                } else {
                    LoginResult.Success
                }
            }
        } catch (e: Exception) {
            loge("Error checking 2FA: ${e.message}")
            LoginResult.Error(getApplication<Application>().getString(R.string.login_status_check_failed))
        }
    }

    fun submitTwoStepCode(code: String) {
        if (_isLoading.value || code.isBlank()) return
        _isLoading.value = true
        _loginResult.value = null
        viewModelScope.launch {
            val result = try {
                withContext(Dispatchers.IO) { postTwoStepCode(code) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loge("Error in 2FA: ${e.message}")
                LoginResult.Error(getApplication<Application>().getString(R.string.error_network))
            } finally {
                _isLoading.value = false
            }
            if (result is LoginResult.Success) setLogin(true)
            _loginResult.value = result
        }
    }

    private fun postTwoStepCode(code: String): LoginResult {
        val twoStepUrl = "$HTTPS_V2EX_BASE/2fa"
        val once = HttpHelper.OK_CLIENT.newCall(Request.Builder().url(twoStepUrl).build()).execute().use { response ->
            if (response.code != 200) {
                return LoginResult.Error(getApplication<Application>().getString(R.string.login_two_step_load_failed))
            }
            Parser(response.body.string()).getOnceNum()
        }
        val postBody = FormBody.Builder().add("code", code).add("once", once).build()
        val noRedirectClient = HttpHelper.OK_CLIENT.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
        val request = Request.Builder().post(postBody).url(twoStepUrl).build()
        return noRedirectClient.newCall(request).execute().use { response ->
            if (response.code == 302) {
                LoginResult.Success
            } else {
                LoginResult.Error(getApplication<Application>().getString(R.string.login_two_step_failed))
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
