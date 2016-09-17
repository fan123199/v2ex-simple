package im.fdx.v2ex.ui.node;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;

public class NodeActivity extends AppCompatActivity {
    private static final String TAG = NodeActivity.class.getSimpleName();
    NodeModel nodeModel;
    RelativeLayout rlNode;
    NetworkImageView ivNodeIcon;
    TextView tvNodeName;
    TextView tvNodeHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rlNode = (RelativeLayout) findViewById(R.id.rl_node);
        ivNodeIcon = (NetworkImageView) findViewById(R.id.iv_node_image);
        tvNodeName = (TextView) findViewById(R.id.tv_node_name);
        tvNodeHeader = (TextView) findViewById(R.id.tv_node_header);

        long nodeId = getIntent().getLongExtra(Keys.KEY_NODE_ID, -1L);

        String requestURL = JsonManager.NODE_JSON + "?id=" + nodeId;
        Log.i(TAG,requestURL);

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

                        ImageLoader imageloader = VolleyHelper.getInstance().getImageLoader();
//                        HintUI.T(getApplication(), nodeModel.getHeader());
                        ivNodeIcon.setImageUrl(nodeModel.getAvatarLargeUrl(),imageloader);
                        tvNodeName.setText(nodeModel.getTitle());
                        tvNodeHeader.setText(nodeModel.getHeader());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HintUI.S(rlNode, "getNothing");
                    }
                }
        );
        VolleyHelper.getInstance().addToRequestQueue(stringRequest);
    }

}
