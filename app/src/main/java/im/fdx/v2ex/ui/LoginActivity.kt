package im.fdx.v2ex.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TextInputEditText
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.MemberModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.network.NetManager.API_USER
import im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE
import im.fdx.v2ex.network.NetManager.SIGN_IN_URL
import im.fdx.v2ex.network.NetManager.dealError
import im.fdx.v2ex.network.NetManager.myGson
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.T
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var button: Button

    private var mSharedPreference: SharedPreferences? = null
    private lateinit var pbLogin: ProgressBar
    private var username: String? = null
    private var password: String? = null
    private var avatar: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        etUsername = findViewById(R.id.input_username)
        etPassword = findViewById(R.id.input_password)

        pbLogin = findViewById(R.id.pb_login)



        val usernamePref = mSharedPreference?.getString("username", "")

        button = findViewById<Button>(R.id.btn_login)
        val tvSignup = findViewById<TextView>(R.id.link_sign_up)

        button.setOnClickListener(this)
        tvSignup.setOnClickListener(this)

        if (!usernamePref.isNullOrEmpty()) {
            etUsername.setText(usernamePref)
            etPassword.requestFocus()
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> {
                if (!isValidated) return
                getLoginData()
            }
            R.id.link_sign_up -> {
                val openUrl = Intent(Intent.ACTION_VIEW, Uri.parse(NetManager.SIGN_UP_URL))
                startActivity(openUrl)
            }
        }
    }

    private fun getLoginData() {
        username = etUsername.text.toString()
        password = etPassword.text.toString()
        val requestToGetOnce = Request.Builder().headers(HttpHelper.baseHeaders)
                .url(SIGN_IN_URL)
                .build()

        pbLogin.visibility = View.VISIBLE
        button.visibility = View.GONE
        // TODO: 2017/6/20 animation
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
                            pbLogin.visibility = View.GONE
                            button.visibility = VISIBLE
                            T("登录成功")
                        }

                        finish()
                    }
                    200 -> runOnUiThread {
                        pbLogin.visibility = View.GONE
                        button.visibility = VISIBLE
                        T("登录失败:\n $errorMsg")
                    }
                    else -> runOnUiThread {
                        pbLogin.visibility = View.GONE
                        button.visibility = VISIBLE
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
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (password.isEmpty()) {
                etPassword.error = "密码不能为空"
                etPassword.requestFocus()
                valid = false
            }
            if (username.isEmpty()) {
                etUsername.error = "名字不能为空"
                etUsername.requestFocus()
                valid = false
            }
            return valid
        }

    companion object {
        private val TAG = LoginActivity::class.java.canonicalName;
    }

}
