package im.fdx.v2ex.ui.node;

import android.app.SearchManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class AllNodesActivity extends AppCompatActivity {
    private AllNodesAdapter mAdapter;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mAdapter.notifyDataSetChanged();
            }


        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_nodes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("所有节点");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        HttpHelper.OK_CLIENT.newCall(new Request.Builder().url(JsonManager.URL_ALL_NODE).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Type type = new TypeToken<ArrayList<NodeModel>>() {
                }.getType();
                ArrayList<NodeModel> nodeModels = JsonManager.myGson.fromJson(response.body().string(), type);
                mAdapter.setAllData(nodeModels);
                handler.sendEmptyMessage(0);
            }
        });


        mAdapter = new AllNodesAdapter();
        RecyclerView rvNode = (RecyclerView) findViewById(R.id.rv_node);
        RecyclerView.LayoutManager  layoutManager = new StaggeredGridLayoutManager(3, VERTICAL);
        rvNode.setLayoutManager(layoutManager);

        rvNode.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_node,menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItemCompat = menu.findItem(R.id.search_node);

        SearchView searchView = (SearchView) menuItemCompat.getActionView();

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                XLog.tag("ALLNODE").d("lala" + newText);
                mAdapter.filter(newText);

                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return false;
    }
}
