package im.fdx.v2ex.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.utils.HintUI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static im.fdx.v2ex.network.JsonManager.HTTPS_V2EX_BASE;
import static im.fdx.v2ex.network.JsonManager.SIGN_IN_URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    {
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sf = getSharedPreferences("DEFAULT", MODE_PRIVATE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setTitle("");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                onBackPressed();
            }
        });

        etUsername = (TextInputEditText) findViewById(R.id.input_username);

        etUsername.setText(sf.getString("username", ""));
        etPassword = (TextInputEditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        TextView tv_signup = (TextView) findViewById(R.id.link_sign_up);
        tv_signup.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
//        login();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HttpHelper.REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (!validate()) {
                    return;
                }

                login();

                break;
            case R.id.link_sign_up:
                Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(JsonManager.SIGN_UP_URL));
                //// TODO: 2015/12/22  
                startActivity(openUrl);
                break;
        }
    }


    private void login() {

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();


        getLoginData(username, password);
        progressDialog.dismiss();


    }

    private void getLoginData(final String username, final String password) {
        Request requestToGetOnce = HttpHelper.baseRequestBuilder
                .url(SIGN_IN_URL)
                .build();

        HttpHelper.OK_CLIENT.newCall(requestToGetOnce).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

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

                XLog.i(nameKey + "|" + passwordKey + "|" + onceCode);


                RequestBody requestBody = new FormBody.Builder()
//                            .add("next", "/")
                        .add(nameKey, username)
                        .add(passwordKey, password)
                        .add("once", onceCode)
                        .build();


                Request request = HttpHelper.baseRequestBuilder
                        .url(SIGN_IN_URL)
                        .addHeader("Origin", HTTPS_V2EX_BASE)
                        .addHeader("Referer", SIGN_IN_URL)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .post(requestBody)
                        .build();

//            XLog.d(request.headers().toString());
//            XLog.d(request.toString());
//            XLog.d(request.isHttps() + request.method() +request.body().toString() );


                HttpHelper.OK_CLIENT.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        final int httpcode = response.code();
                        XLog.d("http code: " + response.code());
                        XLog.d("Headers:  " + response.headers().toString());
                        XLog.d("Requests: " + response.request());
                        XLog.d("msg: " + response.message());
//
                        XLog.d("response String: " + response.toString());

                        final String errorMsg = getErrorCode(response.body().string());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HintUI.T(LoginActivity.this, String.valueOf(httpcode) + errorMsg);

                            }
                        });
                    }
                });


//            }

//            responseToGetOnce.close();
//            response.close();

            }
        });


//            XLog.d( String.valueOf(htmlString.length()));
//            XLog.d(htmlString);

    }

    private String getErrorCode(String body) {
        Element element = Jsoup.parse(body).body();
        Elements message = element.getElementsByClass("problem");

        return message.text();
    }

    private boolean validate() {
        boolean valid = true;
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty()) {
            etUsername.setError("名字不能为空");
            etUsername.requestFocus();
//            valid = false;
        }
        if (password.isEmpty()) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
//            valid = false;
        }
        return valid;
    }


}
