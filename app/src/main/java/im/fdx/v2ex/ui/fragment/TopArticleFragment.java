package im.fdx.v2ex.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
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
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.RecyclerTouchListener;
import im.fdx.v2ex.utils.JsonManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopArticleFragment extends Fragment {

    private ArrayList<TopicModel> TOP10 = new ArrayList<>();

    RecyclerView mRecyclerView;
    MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;//TODO
    SwipeRefreshLayout mSwipeLayout;


    //    MySingleton mSingleton;//暂时不用,调试context
    public RequestQueue queue;


    public TopArticleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_latest_article, container, false);

        queue = MySingleton.getInstance(this.getActivity()).getRequestQueue();
//        mSingleton = MySingleton.getInstance(this.getActivity());
        GetJson();

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));//这里用线性显示 类似于listview
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("topic_id", TOP10.get(position).getTopicId());
                intent.putExtra("model", TOP10.get(position));
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
        mAdapter.setTopic(TOP10);
        mRecyclerView.setAdapter(mAdapter); //大工告成
//        L.m("显示Top成功");

        mSwipeLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        GetJson();
                    }
                });

            }

        });
        // Inflate the layout for this fragment
        return layout;
    }
    public void GetJson() {

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                JsonManager.HOT_JSON, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                JsonManager.handleJson(response, TOP10);

//                L.m(String.valueOf(TOP10));
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

        MySingleton.getInstance(this.getActivity()).addToRequestQueue(jsonArrayRequest);

    }
}
