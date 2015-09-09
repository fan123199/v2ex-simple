package im.fdx.v2ex.ui.fragment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.DetailsActivity;
import im.fdx.v2ex.ui.adapter.MainAdapter;
import im.fdx.v2ex.utils.ClickListener;
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.RecyclerTouchListener;
import im.fdx.v2ex.utils.V2exJsonManager;


public class ArticleFragment extends Fragment {


    private ArrayList<TopicModel> Latest = new ArrayList<>();

    RecyclerView mRecyclerView;
    MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;//TODO
    SwipeRefreshLayout mSwipeLayout;


//    MySingleton mSingleton;//暂时不用,调试context
    public RequestQueue queue;
//    private OnFragmentInteractionListener mListener;

    public ArticleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout = inflater.inflate(R.layout.fragment_article, container, false);

        queue = MySingleton.getInstance(this.getActivity()).getRequestQueue();
//        mSingleton = MySingleton.getInstance(this.getActivity());
        GetJson();

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));//这里用线性显示 类似于listview
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                view.setTransitionName("header");
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("topic_id", Latest.get(position).getTopicId());
                intent.putExtra("model",Latest.get(position));
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),view,"header");

                getActivity().startActivity(intent, options.toBundle());
                L.t(getActivity(), "短按");
                view.setTransitionName("");
            }

            @Override
            public void onLongClick(View view, int position) {
                L.t(getActivity(), "长按");

            }
        }));
        mAdapter = new MainAdapter(this.getActivity());
        mAdapter.setTopic(Latest);
        mRecyclerView.setAdapter(mAdapter); //大工告成
        L.m("显示成功");

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
                V2exJsonManager.LATEST_JSON, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                parseJsonArray(response);

                L.m(String.valueOf(Latest));
                mAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyError(error);
            }
        });

        MySingleton.getInstance(this.getActivity()).addToRequestQueue(jsonArrayRequest);

    }

    private void handleVolleyError(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            L.m(getString(R.string.error_timeout));
            //TODO
        } else if (error instanceof AuthFailureError) {
            L.m(getString(R.string.error_auth_failure));

        } else if (error instanceof ServerError) {
            L.m(getString(R.string.error_auth_failure));

        } else if (error instanceof NetworkError) {
            L.m(getString(R.string.error_network));

        } else if (error instanceof ParseError) {
            L.m(getString(R.string.error_parser));

        }
    }

    public void parseJsonArray(JSONArray response) {
        if(response == null || response.length() == 0) {
            return;
        }
        long id;
        String title;
        String author;
        String content;
        int replies;
        String node_title;
        long created;

        boolean flag = true;

        if(Latest.isEmpty()) {
            flag = false;
        }

//        L.m(String.valueOf(flag));

        try {
            for(int i = 0; i< response.length();i++) {

                JSONObject responseJSONObject = response.getJSONObject(i);

                id = responseJSONObject.optInt("id");
                title = responseJSONObject.optString("title");
                author = responseJSONObject.optJSONObject("member").optString("username");
                content = responseJSONObject.optString("content");
                replies = responseJSONObject.optInt("replies");
                node_title = responseJSONObject.optJSONObject("node").optString("title");
                created = responseJSONObject.optLong("created");
                if(flag) {
                    if (id == Latest.get(i).id) {
                        break;
                    }
                }

                Latest.add(i, new TopicModel(id, title, author, content, replies, node_title, created));
            }

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
