package im.fdx.v2ex.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TextInputEditText
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.MemberModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.*
import im.fdx.v2ex.utils.HintUI
import im.fdx.v2ex.utils.Keys
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var etUsername: TextInputEditText? = null
    private var etPassword: TextInputEditText? = null
    private var progressDialog: ProgressDialog? = null
    private var mSharedPreference: SharedPreferences? = null
    private var username: String? = null
    private var password: String? = null
    private var avatar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        toolbar.title = ""
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        etUsername = findViewById(R.id.input_username) as TextInputEditText
        etPassword = findViewById(R.id.input_password) as TextInputEditText
        progressDialog = ProgressDialog(this, R.style.AppTheme_Light_Dialog)
        val usernamePref = mSharedPreference?.getString("username", "")

        val btnLogin = findViewById(R.id.btn_login) as Button
        val tvSignup = findViewById(R.id.link_sign_up) as TextView

        btnLogin.setOnClickListener(this)
        tvSignup.setOnClickListener(this)

        if (!TextUtils.isEmpty(usernamePref)) {
            etUsername?.setText(usernamePref)
            etPassword?.requestFocus()
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> {
                if (!isValidated) return
                login()
            }
            R.id.link_sign_up -> {
                val openUrl = Intent(Intent.ACTION_VIEW, Uri.parse(NetManager.SIGN_UP_URL))
                startActivity(openUrl)
            }
        }
    }


    private fun login() {

        progressDialog?.isIndeterminate = true
        progressDialog?.setMessage(getString(R.string.authenticating))
        progressDialog?.show()
        username = etUsername?.text.toString()
        password = etPassword?.text.toString()

        getLoginData()


    }

    private fun getLoginData() {
        val requestToGetOnce = Request.Builder().headers(HttpHelper.baseHeaders)
                .url(SIGN_IN_URL)
                .build()

        HttpHelper.OK_CLIENT.newCall(requestToGetOnce).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FDX", "error in get login page")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response0: Response) {
                val htmlString = response0.body()?.string()
                val body = Jsoup.parse(htmlString).body()
                val nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮箱地址").attr("name")
                val passwordKey = body.getElementsByAttributeValue("type", "password").attr("name")
                val onceCode = body.getElementsByAttributeValue("name", "once").attr("value")

                XLog.tag("LoginActivity").d("$nameKey|$passwordKey|$onceCode")
                //            XLog.d(request.headers().toString());
                //            XLog.d(request.toString());
                //            XLog.d(request.isHttps() + request.method() +request.body().toString() );

                postLogin(nameKey, passwordKey, onceCode)

            }
        })


        //            XLog.d( String.valueOf(htmlString.length()));
        //            XLog.d(htmlString);

    }

    private fun postLogin(nameKey: String, passwordKey: String, onceCode: String) {
        val requestBody = FormBody.Builder()
                .add(nameKey, username!!)
                .add(passwordKey, password!!)
                .add("once", onceCode)
                .build()

        val request = Request.Builder().headers(HttpHelper.baseHeaders)
                .url(SIGN_IN_URL)
                .header("Origin", HTTPS_V2EX_BASE)
                .header("Referer", SIGN_IN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build()
        HttpHelper.OK_CLIENT.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FDX", "error in Post Login")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val httpcode = response.code()
                val errorMsg = getErrorMsg(response.body()?.string())
                XLog.tag("LoginActivity").d("http code: ${response.code()}")
                XLog.tag("LoginActivity").d("errorMsg: $errorMsg")

                when (httpcode) {
                    302 -> {
                        MyApp.get().setLogin(true)
                        mSharedPreference?.edit()
                                ?.putString("username", username)
                                ?.apply()
                        goMyHomePage()
                        runOnUiThread {
                            progressDialog?.dismiss()
                            HintUI.T(this@LoginActivity, "登录成功")
                        }

                        finish()
                    }
                    200 -> runOnUiThread {
                        progressDialog?.dismiss()
                        HintUI.T(this@LoginActivity, "登录失败:\n $errorMsg")
                    }
                }
            }
        })
    }

    private fun goMyHomePage() {
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url("$API_USER?username=$username").build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dealError(this@LoginActivity)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() != 200) {
                    dealError(this@LoginActivity, response.code())
                    return
                }
                val body = response.body()?.string()
                val member = myGson.fromJson(body, MemberModel::class.java)
                avatar = member.avatarLargeUrl

                mSharedPreference!!.edit().putString("avatar", avatar).apply()
                val intent = Intent(Keys.ACTION_LOGIN).apply {
                    putExtra(Keys.KEY_USERNAME, username)
                    putExtra(Keys.KEY_AVATAR, avatar)
                }
                LocalBroadcastManager.getInstance(this@LoginActivity).sendBroadcast(intent)
            }
        })
    }


    private fun getErrorMsg(body: String?): String {

        XLog.tag(TAG).d(body)
        val element = Jsoup.parse(body).body()
        val message = element.getElementsByClass("problem") ?: return ""
        return message.text().trim { it <= ' ' }
    }

    private val isValidated: Boolean
        get() {
            var valid = true
            val username = etUsername?.text.toString()
            val password = etPassword?.text.toString()

            if (password.isEmpty()) {
                etPassword?.error = "密码不能为空"
                etPassword?.requestFocus()
                valid = false
            }
            if (username.isEmpty()) {
                etUsername?.error = "名字不能为空"
                etUsername?.requestFocus()
                valid = false
            }
            return valid
        }

    companion object {
        private val TAG = LoginActivity::class.java.canonicalName;
    }

}
