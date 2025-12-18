package im.fdx.v2ex.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
//import im.fdx.v2ex.R // Removed R usage for layout
import im.fdx.v2ex.R
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.pref
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.ui.login.LoginScreen
import im.fdx.v2ex.ui.theme.V2ExTheme
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.*
import im.fdx.v2ex.view.CustomChrome
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import java.io.IOException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig

class LoginActivity : BaseActivity() {

    private var loginName: String? = null
    // View state
    private var username by mutableStateOf("")
    private var password by mutableStateOf("")
    private var captcha by mutableStateOf("")
    private var captchaUrl by mutableStateOf<GlideUrl?>(null)
    private var isLoading by mutableStateOf(false)

    // Hidden form fields
    var onceCode: String? = null
    var passwordKey: String? = null
    var nameKey: String? = null
    var imageCodeKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = setUpToolbar() // BaseActivity setup usually, might need adjustment if using Compose Scaffold fully. 
        // For now, keep BaseActivity structure but replace content.
        // Actually setUpToolbar might depend on layout. BaseActivity source wasn't fully shown but assuming it sets up ActionBar.
        // If content is Compose, standard ActionBar might sit on top.

        setContent {
            V2ExTheme {
                 LoginScreen(
                     username = username,
                     onUsernameChange = { username = it },
                     password = password,
                     onPasswordChange = { password = it },
                     captcha = captcha,
                     onCaptchaChange = { captcha = it },
                     captchaUrl = captchaUrl,
                     isLoading = isLoading,
                     onLoginClick = {
                         if (isValidated()) {
                             performLogin()
                         }
                     },
                     onCaptchaClick = { getLoginElement() },
                     onSignUpClick = {
                         CustomChrome(this@LoginActivity).load(NetManager.SIGN_UP_URL)
                     }
                 )
            }
        }

        val usernamePref = pref.getString(Keys.KEY_USERNAME, "")
        if (!usernamePref.isNullOrEmpty()) {
            username = usernamePref!!
        }

        getLoginElement()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 144 && resultCode == RESULT_OK) {
            setLogin(true)
            finish()
        }
    }

    private fun getLoginElement() {
        val requestToGetOnce = Request.Builder()
            .url(SIGN_IN_URL)
            .build()

        HttpHelper.OK_CLIENT.newCall(requestToGetOnce).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                loge("error in get login page")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response0: Response) {
                val htmlString = response0.body.string()
                val body = Jsoup.parse(htmlString).body()
                nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮件地址").attr("name")
                passwordKey = body.getElementsByAttributeValue("type", "password").attr("name")
                onceCode = body.getElementsByAttributeValue("name", "once").attr("value")
                imageCodeKey = body.getElementsByAttributeValueContaining("placeholder", "请输入上图中的验证码").attr("name")
                
                runOnUiThread {
                    val str = "https://www.v2ex.com/_captcha?once=$onceCode"

                    val headers: LazyHeaders = LazyHeaders.Builder()
                        .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .addHeader("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
                        .addHeader("Host", "www.v2ex.com")
                        .addHeader("Cache-Control", "max-age=0")
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36"
                        )
                        .addHeader(
                            "cookie",
                            HttpHelper.myCookieJar.loadForRequest(str.toHttpUrlOrNull()!!).joinToString(separator = ";")
                        )
                        .build()
                    captchaUrl = GlideUrl(str, headers)
                }
                XLog.tag("LoginActivity").d("$nameKey|$passwordKey|$onceCode|$imageCodeKey")
            }
        })
    }

    private fun performLogin() {
        isLoading = true
        postLogin(
            nameKey ?: "", passwordKey ?: "", onceCode = onceCode
                ?: "", imageCodeKey = imageCodeKey ?: ""
        )
    }

    private fun postLogin(nameKey: String, passwordKey: String, onceCode: String, imageCodeKey: String) {
        val requestBody = FormBody.Builder()
            .add(nameKey, username)
            .add(passwordKey, password)
            .add(imageCodeKey, captcha)
            .add("once", onceCode)
            .build()

        val request = Request.Builder()
            .url(SIGN_IN_URL)
            .header("Origin", HTTPS_V2EX_BASE)
            .header("Referer", SIGN_IN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()
        HttpHelper.OK_CLIENT.newCall(request).start(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginActivity", "error in Post Login")
                runOnUiThread { isLoading = false }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val httpcode = response.code
                val errorMsg = Parser(response.body!!.string()).getErrorMsg()
                logd("http code: ${response.code}")
                logd("errorMsg: $errorMsg")

                when (httpcode) {
                    302 -> {
                        vCall(HTTPS_V2EX_BASE).start(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                                runOnUiThread { isLoading = false }
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response2: Response) {
                                if (response2.code == 302) {
                                    if (("/2fa" == response2.header("Location"))) {
                                        runOnUiThread {
                                            isLoading = false
                                            showTwoStepDialog(this@LoginActivity)
                                        }
                                    }
                                } else {
                                    setLogin(true)
                                    runOnUiThread {
                                        toast("登录成功")
                                        finish()
                                    }
                                }
                            }
                        })

                    }
                    200 -> runOnUiThread {
                        // showHint(binding.root, errorMsg, Snackbar.LENGTH_INDEFINITE) // Can't use binding.root. Use Toast for now.
                        toast(errorMsg)
                        getLoginElement()
                        isLoading = false
                    }
                }

            }
        })
    }

    private fun isValidated(): Boolean {
        if (username.isEmpty()) {
            toast("名字不能为空")
            return false
        }
        if (password.isEmpty()) {
            toast("密码不能为空")
            return false
        }
        if (captcha.isEmpty()) {
            toast("验证码不能为空")
            return false
        }
        return true
    }

    /**
     * 两步验证，对话框
     */
    @SuppressLint("InflateParams")
    fun showTwoStepDialog(activity: Activity) {
        val dialogEt = LayoutInflater.from(activity).inflate(R.layout.dialog_et, null)
        val etCode = dialogEt.findViewById<EditText>(R.id.et_two_step_code)
        AlertDialog.Builder(activity, R.style.AppTheme_Simple)
            .setTitle("您需要进行两步验证")
            .setPositiveButton("验证") { _, _ ->
                finishLogin(etCode.text.toString(), activity)
            }
            .setNegativeButton("暂不登录") { _, _ ->
                HttpHelper.myCookieJar.clear()
                setLogin(false)

            }
            .setView(dialogEt).show()
    }

    /**
     * 两步验证，完成登录
     */
    private fun finishLogin(code: String, activity: Activity) {
        val twoStepUrl = "https://www.v2ex.com/2fa"
        vCall(twoStepUrl).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(activity)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val bodyStr = response.body?.string()!!
                    val once = Parser(bodyStr).getOnceNum()
                    val body: RequestBody = FormBody.Builder()
                        .add("code", code)
                        .add("once", once).build()
                    HttpHelper.OK_CLIENT.newCall(
                        Request.Builder()
                            .post(body)
                            .url(twoStepUrl)
                            .build()
                    )
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                NetManager.dealError(activity)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                activity.runOnUiThread {
                                    if (response?.code == 302) {
                                        activity.toast("登录成功")
                                        setLogin(true)
                                        finish()
                                    } else {
                                        activity.toast("登录失败")
                                    }
                                }
                            }
                        })
                }
            }
        })
    }

}
