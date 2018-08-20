package im.fdx.v2ex.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.core.content.edit
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.elvishew.xlog.XLog
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.network.*
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.pref
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.setStatusBarColor
import im.fdx.v2ex.utils.extensions.setUpToolbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.item_verify_code.*
import okhttp3.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import java.io.IOException

class LoginActivity : BaseActivity() {

  private lateinit var progressBar: ProgressBar

  /**
   * 不一定是用户名，可能是邮箱
   */
  private var loginName: String? = null
  private var password: String? = null

  var onceCode: String? = null
  var passwordKey: String? = null
  var nameKey: String? = null
  var imageCodeKey: String? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    setUpToolbar()
    setStatusBarColor(R.color.primary)
    progressBar = findViewById(R.id.pb_login)

    val usernamePref = pref.getString(Keys.KEY_USERNAME, "")
    btn_login.setOnClickListener {
      if (!isValidated()) return@setOnClickListener
      loginName = input_username.text.toString()
      password = input_password.text.toString()
      progressBar.visibility = View.VISIBLE
      btn_login.visibility = View.GONE
      postLogin(nameKey ?: "", passwordKey ?: "", onceCode = onceCode
          ?: "", imageCodeKey = imageCodeKey ?: "")
    }
    link_sign_up.setOnClickListener {
      val openUrl = Intent(Intent.ACTION_VIEW, Uri.parse(NetManager.SIGN_UP_URL))
      startActivity(openUrl)
    }
    iv_code.setOnClickListener {
      getLoginElement()
    }

    if (!usernamePref.isNullOrEmpty()) {
      input_username.setText(usernamePref)
      input_password.requestFocus()
    }

    getLoginElement()
  }

  private fun getLoginElement() {

    val requestToGetOnce = Request.Builder()
        .url(SIGN_IN_URL)
        .build()

    HttpHelper.OK_CLIENT.newCall(requestToGetOnce).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        XLog.tag("LoginActivity").e("error in get login page")
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response0: Response) {
        val htmlString = response0.body()?.string()
        val body = Jsoup.parse(htmlString).body()
        nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮箱地址").attr("name")
        passwordKey = body.getElementsByAttributeValue("type", "password").attr("name")
        onceCode = body.getElementsByAttributeValue("name", "once").attr("value")
        imageCodeKey = body.getElementsByAttributeValue("placeholder", "请输入上图中的验证码").attr("name")
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
              .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " (KHTML, like Gecko) Chrome/58.0.3013.3 Safari/537.36")
              .addHeader("cookie", HttpHelper.myCookieJar.loadForRequest(HttpUrl.parse(str)!!).joinToString(separator = ";"))
              .build()
          val url = GlideUrl(str, headers)
          if (!this@LoginActivity.isDestroyed) {
            GlideApp.with(iv_code).load(url).centerCrop().into(iv_code)
          }
        }
        XLog.tag("LoginActivity").d("$nameKey|$passwordKey|$onceCode|$imageCodeKey")
      }
    })
  }

  private fun postLogin(nameKey: String, passwordKey: String, onceCode: String, imageCodeKey: String) {
    val requestBody = FormBody.Builder()
        .add(nameKey, input_username.text.toString())
        .add(passwordKey, input_password.text.toString())
        .add(imageCodeKey, et_input_code.text.toString())
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
        val httpcode = response.code()
        val errorMsg = Parser(response.body()!!.string()).getErrorMsg()
        logd("http code: ${response.code()}")
        logd("errorMsg: $errorMsg")

        when (httpcode) {
          302 -> {
            MyApp.get().setLogin(true)

            vCall(HTTPS_V2EX_BASE).start(object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
              }

              @Throws(IOException::class)
              override fun onResponse(call: Call, response2: okhttp3.Response) {
                if (response2.code() == 302) {
                  if (("/2fa" == response2.header("Location"))) {
                    runOnUiThread {
                      progressBar.visibility = View.GONE
                      btn_login.visibility = VISIBLE
                      NetManager.showTwoStepDialog(this@LoginActivity)
                    }
                  }
                } else {
                  val body = response2.body()?.string()!!
                  val myInfo = Parser(body).getMember()

                  pref.edit {
                    putString(Keys.KEY_USERNAME, myInfo.username)
                    putString(Keys.KEY_AVATAR, myInfo.avatarNormalUrl)
                  }

                  val intent = Intent(Keys.ACTION_LOGIN).apply {
                    putExtra(Keys.KEY_USERNAME, myInfo.username)
                    putExtra(Keys.KEY_AVATAR, myInfo.avatarNormalUrl)
                  }
                  LocalBroadcastManager.getInstance(this@LoginActivity).sendBroadcast(intent)
                  runOnUiThread {
                    toast("登录成功")
                    finish()
                  }
                }
              }
            })

          }
          200 -> runOnUiThread {
            longToast("登录失败:\n $errorMsg")
            progressBar.visibility = View.GONE
            btn_login.visibility = VISIBLE
          }
        }

      }
    })
  }

  private fun isValidated(): Boolean {
    val username = input_username.text.toString()
    val password = input_password.text.toString()

    if (username.isEmpty()) {
      input_username.error = "名字不能为空"
      input_username.requestFocus()
      return false
    }
    if (password.isEmpty()) {
      input_password.error = "密码不能为空"
      input_password.requestFocus()
      return false
    }
    if (et_input_code.text.isNullOrEmpty()) {
      et_input_code.error = "验证码不能为空"
      et_input_code.requestFocus()
      return false
    }

    return true
  }

}
