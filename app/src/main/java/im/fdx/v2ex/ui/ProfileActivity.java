package im.fdx.v2ex.ui;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;

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
        mTvTwitter = (TextView) findViewById(R.id.tv_twiiter);
        mTvWebsite = (TextView) findViewById(R.id.tv_website);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.profile);
//        toolbar.setTitle(R.string.profile);


        long profileId = getIntent().getExtras().getLong("profile_id", -1L);

        String url = JsonManager.USER_JSON+"?id="+profileId;
        Log.i(TAG, url);

        StringRequest sr = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                MemberModel member =  JsonManager.myGson.fromJson(response, MemberModel.class);
                mTvUsername.setText(member.getUserName());

                mIvAvatar.setImageUrl(member.getAvatarLargeUrl(), imageLoader);
//                Uri uri = Uri.parse(member.getAvatarLargeUrl());
//                mIvAvatar.setImageURI(uri);


                mTvId.setText(String.valueOf(member.getId()));
                mTvIntro.setText("bio: "+member.getBio());
                mTvUserCreatedTime.setText(TOABUS(member.getCreated()));

                mTvBitcoin.setText(member.getBtc());
                mTvGithub.setText(member.getGithub());
                mTvLocation.setText(toAbsLocation(member.getLocation()));
                mTvTwitter.setText(member.getTwitter());
                mTvWebsite.setText(member.getWebsite());
            }


            /**
             * 绝对位置
             * @param location
             * @return
             */
            private String toAbsLocation(String location) {
                return null;
            }

            /**
             * 转换成绝对时间
             * @param created
             * @return
             */
            private String TOABUS(String created) {
                    return null;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleyHelper.getInstance().addToRequestQueue(sr);


    }
}
