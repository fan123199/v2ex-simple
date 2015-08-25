package im.fdx.v2ex.ui.fragment;

import android.app.Activity;
import android.net.Uri;
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
import im.fdx.v2ex.ui.adapter.MainAdapter;
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.V2exJsonManager;


public class ArticleFragment extends Fragment {


    private ArrayList<TopicModel> Top10 = new ArrayList<>();

    RecyclerView mRecyclerView;
    MainAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManger;
    SwipeRefreshLayout mSwipeLayout;

    MySingleton mSingleton;//暂时不用,调试context
    public RequestQueue queue;
//    private OnFragmentInteractionListener mListener;

    public ArticleFragment() {
        // Required empty public constructor
    }

    public static ArticleFragment newInstance() {
        ArticleFragment fragment = new ArticleFragment();
        return fragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_article, container, false);

        queue = MySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue();
        mSingleton = MySingleton.getInstance(this.getActivity());
        GetJson();

        //找出recyclerview,并赋予变量
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));//这里用线性显示 类似于listview
        mAdapter = new MainAdapter(this.getActivity());
        mAdapter.setTopic(Top10);
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
                        L.m("Thread done");
                    }
                });

            }

        });
        // Inflate the layout for this fragment
        return layout;
    }

    public void GetJson() {

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                V2exJsonManager.LATEST_JSON, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                parseJsonArray(response);
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

        } else if (error instanceof AuthFailureError) {
            L.m(getString(R.string.error_auth_failure));
            //TODO
        } else if (error instanceof ServerError) {
            L.m(getString(R.string.error_auth_failure));
            //TODO
        } else if (error instanceof NetworkError) {
            L.m(getString(R.string.error_network));
            //TODO
        } else if (error instanceof ParseError) {
            L.m(getString(R.string.error_parser));
            //TODO
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
        try {
            for(int i = 0; i< response.length();i++) {
                JSONObject responseJSONObject = response.getJSONObject(i);


                id = responseJSONObject.optInt("id");

                title = responseJSONObject.optString("title");

                author = responseJSONObject.optJSONObject("member").optString("username");

                content = responseJSONObject.optString("content");

                replies = responseJSONObject.optInt("replies");

                node_title = responseJSONObject.optJSONObject("node").optString("title");
                if(Top10.size()!=0) {
                    if (id == Top10.get(0).id) break;
                }
                Top10.add(new TopicModel(id, title, author, content, replies, node_title));
            }

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
        L.m(String.valueOf(Top10));
        mAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }


    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
