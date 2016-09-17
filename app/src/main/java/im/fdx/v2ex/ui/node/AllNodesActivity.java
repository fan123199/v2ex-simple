package im.fdx.v2ex.ui.node;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Node;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.GsonSimple;

import static android.R.attr.switchMinWidth;
import static android.R.attr.type;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.N;
import static android.support.v7.widget.StaggeredGridLayoutManager.HORIZONTAL;
import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public class AllNodesActivity extends AppCompatActivity {
    List<NodeModel> nodeModels;
    private AllNodesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_nodes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("所有节点");


        Type type = new TypeToken<ArrayList<NodeModel>>(){}.getType();
        GsonSimple<ArrayList<NodeModel>> nodeModelGsonSimple  = new GsonSimple<>(JsonManager.URL_ALL_NODE, type,
                new Response.Listener<ArrayList<NodeModel>>() {
                    @Override
                    public void onResponse(ArrayList<NodeModel> response) {
                        adapter.setData(response);
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleyHelper.getInstance().addToRequestQueue(nodeModelGsonSimple);


        adapter = new AllNodesAdapter();
//        adapter.setData(nodeModels);
        RecyclerView rvNode = (RecyclerView) findViewById(R.id.rv_node);
        RecyclerView.LayoutManager  layoutManager = new StaggeredGridLayoutManager(3, VERTICAL);
        rvNode.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
            }


            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

            }
        });
        rvNode.setLayoutManager(layoutManager);

        rvNode.setAdapter(adapter);
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

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return false;
    }
}
