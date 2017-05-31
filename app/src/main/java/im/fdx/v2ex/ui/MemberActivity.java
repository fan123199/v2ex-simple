package im.fdx.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.BuildConfig;
import im.fdx.v2ex.MyApp;
import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.ui.main.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.main.TopicsRVAdapter;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.TimeUtil;
import im.fdx.v2ex.utils.ViewUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static im.fdx.v2ex.network.NetManager.*;


/**
 * 获取user的主题，依然使用api的方式
 */
public class MemberActivity extends AppCompatActivity {


    public static final String TAG = MemberActivity.class.getSimpleName();
    private static final int MSG_GET_USER_INFO = 0;
    private static final int MSG_GET_TOPIC = 1;
    private TextView mTvUsername;
    private ImageView mIvAvatar;
    private TextView mTvId;
    private TextView mTvUserCreated;
    private TextView mTvIntro;
    private TextView mTvLocation;
    private TextView mTvBitcoin;
    private TextView mTvGithub;
    private TextView mTvTwitter;
    private TextView mTvWebsite;
    private List<TopicModel> mTopics = new ArrayList<>();

    private String username;
    private TopicsRVAdapter mAdapter;
    private String urlTopic;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private MemberModel member;
    private String blockOfT;
    private String followOfOnce;
    private Boolean isBlocked;
    private Boolean isFollowed;
    private ConstraintLayout constraintLayout;
    private FrameLayout container;
    private Menu mMenu;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_GET_USER_INFO) {
                showUser((String) msg.obj);
            } else if (msg.what == MSG_GET_TOPIC) {
                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mTvUsername = (TextView) findViewById(R.id.tv_username_profile);
        mIvAvatar = (ImageView) findViewById(R.id.iv_avatar_profile);
        mTvId = ((TextView) findViewById(R.id.tv_id));
        mTvUserCreated = (TextView) findViewById(R.id.tv_created);
        mTvIntro = (TextView) findViewById(R.id.tv_intro);
        mTvLocation = ((TextView) findViewById(R.id.tv_location));
        mTvBitcoin = (TextView) findViewById(R.id.tv_bitcoin);
        mTvGithub = (TextView) findViewById(R.id.tv_github);
        mTvTwitter = (TextView) findViewById(R.id.tv_twitter);
        mTvWebsite = (TextView) findViewById(R.id.tv_website);

        {
            mTvLocation.setVisibility(View.GONE);
            mTvBitcoin.setVisibility(View.GONE);
            mTvGithub.setVisibility(View.GONE);
            mTvTwitter.setVisibility(View.GONE);
            mTvWebsite.setVisibility(View.GONE);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this::getTopicsByUsernameAPI);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.al_profile);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.ctl_profile);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {

            int maxScroll = appBarLayout1.getTotalScrollRange();
            float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
            handleAlphaOnTitle(percentage);
        });

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_member);

        container = (FrameLayout) findViewById(R.id.fl_container);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_container);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MemberActivity.this);

        rv.setLayoutManager(layoutManager);
        mAdapter = new TopicsRVAdapter(this, mTopics);
        rv.setAdapter(mAdapter);
        parseIntent(getIntent());
    }

    // 设置渐变的动画
    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage > 0.8f && percentage <= 1f) {
            constraintLayout.setVisibility(View.INVISIBLE);
        } else if (percentage <= 0.8f && percentage >= 0f) {
            constraintLayout.setVisibility(View.VISIBLE);
        }

    }

    private void parseIntent(Intent intent) {

        Uri appLinkData = intent.getData();
        String urlUserInfo = "";
        if (appLinkData != null) {
            String scheme = appLinkData.getScheme();
            String host = appLinkData.getHost();
            List<String> params = appLinkData.getPathSegments();
            if (host.contains("v2ex.com") && params.get(0).contains("member")) {
                username = params.get(1);
                urlUserInfo = API_USER + "?username=" + username;
            }
        } else if (intent.getExtras() != null) {
            username = getIntent().getExtras().getString(Keys.KEY_USERNAME);
            urlUserInfo = API_USER + "?username=" + username;
        } else if (BuildConfig.DEBUG) {
            username = "Livid";
            urlUserInfo = API_USER + "?username=" + username;  //Livid's profile
        }
        collapsingToolbarLayout.setTitle(username);
        urlTopic = API_TOPIC + "?username=" + username;
        Log.i(TAG, urlUserInfo + "\n" + urlTopic);
        //// TODO: 2017/3/20 可以改成一步，分析下性能
        getUserInfoAPI(urlUserInfo);
        getTopicsByUsernameAPI();
        getBlockAndFollowWeb();
    }

    private void getBlockAndFollowWeb() {
        if (username.equals(PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.KEY_USERNAME, ""))) {
            return;
        }
        String webUrl = "https://www.v2ex.com/member/" + username;
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(webUrl)
                .get().build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError(MemberActivity.this);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String html = response.body().string();
                    Element body = Jsoup.parse(html).body();
                    isBlocked = parseIsBlock(html);
                    isFollowed = parseIsFollowed(html);
                    XLog.d("isBlocked" + isBlocked + "|" + "isFollowed" + isFollowed);


                    runOnUiThread(() -> {
                        if (isBlocked) {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp);
                        } else {
                            mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp);
                        }

                        if (isFollowed) {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_white_24dp);
                        } else {
                            mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp);
                        }

                    });


                    blockOfT = parseToBlock(html);

                    if (blockOfT == null) {
                        MyApp.getInstance().setLogin(false);
                    }
                    followOfOnce = parseToOnce(html);
                }
            }
        });
    }

    private Boolean parseIsFollowed(String html) {
        Pattern pFollow = Pattern.compile("un(?=follow/\\d{1,8}\\?once=)");
        Matcher matcher = pFollow.matcher(html);
        return matcher.find();

    }

    private Boolean parseIsBlock(String html) {
        Pattern pFollow = Pattern.compile("un(?=block/\\d{1,8}\\?t=)");
        Matcher matcher = pFollow.matcher(html);
        return matcher.find();
    }

    private String parseToOnce(String html) {

//        <input type="button" value="加入特别关注"
// onclick="if (confirm('确认要开始关注 SoulGem？'))
// { location.href = '/follow/209351?once=61676'; }" class="super special button">

        Pattern pFollow = Pattern.compile("follow/\\d{1,8}\\?once=\\d{1,10}");
        Matcher matcher = pFollow.matcher(html);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * @param html
     * @return the whole path url
     */
    private String parseToBlock(String html) {
//        <input type="button" value="Block" onclick="if (confirm('确认要屏蔽 SoulGem？'))
// { location.href = '/block/209351?t=1490028444'; }" class="super normal button">
        Pattern pFollow = Pattern.compile("block/\\d{1,8}\\?t=\\d{1,20}");
        Matcher matcher = pFollow.matcher(html);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private void getUserInfoAPI(String urlUserInfo) {
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(urlUserInfo).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dealError(MemberActivity.this);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String body = response.body().string();
                Message.obtain(handler, MSG_GET_USER_INFO, body).sendToTarget();
            }
        });
    }

    private void getTopicsByUsernameAPI() {

        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(urlTopic).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dealError(MemberActivity.this);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                String body = response.body().string();
                Type type = new TypeToken<ArrayList<TopicModel>>() {
                }.getType();
                List<TopicModel> topicModels = myGson.fromJson(body, type);
                if (topicModels == null || topicModels.size() == 0) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        ViewUtil.showNoContent(MemberActivity.this, container);
                    });
                    return;
                }
                mAdapter.updateData(topicModels);
                XLog.tag("profile").i(topicModels.get(0).getTitle());
                Message.obtain(handler, MSG_GET_TOPIC).sendToTarget();
            }
        });
    }

    public void openTwitter(View view) {
        if (TextUtils.isEmpty(member.getTwitter())) {
            return;
        }
        Intent intent;
        try {
            // get the Twitter app if possible
            this.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=" + member.getTwitter()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/" + member.getTwitter()));
        }
        this.startActivity(intent);
    }

    public void openWeb(View view) {
        if (TextUtils.isEmpty(member.getWebsite())) {
            return;
        }
        String text;
        if (!member.getWebsite().contains("http")) {
            text = "http://" + member.getWebsite();
        } else {
            text = member.getWebsite();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
        startActivity(intent);
    }

    public void openGithub(View view) {
        if (TextUtils.isEmpty(member.getGithub())) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.github.com/" + member.getGithub()));
        startActivity(intent);
    }

    private void showUser(String response) {
        member = myGson.fromJson(response, MemberModel.class);
        mTvUsername.setText(member.getUsername());

//        toolbar.setTitle(member.getUsername());

        Picasso.with(this).load(member.getAvatarLargeUrl()).error(R.drawable.ic_person_outline_black_24dp).into(mIvAvatar);
        mTvId.setText(getString(R.string.the_n_member, member.getId()));
        mTvIntro.setText(member.getBio());
        mTvUserCreated.setText(TimeUtil.getAbsoluteTime(Long.parseLong(member.getCreated())));

        boolean debug_view = false;
        if (debug_view || TextUtils.isEmpty(member.getBtc())) {
            mTvBitcoin.setVisibility(View.GONE);
        } else {
            mTvBitcoin.setVisibility(View.VISIBLE);
            mTvBitcoin.setText(member.getBtc());
        }
        if (debug_view || TextUtils.isEmpty(member.getGithub())) {
            mTvGithub.setVisibility(View.GONE);
        } else {
            mTvGithub.setVisibility(View.VISIBLE);
            mTvGithub.setText(member.getGithub());
        }

        if (debug_view || TextUtils.isEmpty(member.getLocation())) {
            mTvLocation.setVisibility(View.GONE);
        } else {
            mTvLocation.setVisibility(View.VISIBLE);
            mTvLocation.setText(member.getLocation());
        }

        if (debug_view || TextUtils.isEmpty(member.getTwitter())) {
            mTvTwitter.setVisibility(View.GONE);
        } else {
            mTvTwitter.setVisibility(View.VISIBLE);
            mTvTwitter.setText(member.getTwitter());
        }

        if (debug_view || TextUtils.isEmpty(member.getWebsite())) {
            mTvWebsite.setVisibility(View.GONE);
        } else {
            mTvWebsite.setVisibility(View.VISIBLE);
            mTvWebsite.setText(member.getWebsite());

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_member, menu);
        this.mMenu = menu;
        if (username.equals(PreferenceManager.getDefaultSharedPreferences(this).getString(Keys.KEY_USERNAME, ""))) {
            menu.findItem(R.id.menu_block).setVisible(false);
            menu.findItem(R.id.menu_follow).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        XLog.d("onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.menu_follow:
                switchFollowAndRefresh(isFollowed);
                break;
            case R.id.menu_block:
                switchBlockAndRefresh(isBlocked);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;

    }

    private void switchFollowAndRefresh(boolean isFollowed) {
        if (isFollowed) {
            HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                    .url(NetManager.HTTPS_V2EX_BASE + "/un" + followOfOnce)
                    .build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dealError(MemberActivity.this);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 302) {
                        getBlockAndFollowWeb();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_border_white_24dp);
                                HintUI.t(MemberActivity.this, "取消关注成功");
                            }
                        });

                    }
                }
            });
        } else {
            HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().headers(HttpHelper.INSTANCE.getBaseHeaders())
                    .url(NetManager.HTTPS_V2EX_BASE + "/" + followOfOnce)
                    .build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dealError(MemberActivity.this);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 302) {
                        getBlockAndFollowWeb();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mMenu.findItem(R.id.menu_follow).setIcon(R.drawable.ic_favorite_white_24dp);
                                HintUI.t(MemberActivity.this, "关注成功");
                            }
                        });
                    }
                }
            });

        }
    }

    private void switchBlockAndRefresh(Boolean isBlocked) {
        if (isBlocked) {
            HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                    .headers(HttpHelper.INSTANCE.getBaseHeaders())
                    .url(HTTPS_V2EX_BASE + "/un" + blockOfT).build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 302) {
                        getBlockAndFollowWeb();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_white_24dp);
                                HintUI.t(MemberActivity.this, "取消屏蔽成功");
                            }
                        });
                    }

                }
            });
        } else {
            HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                    .headers(HttpHelper.INSTANCE.getBaseHeaders())
                    .url(HTTPS_V2EX_BASE + "/" + blockOfT).build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 302) {
                        getBlockAndFollowWeb();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mMenu.findItem(R.id.menu_block).setIcon(R.drawable.ic_block_primary_24dp);
                                HintUI.t(MemberActivity.this, "屏蔽成功");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        parseIntent(intent);


    }

}
