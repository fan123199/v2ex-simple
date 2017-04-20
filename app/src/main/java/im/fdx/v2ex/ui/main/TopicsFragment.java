package im.fdx.v2ex.ui.main;

import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.utils.EasyClickListener;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.network.MyGsonRequest;
import im.fdx.v2ex.utils.RecyclerTouchListener;
import im.fdx.v2ex.utils.SmoothLayoutManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static im.fdx.v2ex.network.NetManager.HTTPS_V2EX_BASE;

// 2015/10/12 How and When to receive the params in fragment's lifecycle
// simplify it, receive in onCreateView
public class TopicsFragment extends Fragment {

    private static final String TAG = TopicsFragment.class.getSimpleName();

    public static final int LATEST_TOPICS = 1;
    public static final int TOP_10_TOPICS = 2;
    private static final int MSG_FAILED = 3;


    private List<TopicModel> mTopicModels = new ArrayList<>();

    private TopicsRVAdapter mAdapter;
    private int MSG_GET_DATA_BY_OK = 1;


    private SwipeRefreshLayout mSwipeLayout;
    private String mRequestURL;
    private int mMNodeID;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private RelativeLayout container;

    //    private OnFragmentInteractionListener mListener;

    public TopicsFragment() {
        // Required empty public constructor
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == MSG_GET_DATA_BY_OK) {
//                XLog.tag("TopicsFragment").d("GET MESSAGE");
                mAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);
            } else if (msg.what == MSG_FAILED) {
                mSwipeLayout.setRefreshing(false);
            }

            return false;
        }
    });

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
        mMNodeID = args.getInt(Keys.KEY_COLUMN_ID, 0);
        String mTabs = args.getString(Keys.KEY_TAB);
        if (args.getInt("type", -1) == 1) {
            mRequestURL = HTTPS_V2EX_BASE + "/my/topics";
            getTopicsByOK(mRequestURL);
        } else if (args.getInt("type", -1) == 2) {
            mRequestURL = HTTPS_V2EX_BASE + "/my/following";
            getTopicsByOK(mRequestURL);
        } else if (mMNodeID == LATEST_TOPICS) {
            mRequestURL = NetManager.API_LATEST;
            getTopicsJsonByVolley(mRequestURL);
        } else if (mMNodeID == TOP_10_TOPICS) {
            mRequestURL = NetManager.API_HOT;
            getTopicsJsonByVolley(mRequestURL);
        } else if (mMNodeID == 0) {
            if (mTabs != null && !mTabs.isEmpty())
                if (mTabs.equals("recent")) {
                    mRequestURL = HTTPS_V2EX_BASE + "/recent";
                } else {
                    mRequestURL = HTTPS_V2EX_BASE + "/?tab=" + mTabs;
                }
            getTopicsByOK(mRequestURL);
        }

        mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getTopicsJsonByVolley(mRequestURL);
                refresh();
            }
        });
        mSwipeLayout.setRefreshing(true);

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        //这里用线性显示 类似于listView
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setLayoutManager(new SmoothLayoutManager(getActivity()));


        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_main);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            boolean isFabShowing = true;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                ValueAnimator animator = ValueAnimator.ofInt(100, 200, 300, 400);
                animator.setDuration(1000);

                Animation animation = new TranslateAnimation(Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE, 100);

                animation.setRepeatCount(1);
                if (dy > 0) {
                    hideFab();
                }

                if (dy < 0) {
                    showFab();

                }
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

        mAdapter = new TopicsRVAdapter(getActivity(), mTopicModels);
        mRecyclerView.setAdapter(mAdapter); //大工告成


        this.container = (RelativeLayout) layout.findViewById(R.id.rv_container);

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

                if (topicList == null || topicList.isEmpty()) {
                    final TextView child = new TextView(getActivity());
                    child.setText("没有内容");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeLayout.setRefreshing(false);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            params.addRule(RelativeLayout.CENTER_VERTICAL);
                            container.addView(child, params);
                        }
                    });
                    return;
                }

                mTopicModels.clear();
                mTopicModels.addAll(topicList);
//                XLog.tag("TopicFragment").d("done, get topic models");
                handler.sendEmptyMessage(MSG_GET_DATA_BY_OK);
            }
        });
    }

    @Deprecated
    private void getTopicsJsonByVolley(String requestURL) {
        Log.i(TAG, "In get json" + mMNodeID);
        Type typeOfT = new TypeToken<ArrayList<TopicModel>>() {
        }.getType();
        MyGsonRequest<ArrayList<TopicModel>> topicGson = new MyGsonRequest<>(requestURL, typeOfT, new Response.Listener<ArrayList<TopicModel>>() {
            @Override
            public void onResponse(ArrayList<TopicModel> response) {
                Log.i(TAG, "I refresh" + mMNodeID);

                if (mTopicModels.equals(response)) {
                    mAdapter.notifyDataSetChanged();
                    mSwipeLayout.setRefreshing(false);
                    return;
                }

                mTopicModels.clear();
                mTopicModels.addAll(response);

                mAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetManager.handleVolleyError(getActivity(), error);
                mSwipeLayout.setRefreshing(false);
            }
        });

        VolleyHelper.getInstance().addToRequestQueue(topicGson);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_topic_fragement, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            mSwipeLayout.setRefreshing(true);
//            getTopicsJsonByVolley(mRequestURL);
            refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        if (mMNodeID == 0) {
            getTopicsByOK(mRequestURL);
        } else {
            getTopicsJsonByVolley(mRequestURL);
        }
    }

    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
