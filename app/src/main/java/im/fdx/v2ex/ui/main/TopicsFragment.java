package im.fdx.v2ex.ui.main;

import android.app.Fragment;
import android.os.Bundle;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.GsonSimple;

import static im.fdx.v2ex.network.JsonManager.myGson;

// 2015/10/12 How and When to receive the params in fragment's lifecycle
// simplify it, receive in onCreateView
public class TopicsFragment extends Fragment {

    private static final String TAG = TopicsFragment.class.getSimpleName();

    public static final int LATEST_TOPICS = 11;
    public static final int TOP_10_TOPICS = 2;


    private List<TopicModel> topicModels = new ArrayList<>();

    private MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;//TODO
    private SwipeRefreshLayout mSwipeLayout;

    private String requestURL;
    private int mMNodeID;
    //    private OnFragmentInteractionListener mListener;

    public TopicsFragment() {
        // Required empty public constructor
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
        Bundle gets_args = getArguments();
        mMNodeID = gets_args.getInt("column_id", 0);

        if (mMNodeID == LATEST_TOPICS) {
            requestURL = JsonManager.LATEST_JSON;
        } else if (mMNodeID == TOP_10_TOPICS) {
            requestURL = JsonManager.HOT_JSON;
        }


        mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getJson(requestURL);

            }
        });
        getJson(requestURL);
        mSwipeLayout.setRefreshing(true);

        //setTopic 不好，还是在创建Apter时加入参数比较好。
//        mAdapter.setTopic(topicModels);


        //找出recyclerview,并赋予变量
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        //这里用线性显示 类似于listView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new MainAdapter(getActivity(), topicModels);
        mRecyclerView.setAdapter(mAdapter); //大工告成


        // TODO: 16/4/30 不用自定义的Listener。 因为子视图的点击问题，无法屏蔽父视图的点击。
//        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
//                mRecyclerView, new EasyClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                intent.putExtra("model", topicModels.get(position));
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

    private void getJson(String requestURL) {
        Log.i(TAG,"In getjson" + mMNodeID);

        Type typeOfT = new TypeToken<ArrayList<TopicModel>>() {
        }.getType();
        GsonSimple<ArrayList<TopicModel>> topicGson = new GsonSimple<>(requestURL, typeOfT, new Response.Listener<ArrayList<TopicModel>>() {
            @Override
            public void onResponse(ArrayList<TopicModel> response) {
                Log.i(TAG,"I refresh" + mMNodeID);

                if (topicModels.equals(response)) {
                    mAdapter.notifyDataSetChanged();
                    mSwipeLayout.setRefreshing(false);
                    return;
                }

                topicModels.clear();
                topicModels.addAll(0, response);

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
            getJson(requestURL);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        super.onStart();



    }
}
