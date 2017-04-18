package im.fdx.v2ex.ui.favor;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.node.AllNodesAdapter;
import im.fdx.v2ex.ui.node.NodeAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class NodeFavorFragment extends Fragment {

    private ArrayList<NodeModel> nodeModels;
    private final String url = "https://www.v2ex.com/my/nodes";
    private View view;

    public NodeFavorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.favor_node, container, false);


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_node);

        final AllNodesAdapter adapter = new AllNodesAdapter(getActivity(), true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url(url)
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 200) {
                    NetManager.dealError();
                    return;
                }

                nodeModels = parseToNode(response.body().string());
                adapter.addAll(nodeModels);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();

                    }
                });
            }
        });


        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    private ArrayList<NodeModel> parseToNode(String string) {
        Element element = Jsoup.parse(string).body();

        ArrayList<NodeModel> nodeModels = new ArrayList<>();
        Elements items = element.getElementsByClass("grid_item");

        for (Element item : items) {
            NodeModel nodeModel = new NodeModel();
            String id = item.attr("id").substring(2);
            nodeModel.setId(Long.valueOf(id));

            String title = item.getElementsByTag("div").first().ownText().trim();
            nodeModel.setTitle(title);
            String name = item.attr("href").replace("/go/", "");
            nodeModel.setName(name);

            String num = item.getElementsByTag("span").first().ownText().trim();

            nodeModel.setTopics(Integer.parseInt(num));

            String imageUrl = item.getElementsByTag("img").first().attr("src");
            nodeModel.setAvatar_large(imageUrl);
            nodeModels.add(nodeModel);
        }

        return nodeModels;
    }
}
