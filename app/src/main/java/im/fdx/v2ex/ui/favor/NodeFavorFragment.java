package im.fdx.v2ex.ui.favor;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.node.AllNodesAdapter;
import im.fdx.v2ex.ui.node.NodeModel;
import im.fdx.v2ex.utils.ViewUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class NodeFavorFragment extends Fragment {


    private final String url = "https://www.v2ex.com/my/nodes";
    private SwipeRefreshLayout swipe;
    private AllNodesAdapter adapter;

    private FrameLayout flContainer;

    public NodeFavorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        XLog.d("NodeFavorFragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_tab_article, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_container);
        adapter = new AllNodesAdapter(getActivity(), true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        flContainer = (FrameLayout) view.findViewById(R.id.fl_container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipe.setRefreshing(true);
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                .headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url(url)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError(getActivity());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 200) {
                    NetManager.dealError(getActivity(), response.code());
                    return;
                }
                ArrayList<NodeModel> nodeModels = NetManager.parseToNode(response.body().string());
                if (nodeModels == null || nodeModels.isEmpty()) {
                    XLog.d("hehe" + nodeModels);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewUtil.INSTANCE.showNoContent(getActivity(), flContainer);
                            swipe.setRefreshing(false);
                        }
                    });
                    return;
                }

                adapter.clear();
                adapter.addAll(nodeModels);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        swipe.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}