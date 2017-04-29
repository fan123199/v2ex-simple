package im.fdx.v2ex.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.util.Base64.encodeToString;
import static im.fdx.v2ex.network.NetManager.API_USER;
import static im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE;
import static im.fdx.v2ex.network.NetManager.SIGN_IN_URL;
import static im.fdx.v2ex.network.NetManager.myGson;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final int LOG_IN_SUCCEED = 2;
    private static final int LOG_IN_FAILED = 3;
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private ProgressDialog progressDialog;
    private SharedPreferences mSharedPreference;
    private String username;
    private String password;
    String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            toolbar.setTitle("");
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        etUsername = (TextInputEditText) findViewById(R.id.input_username);
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Light_Dialog);
        String usernamePref = mSharedPreference.getString("username", "");
        etPassword = (TextInputEditText) findViewById(R.id.input_password);

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        TextView tvSignup = (TextView) findViewById(R.id.link_sign_up);


        btnLogin.setOnClickListener(this);
        tvSignup.setOnClickListener(this);

        if (!TextUtils.isEmpty(usernamePref)) {
            etUsername.setText(usernamePref);
            etPassword.requestFocus();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
//        login();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (!isValidated()) return;
                login();
                break;
            case R.id.link_sign_up:
                Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(NetManager.SIGN_UP_URL));
                startActivity(openUrl);
                break;
        }
    }


    private void login() {

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();

        getLoginData();


    }

    private void getLoginData() {
        Request requestToGetOnce = new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(SIGN_IN_URL)
                .build();

        HttpHelper.OK_CLIENT.newCall(requestToGetOnce).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FDX", "error in get login page");
            }

            @Override
            public void onResponse(Call call, Response response0) throws IOException {
                String htmlString = response0.body().string();
                Element body = Jsoup.parse(htmlString).body();
//                    Element box = body.getElementsByClass("box").last();
//                    Element cell = box.getElementsByClass("cell").first();
                String nameKey = body.getElementsByAttributeValue("placeholder", "用户名或电子邮箱地址").attr("name");
                String passwordKey = body.getElementsByAttributeValue("type", "password").attr("name");
                String onceCode = body.getElementsByAttributeValue("name", "once").attr("value");

                XLog.tag("LoginActivity").d(nameKey + "|" + passwordKey + "|" + onceCode);
//            XLog.d(request.headers().toString());
//            XLog.d(request.toString());
//            XLog.d(request.isHttps() + request.method() +request.body().toString() );

                postLogin(nameKey, passwordKey, onceCode);

            }
        });


//            XLog.d( String.valueOf(htmlString.length()));
//            XLog.d(htmlString);

    }

    private void postLogin(String nameKey, final String passwordKey, String onceCode) {
        RequestBody requestBody = new FormBody.Builder()
//                            .add("next", "/")
                .add(nameKey, username)
                .add(passwordKey, password)
                .add("once", onceCode)
                .build();

        final Request request = new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(SIGN_IN_URL)
                .header("Origin", HTTPS_V2EX_BASE)
                .header("Referer", SIGN_IN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        HttpHelper.OK_CLIENT.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FDX", "error in Post Login");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                final int httpcode = response.code();
                final String errorMsg = getErrorMsg(response.body().string());
                XLog.tag("LoginActivity").d("http code: " + response.code());
                XLog.tag("LoginActivity").d("errorMsg" + errorMsg);

                switch (httpcode) {
                    case 302:

                        MyApp.getInstance().setLogin(true);
                        mSharedPreference.edit()
                                .putString("username", username)
                                .putBoolean("is_login", true)
                                .apply();
//                        LoginActivity.this.setResult(LOG_IN_SUCCEED);


                        goMyHomePage();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                HintUI.T(LoginActivity.this, "登录成功");
                            }
                        });

                        finish();
                        break;
                    case 200:
//                        LoginActivity.this.setResult(LOG_IN_FAILED);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                HintUI.T(LoginActivity.this, "登录失败\n " + errorMsg);
                            }
                        });
                        break;
                    default:

                        break;
                }

            }
        });
    }

    private void goMyHomePage() {
        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(API_USER + "?username=" + username).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 200) {
                    return;
                }
                String body = response.body().string();
                MemberModel member = myGson.fromJson(body, MemberModel.class);
                avatar = member.getAvatarLargeUrl();

                mSharedPreference.edit().putString("avatar", avatar).apply();
                Intent intent = new Intent(Keys.ACTION_LOGIN);
                intent.putExtra(Keys.KEY_USERNAME, username);
                intent.putExtra(Keys.KEY_AVATAR, avatar);
                LocalBroadcastManager.getInstance(LoginActivity.this)
                        .sendBroadcast(intent);
            }
        });
    }


    private String getErrorMsg(String body) {

        XLog.tag(TAG).d(body);
        Element element = Jsoup.parse(body).body();
        Elements message = element.getElementsByClass("problem");
        if (message == null) {
            return "";
        }

        return message.text().trim();
    }

    private boolean isValidated() {
        boolean valid = true;
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (password.isEmpty()) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
            valid = false;
        }
        if (username.isEmpty()) {
            etUsername.setError("名字不能为空");
            etUsername.requestFocus();
            valid = false;
        }
        return valid;
    }


}
