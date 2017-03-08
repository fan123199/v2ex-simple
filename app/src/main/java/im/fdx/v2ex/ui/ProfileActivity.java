package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.TimeHelper;

public class ProfileActivity extends AppCompatActivity {


    public static final String TAG = ProfileActivity.class.getSimpleName();
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
        getSupportActionBar().setTitle(R.string.profile);

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
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
        } else {
            profileId = getIntent().getExtras().getLong("profile_id", -1L);
            url = JsonManager.USER_JSON + "?id=" + profileId;
        }

        Log.i(TAG, url);

        StringRequest sr = new StringRequest(url, new StringListener(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleyHelper.getInstance().addToRequestQueue(sr);


    }

    public void openTwitter(View view) {
        Intent intent = null;
        try {
            // get the Twitter app if possible
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + ((TextView) view).getText()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + ((TextView) view).getText()));
        }
        this.startActivity(intent);
    }

    public void openWeb(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view).getText().toString()));
        startActivity(intent);
    }

    public void openGithub(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/" + ((TextView) view).getText().toString()));
        startActivity(intent);
    }

    private class StringListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {


            //// TODO: 2017/3/8 没有将member 持久化，所以我只能从view中获取当前member的值。
            MemberModel member = JsonManager.myGson.fromJson(response, MemberModel.class);
            mTvUsername.setText(member.getUserName());

            mIvAvatar.setImageUrl(member.getAvatarLargeUrl(), imageLoader);
//                Uri uri = Uri.parse(member.getAvatarLargeUrl());
//                mIvAvatar.setImageURI(uri);


            mTvId.setText(String.valueOf(member.getId()));
            mTvIntro.setText(String.format("bio: %s", member.getBio()));
            mTvUserCreatedTime.setText(TimeHelper.getAbsoluteTime(Long.parseLong(member.getCreated())));

            mTvBitcoin.setText(member.getBtc());
            mTvGithub.setText(member.getGithub());
            mTvLocation.setText(toAbsLocation(member.getLocation()));
            mTvTwitter.setText(member.getTwitter());
            mTvWebsite.setText(member.getWebsite());
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

        /**
         * 转换成绝对时间
         *
         * @param created
         * @return
         */
        private String toAbsTime(String created) {
            return null;
        }
    }
}
