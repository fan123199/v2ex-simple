package im.fdx.v2ex.ui.main;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.SmoothLayoutManager;
import im.fdx.v2ex.utils.ViewUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE;

// 2015/10/12 How and When to receive the params in fragment's lifecycle
// simplify it, receive in onCreateView
public class TopicsFragment extends Fragment {

    private static final String TAG = TopicsFragment.class.getSimpleName();
    private static final int MSG_FAILED = 3;
    private static final int MSG_GET_DATA_BY_OK = 1;

    private List<TopicModel> mTopicModels = new ArrayList<>();
    private TopicsRVAdapter mAdapter;
    private SwipeRefreshLayout mSwipeLayout;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_DATA_BY_OK:
                    mAdapter.notifyDataSetChanged();
                    mSwipeLayout.setRefreshing(false);
                    break;
                case MSG_FAILED:
                    mSwipeLayout.setRefreshing(false);
                    break;
            }
            return false;
        }
    });
    private String mRequestURL;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private FrameLayout flContainer;

    public TopicsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_tab_article, container, false);
        Bundle args = getArguments();
        String mTabs = args.getString(Keys.KEY_TAB);
        if (args.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 1) {
            mRequestURL = HTTPS_V2EX_BASE + "/my/topics";
        } else if (args.getInt(Keys.FAVOR_FRAGMENT_TYPE, -1) == 2) {
            mRequestURL = HTTPS_V2EX_BASE + "/my/following";
        } else {
            if (mTabs != null && !mTabs.isEmpty())
                if (mTabs.equals("recent")) {
                    mRequestURL = HTTPS_V2EX_BASE + "/recent";
                } else {
                    mRequestURL = HTTPS_V2EX_BASE + "/?tab=" + mTabs;
                }

        }
        getTopicsByOK(mRequestURL);

        mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getTopicsJsonByVolley(mRequestURL);
                getTopicsByOK(mRequestURL);
            }
        });
        mSwipeLayout.setRefreshing(true);

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_container);
        //这里用线性显示 类似于listView
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setLayoutManager(new SmoothLayoutManager(getActivity()));


        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_main);
        if (fab != null) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                boolean isFabShowing = true;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    ValueAnimator animator = ValueAnimator.ofInt(100, 200, 300, 400);
                    animator.setDuration(1000);
                    Animation animation = new TranslateAnimation(Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE, 100);
                    animation.setRepeatCount(1);
                    if (dy > 0) hideFab();
                    if (dy < 0) showFab();
                }

                private void hideFab() {
                    if (isFabShowing) {
                        isFabShowing = false;
                        final Point point = new Point();
                        getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(point);
                        final float translation = fab.getY() - point.y;
                        fab.animate().translationYBy(-translation).setDuration(500).start();
                    }
                }

                private void showFab() {
                    if (!isFabShowing) {
                        isFabShowing = true;
                        fab.animate().translationY(0).setDuration(500).start();
                    }
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }

        mAdapter = new TopicsRVAdapter(getActivity(), mTopicModels);
        mAdapter.setNodeClickable(false);
        mRecyclerView.setAdapter(mAdapter); //大工告成

        flContainer = (FrameLayout) layout.findViewById(R.id.fl_container);

        return layout;
    }


    private void getTopicsByOK(String requestURL) {

        HttpHelper.OK_CLIENT.newCall(new Request.Builder().headers(HttpHelper.baseHeaders)
                .url(requestURL)
                .get()
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                XLog.tag("TopicFragment").e("error OK");
                handler.sendEmptyMessage(MSG_FAILED);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Document html = Jsoup.parse(response.body().string());
                List<TopicModel> topicList = NetManager.parseTopicLists(html, 0);

                XLog.i("TOPICS" + topicList);
                if (topicList == null || topicList.isEmpty()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewUtil.showNoContent(getActivity(), flContainer);
                            mSwipeLayout.setRefreshing(false);
                        }
                    });
                    return;
                }

                mTopicModels.clear();
                mTopicModels.addAll(topicList);
                handler.sendEmptyMessage(MSG_GET_DATA_BY_OK);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            mSwipeLayout.setRefreshing(true);
            getTopicsByOK(mRequestURL);
        }
        return super.onOptionsItemSelected(item);
    }

    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

}
