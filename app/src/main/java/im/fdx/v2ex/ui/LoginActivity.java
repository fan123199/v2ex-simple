package im.fdx.v2ex.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.OkHttpHelper;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.HintUI;
import okhttp3.OkHttpClient;

import static android.os.Build.VERSION_CODES.M;
import static im.fdx.v2ex.network.JsonManager.SIGN_IN_URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_SIGNUP = 0;

    private Button btnLogin;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            Log.d("hehe", String.valueOf(msg.what));

            if (msg.what == 0) {
                String onceCode = (String) msg.obj;
                HintUI.T(LoginActivity.this, onceCode);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
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
        etPassword = (TextInputEditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        assert btnLogin != null;
        btnLogin.setOnClickListener(this);
        TextView tv_signup = (TextView) findViewById(R.id.link_sign_up);
        assert tv_signup != null;
        tv_signup.setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
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


    public final OkHttpClient okHttpClient = new OkHttpClient();

    private void login() {

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                doit();
                progressDialog.dismiss();

            }
        }).start();


    }

    private void doit() {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(SIGN_IN_URL)
                .build();

        okhttp3.Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String onceCode = null;
        try {
            onceCode = JsonManager.getOnceCode(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("FDX", onceCode);
        Message.obtain(handler, 0, onceCode).sendToTarget();
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
