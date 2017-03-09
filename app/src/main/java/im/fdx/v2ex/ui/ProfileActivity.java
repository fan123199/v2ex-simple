package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.TimeHelper;
import okhttp3.Call;
import okhttp3.Callback;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class ProfileActivity extends AppCompatActivity {


    public static final String TAG = ProfileActivity.class.getSimpleName();
    private static final int USE_VOLLEY = 1;
    public static final int USE_OKHTTP = 2;
    private final ImageLoader imageLoader = VolleyHelper.getInstance().getImageLoader();
    private TextView mTvUsername;
    private NetworkImageView mIvAvatar;
    private TextView mTvId;
    private TextView mTvUserCreatedTime;
    private TextView mTvIntro;
    private TextView mTvLocation;
    private TextView mTvBitcoin;
    private TextView mTvGithub;
    private TextView mTvTwitter;
    private TextView mTvWebsite;
    public int mHttpMode = 2;
    public static final int MSG_GET = 0;
    private boolean debug_view = false;


    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_GET) {
                makeMember((String) msg.obj);
                return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mTvUsername = (TextView) findViewById(R.id.tv_username_profile);
        mIvAvatar = (NetworkImageView) findViewById(R.id.iv_avatar_profile);
        mTvId = ((TextView) findViewById(R.id.tv_id));
        mTvUserCreatedTime = (TextView) findViewById(R.id.tv_created);
        mTvIntro = (TextView) findViewById(R.id.tv_intro);


        mTvLocation = ((TextView) findViewById(R.id.tv_location));
        mTvBitcoin = (TextView) findViewById(R.id.tv_bitcoin);
        mTvGithub = (TextView) findViewById(R.id.tv_github);
        mTvTwitter = (TextView) findViewById(R.id.tv_twitter);
        mTvWebsite = (TextView) findViewById(R.id.tv_website);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.profile);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        // ATTENTION: This was auto-generated to handle app links.
        Intent intent = getIntent();
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        long profileId = -1L;
        String profileName = "";
        String url = "";
        if (appLinkData != null) {
            String scheme = appLinkData.getScheme();
            String host = appLinkData.getHost();
            List<String> params = appLinkData.getPathSegments();
            if (host.contains("v2ex.com") && params.get(0).contains("member")) {
                profileName = params.get(1);
                url = JsonManager.USER_JSON + "?username=" + profileName;
            }
        } else if (intent.getExtras() != null) {
            profileId = getIntent().getExtras().getLong("profile_id", -1L);
            url = JsonManager.USER_JSON + "?id=" + profileId;
        } else {
            profileId = 1;
            url = JsonManager.USER_JSON + "?id=" + profileId;
        }

        Log.i(TAG, url);
        if (mHttpMode == USE_VOLLEY) {
            StringRequest sr = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    makeMember(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JsonManager.handleVolleyError(ProfileActivity.this, error);
                }
            });
            VolleyHelper.getInstance().addToRequestQueue(sr);
        } else if (mHttpMode == USE_OKHTTP) {
            HttpHelper.okHttpClient.newCall(HttpHelper.baseRequestBuilder.url(url).build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    HintUI.t(ProfileActivity.this, "网络异常");
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    Message.obtain(handler, MSG_GET, response.body().string()).sendToTarget();
                }
            });
        }
    }

    public void openTwitter(View view) {
        if ((TextUtils.isEmpty(((TextView) view).getText().toString()))) {
            return;
        }
        Intent intent = null;
        try {
            // get the Twitter app if possible
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=" + ((TextView) view).getText()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/" + ((TextView) view).getText()));
        }
        this.startActivity(intent);
    }

    public void openWeb(View view) {
        if ((TextUtils.isEmpty(((TextView) view).getText().toString()))) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view).getText().toString()));
        startActivity(intent);
    }

    public void openGithub(View view) {
        if ((TextUtils.isEmpty(((TextView) view).getText().toString()))) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.github.com/" + ((TextView) view).getText().toString()));
        startActivity(intent);
    }

    private void makeMember(String response) {
        //// TODO: 2017/3/8 没有将member 持久化，所以我只能从view中获取当前member的值。
        MemberModel member = JsonManager.myGson.fromJson(response, MemberModel.class);
        mTvUsername.setText(member.getUserName());

        mIvAvatar.setImageUrl(member.getAvatarLargeUrl(), imageLoader);
//                Uri uri = Uri.parse(member.getAvatarLargeUrl());
//                mIvAvatar.setImageURI(uri);

        mTvId.setText(getString(R.string.the_n_member, member.getId()));
        mTvIntro.setText(member.getBio());
        mTvUserCreatedTime.setText(TimeHelper.getAbsoluteTime(Long.parseLong(member.getCreated())));

        if (debug_view && TextUtils.isEmpty(member.getBtc())) {
            mTvBitcoin.setVisibility(View.GONE);
        } else {
            mTvBitcoin.setText(member.getBtc());
        }
        if (debug_view && TextUtils.isEmpty(member.getGithub())) {
            mTvGithub.setVisibility(View.GONE);
        } else {
            mTvGithub.setText(member.getGithub());
        }

        if (debug_view && TextUtils.isEmpty(member.getLocation())) {
            mTvLocation.setVisibility(View.GONE);
        } else {
            mTvLocation.setText(toAbsLocation(member.getLocation()));
        }

        if (debug_view && TextUtils.isEmpty(member.getTwitter())) {
            mTvTwitter.setVisibility(View.GONE);
        } else {
            mTvTwitter.setText(member.getTwitter());
        }

        if (debug_view && TextUtils.isEmpty(member.getWebsite())) {
            mTvWebsite.setVisibility(View.GONE);
        } else {
            mTvWebsite.setText(member.getWebsite());

        }
    }

    /**
     * 绝对位置
     *
     * @param location
     * @return
     */
    private String toAbsLocation(String location) {
        //// TODO: 2017/3/8
        return null;
    }
}
