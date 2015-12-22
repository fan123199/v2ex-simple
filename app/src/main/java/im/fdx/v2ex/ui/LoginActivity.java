package im.fdx.v2ex.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import im.fdx.v2ex.R;
import im.fdx.v2ex.utils.JsonManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_SIGNUP = 0;

    private Button btnLogin;
    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.input_username);
        etPassword = (EditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(this);
        findViewById(R.id.link_sign_up).setOnClickListener(this);

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
        switch (v.getId()){
            case R.id.btn_login:
                if(!validate()) {
                    return;
                }

                login();
                break;
            case R.id.link_sign_up:
                Intent openUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(JsonManager.SIGN_UP_URL));
                //// TODO: 2015/12/22  
                startActivityForResult(openUrl, REQUEST_SIGNUP);
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
        JsonManager.Login(this,username,password);
    }

    private boolean validate() {
        boolean valid =true;
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if(username.isEmpty()){
            etUsername.setError("名字不能为空");
            etUsername.requestFocus();
            valid = false;
        }
        if(password.isEmpty()) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
            valid = false;
        }
        return valid;
    }




}
