package im.fdx.v2ex.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.MyGsonRequest;
import im.fdx.v2ex.utils.SmoothManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static im.fdx.v2ex.network.JsonManager.HTTPS_V2EX_BASE;

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
    private String mTabs;
    private RecyclerView mRecyclerView;

    //    private OnFragmentInteractionListener mListener;

    public TopicsFragment() {
        // Required empty public constructor
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == MSG_GET_DATA_BY_OK) {
                XLog.tag("TopicsFragment").d("GET MESSAGE");
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
        mTabs = args.getString(Keys.KEY_TAB);

        if (mMNodeID == LATEST_TOPICS) {
            mRequestURL = JsonManager.API_LATEST;
            getTopicsJsonByVolley(mRequestURL);
        } else if (mMNodeID == TOP_10_TOPICS) {
            mRequestURL = JsonManager.API_HOT;
            getTopicsJsonByVolley(mRequestURL);
        } else if (mMNodeID == 0) {
            if (mTabs != null && !mTabs.isEmpty())
                mRequestURL = HTTPS_V2EX_BASE + "/?tab=" + mTabs;
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
        mRecyclerView.setLayoutManager(new SmoothManager(getActivity()));

        mAdapter = new TopicsRVAdapter(getActivity(), mTopicModels);
        mRecyclerView.setAdapter(mAdapter); //大工告成


        // TODO: 16/4/30 不用自定义的Listener。 因为子视图的点击问题，无法屏蔽父视图的点击。
//        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
//                mRecyclerView, new EasyClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                intent.putExtra("model", mTopicModels.get(position));
//                //动画实现bug，先放着，先实现核心功能。// TODO: 15-9-14
////                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
////                    ActivityOptions options = ActivityOptions
////                            .makeSceneTransitionAnimation(getActivity(), view, "headee");
////                    getActivity().startActivity(intent, options.toBundle());
////                } else {
//                getActivity().startActivity(intent);
////                }
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//            }
//        }));

        // Inflate the layout for this fragment
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
                List<TopicModel> c = JsonManager.parseTopicLists(html, 0);
                mTopicModels.clear();
                mTopicModels.addAll(c);
                XLog.tag("TopicFragment").d("done, get topic models");
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
                Log.i(TAG,"I refresh" + mMNodeID);

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
                JsonManager.handleVolleyError(getActivity(), error);
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
