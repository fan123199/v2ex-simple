package im.fdx.v2ex.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.MySingleton;
import im.fdx.v2ex.ui.adapter.NodeAdapter;
import im.fdx.v2ex.utils.Keys;
import im.fdx.v2ex.utils.L;

public class NodeActivity extends AppCompatActivity {
    NodeModel nodeModel= new NodeModel();
    RecyclerView rvNode;
    NetworkImageView ivNode;
    TextView tvNode;
    TextView tvNodeHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivNode = (NetworkImageView) findViewById(R.id.ivNodeImage);
        tvNode = (TextView) findViewById(R.id.tvNodeName);
        tvNodeHeader = (TextView) findViewById(R.id.tvNodeHeader);

        String nodeName = getIntent().getStringExtra(Keys.KEY_NODE_NAME);
        String requestURL = JsonManager.NODE_JSON + "?name=" + nodeName;
        L.m(requestURL);

        getNodeInfoJson(requestURL);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void getNodeInfoJson(String url) {
        StringRequest stringRequest = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        nodeModel = JsonManager.myGson.fromJson(response,NodeModel.class);
                        ImageLoader imageloader = MySingleton.getInstance().getImageLoader();
                        L.T(getApplication(), nodeModel.getHeader());
                        ivNode.setImageUrl(nodeModel.getAvatarLargeUrl(),imageloader);
                        tvNode.setText(nodeModel.getName());
                        tvNodeHeader.setText(nodeModel.getHeader());


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.S(rvNode, "hehe");
                    }
                }
        );
        MySingleton.getInstance().addToRequestQueue(stringRequest);
    }

}
