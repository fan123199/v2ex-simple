package im.fdx.v2ex.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ActivityLoginBinding
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.pref
import im.fdx.v2ex.setLogin
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.*
import im.fdx.v2ex.view.CustomChrome
import im.fdx.v2ex.view.UrlSpanNoUnderline
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import java.io.IOException

class LoginActivity : BaseActivity() {

    /**
     * 不一定是用户名，可能是邮箱
     */
    private var loginName: String? = null
    private var password: String? = null

    var onceCode: String? = null
    var passwordKey: String? = null
    var nameKey: String? = null
    var imageCodeKey: String? = null
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = setUpToolbar()
        val usernamePref = pref.getString(Keys.KEY_USERNAME, "")
        binding.btnLogin.setOnClickListener {
            if (!isValidated()) return@setOnClickListener
            loginName = binding.inputUsername.text.toString()
            password = binding.inputPassword.text.toString()
            binding.pbLogin.visibility = VISIBLE
            binding.btnLogin.visibility = GONE
            postLogin(
                nameKey ?: "", passwordKey ?: "", onceCode = onceCode
                    ?: "", imageCodeKey = imageCodeKey ?: ""
            )
        }
        binding.linkSignUp.setOnClickListener {
            CustomChrome(this@LoginActivity).load(NetManager.SIGN_UP_URL)
        }
        binding.ivCode.setOnClickListener {
            getLoginElement()
        }

        val span = SpannableString("点击登录或注册，表明您已了解并同意《V2EX社区规则》")
        val url = UrlSpanNoUnderline("https://www.v2ex.com/about") {
            CustomChrome(this@LoginActivity).load("https://www.v2ex.com/about")
        }
        span.setSpan(url, 18, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvAgreement.movementMethod = LinkMovementMethod()
        binding.tvAgreement.text = span
        binding.tvGoogleLogin.setOnClickListener {
            if (onceCode == null) {
                toast("请稍后")
            } else {

                Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
                    logi("google_sign_in_url")
                    logi(Firebase.remoteConfig.getString("google_sign_in_url"))
                    val intent = Intent(this, WebViewActivity::class.java).apply {
                        putExtras(bundleOf("url" to Firebase.remoteConfig.getString("google_sign_in_url")))
                    }
                    startActivityForResult(intent, 144)
                }

            }
        }

        if (!usernamePref.isNullOrEmpty()) {
            binding.inputUsername.setText(usernamePref)
            binding.inputPassword.requestFocus()
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
                val htmlString = response0.body?.string()
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
                        //  .addHeader("X-Requested-With", "com.android.browser")
                        //  .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3013.3 Mobile Safari/537.36");
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36"
                        )
                        .addHeader(
                            "cookie",
                            HttpHelper.myCookieJar.loadForRequest(str.toHttpUrlOrNull()!!).joinToString(separator = ";")
                        )
                        .build()
                    val url = GlideUrl(str, headers)
                    if (!this@LoginActivity.isDestroyed) {
                        GlideApp.with(binding.ivCode)
                            .load(url)
                            .transition(withCrossFade())
                            .centerCrop().into(binding.ivCode)
                    }
                }
                XLog.tag("LoginActivity").d("$nameKey|$passwordKey|$onceCode|$imageCodeKey")
            }
        })
    }

    private fun postLogin(nameKey: String, passwordKey: String, onceCode: String, imageCodeKey: String) {
        val requestBody = FormBody.Builder()
            .add(nameKey, binding.inputUsername.text.toString())
            .add(passwordKey, binding.inputPassword.text.toString())
            .add(imageCodeKey, binding.etInputCode.text.toString())
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
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response2: Response) {
                                if (response2.code == 302) {
                                    if (("/2fa" == response2.header("Location"))) {
                                        runOnUiThread {
                                            binding.pbLogin.visibility = GONE
                                            binding.btnLogin.visibility = VISIBLE
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
                        showHint(binding.root, errorMsg, Snackbar.LENGTH_INDEFINITE)
                        getLoginElement()
                        binding.pbLogin.visibility = GONE
                        binding.btnLogin.visibility = VISIBLE
                    }
                }

            }
        })
    }

    private fun isValidated(): Boolean {
        val username = binding.inputUsername.text.toString()
        val password = binding.inputPassword.text.toString()

        if (username.isEmpty()) {
            binding.inputUsername.error = "名字不能为空"
            binding.inputUsername.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.inputPassword.error = "密码不能为空"
            binding.inputPassword.requestFocus()
            return false
        }
        if (binding.etInputCode.text.isNullOrEmpty()) {
            binding.etInputCode.error = "验证码不能为空"
            binding.etInputCode.requestFocus()
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
