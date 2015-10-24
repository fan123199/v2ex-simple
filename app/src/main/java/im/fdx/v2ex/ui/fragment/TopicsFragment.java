package im.fdx.v2ex.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.DetailsActivity;
import im.fdx.v2ex.ui.adapter.MainAdapter;
import im.fdx.v2ex.utils.ClickListener;
import im.fdx.v2ex.utils.RecyclerTouchListener;
import im.fdx.v2ex.utils.JsonManager;

// 2015/10/12 How and When to receive the params in fragment's lifecycle
// simplify it, receive in onCreateView
public class TopicsFragment extends Fragment {

    public static final int LatestTopics = -1;
    public static final int Top10Topics = -2;


    private ArrayList<TopicModel> Latest = new ArrayList<>();

    private MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;//TODO
    private SwipeRefreshLayout mSwipeLayout;

    private RequestQueue queue;
    private int mNodeID;
//    private OnFragmentInteractionListener mListener;

    public TopicsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle gets_args = getArguments();
        mNodeID = gets_args.getInt("node_id", 0);


        final View layout = inflater.inflate(R.layout.fragment_latest_article, container, false);

        queue = MySingleton.getInstance().getRequestQueue();
        GetJson(mNodeID);

        //找出recyclerview,并赋予变量
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));//这里用线性显示 类似于listView
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("topic_id", Latest.get(position).getTopicId());
                intent.putExtra("model", Latest.get(position));
                //动画实现bug，先放着，先实现核心功能。// TODO: 15-9-14
//                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),view.findViewById(R.id.tvContent),"header");

                getActivity().startActivity(intent);
//                L.t(getActivity(), "短按");
            }

            @Override
            public void onLongClick(View view, int position) {
//                L.t(getActivity(), "长按");

            }
        }));
        mAdapter = new MainAdapter(this.getActivity());
        mAdapter.setTopic(Latest);
        mRecyclerView.setAdapter(mAdapter); //大工告成
//        L.m("显示Latest成功");

        mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetJson(mNodeID);
            }
        });

        // Inflate the layout for this fragment
        return layout;
    }

    private void GetJson(int nodeID) {
        String requestURL = "";
        if(nodeID == LatestTopics){
            requestURL = JsonManager.LATEST_JSON;
        }
        else if(nodeID == Top10Topics){
            requestURL = JsonManager.HOT_JSON;
        }

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                requestURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                JsonManager.handleJson(response, Latest);
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

        MySingleton.getInstance().addToRequestQueue(jsonArrayRequest);

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
