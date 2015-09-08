package im.fdx.v2ex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.DetailsAdapter;
import im.fdx.v2ex.utils.L;
import im.fdx.v2ex.utils.V2exJsonManager;

public class DetailsActivity extends Activity {

    public RecyclerView mDetailRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DetailsAdapter mDetailAdapter;


    ArrayList<ReplyModel> replyModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent mGetIntent = getIntent();
        mGetIntent.getData();//// TODO: 15-9-8 Do it next time 


        mDetailRecyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mDetailRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_details);
        GetJson();
        mDetailAdapter = new DetailsAdapter(this);
        //// TODO: 15-9-8  
        mDetailRecyclerView.setAdapter(mDetailAdapter);
        


    }

    public void GetJson() {

        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(Request.Method.GET,
                V2exJsonManager.LATEST_JSON, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                parseDetailJsonArray(response);

//                L.m(String.valueOf(Latest));
                mDetailAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                handleVolleyError(error);//// TODO: 15-9-8 重构volleyerror.

            }
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }

    private void parseDetailJsonArray(JSONArray response) {

        if(response == null || response.length() == 0) {
            return;
        }
        long id;
        String author;
        String content;

        long created;
        int thanks;

        try {
            for(int i = 0; i< response.length();i++) {

                JSONObject responseJSONObject = response.getJSONObject(i);

                id = responseJSONObject.optInt("id");

                author = responseJSONObject.optJSONObject("member").optString("username");
                content = responseJSONObject.optString("content");
                thanks = responseJSONObject.optInt("thanks");
                created = responseJSONObject.optLong("created");

                replyModels.add(i, new ReplyModel(id,content,thanks,created,author));
            }

        } catch (JSONException e) {
            L.m("parse false");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
