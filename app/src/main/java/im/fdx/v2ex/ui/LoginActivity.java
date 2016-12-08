package im.fdx.v2ex.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.HintUI;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_SIGNUP = 0;

    private Button btnLogin;
    private EditText etUsername;
    private EditText etPassword;

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

        etUsername = (EditText) findViewById(R.id.input_username);
        etPassword = (EditText) findViewById(R.id.input_password);
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


        StringRequest stringRequest = new StringRequest(Request.Method.GET, JsonManager.SIGN_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO: 15-9-17
//value="54055" name="once"


//                int onceCode = getOnceCode(response);



                HintUI.t(LoginActivity.this,response);
                Log.i(JsonManager.TAG,response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JsonManager.handleVolleyError(LoginActivity.this,error);
            }
        }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String,String> params = new HashMap<>();
//                params.put("u",username);
//                params.put("p", password);
//                params.put("once", "1154");  // TODO: 15-9-17
//                params.put("next","/");
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//               Map<String,String> params = new HashMap<>();
//                params.put("Content-Type", "application/x-www-form-urlencoded");
//
//
//                return params;
//            }
        };
        VolleyHelper.getInstance().addToRequestQueue(stringRequest);
        progressDialog.dismiss();
    }

    private boolean validate() {
        boolean valid =true;
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if(username.isEmpty()){
            etUsername.setError("名字不能为空");
            etUsername.requestFocus();
//            valid = false;
        }
        if(password.isEmpty()) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
//            valid = false;
        }
        return valid;
    }




}
