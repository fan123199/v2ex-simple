package im.fdx.v2ex.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.StringRequest;
import com.elvishew.xlog.XLog;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.TimeHelper;

import static android.R.attr.name;
import static android.R.attr.value;

/**
 * Created by a708 on 15-8-13.
 * 用于对Json处理的类
 */

//BasicNetwork.logSlowRequests: HTTP response for request=<[ ]
//        https://www.v2ex.com/api/topics/latest.json 0xe61fa5b9 NORMAL 1>
//        // [lifetime=5462], [size=60681], [rc=200], [retryCount=0]

public class JsonManager {

    public static final String TAG = JsonManager.class.getSimpleName();


    public static final String V2EX_API = "https://www.v2ex.com/api";
    public static final String HTTPS_V2EX_BASE = "https://www.v2ex.com";
    public static final String HTTP_V2EX_BASE = "http://www.v2ex.com";
    public static final String HOT_JSON = HTTPS_V2EX_BASE+"/api/topics/hot.json";
    public static final String LATEST_JSON = HTTPS_V2EX_BASE + "/api/topics/latest.json";
    //以下,接受参数： name: 节点名
    public static final String NODE_JSON = HTTPS_V2EX_BASE + "/api/nodes/show.json";


    public static final String NODE_TOPIC = HTTPS_V2EX_BASE + "/api/topics/show.json";


    public static final String SIGN_UP_URL = HTTPS_V2EX_BASE + "/signup";

    public static final String SIGN_IN_URL = HTTPS_V2EX_BASE + "/signin";
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    public static final String USER_JSON = "https://www.v2ex.com/api/members/show.json";
    //以下接收参数：
    //     topic_id: 主题ID
    // fdx_comment: 坑爹，官网没找到。怪不得没法子
    public static final String API_REPLIES = "https://www.v2ex.com/api/replies/show.json";

    public static final int MY_TIMEOUT_MS = 4000;

    public static final int MY_MAX_RETRIES = 1;
    public static final String URL_ALL_NODE =HTTPS_V2EX_BASE + "/api/nodes/all.json";

    public static void handleVolleyError(Context context,VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Log.e(TAG,context.getString(R.string.error_timeout));
            HintUI.t(context,context.getString(R.string.app_name) + ": " + context.getString(R.string.error_timeout));
        } else if (error instanceof AuthFailureError) {
            Log.e(TAG,context.getString(R.string.error_auth_failure));
            HintUI.t(context,context.getPackageName() + ": " + context.getString(R.string.error_auth_failure));

        } else if (error instanceof ServerError) {
            Log.e(TAG,context.getString(R.string.error_auth_failure));
            HintUI.t(context,context.getPackageName() + ": " + context.getString(R.string.error_auth_failure));

        } else if (error instanceof NetworkError) {
            Log.e(TAG,context.getString(R.string.error_network));
            HintUI.t(context, context.getPackageName() + ": " + context.getString(R.string.error_network));
        } else if (error instanceof ParseError) {
            Log.e(TAG,context.getString(R.string.error_parser));
            HintUI.t(context, context.getPackageName() + ": " + context.getString(R.string.error_parser));
        }
    }

    public static Gson myGson = new Gson();

    public static ArrayList<TopicModel> parseTopics(String string) {
        ArrayList<TopicModel> topics = new ArrayList<>();

        //用okhttp模拟登录的话获取不到Content内容，必须再一步。
        Document element = Jsoup.parse(string);
        Element body = element.body();
        Elements items = body.getElementsByClass("cell item");
        for (Element item :
                items) {
            TopicModel topicModel = new TopicModel();


            String title = item.getElementsByClass("item_title").first().text();
            String linkWithReply = item.getElementsByClass("item_title").first()
                    .getElementsByTag("a").first().attr("href");
            int replies = Integer.parseInt(linkWithReply.split("reply")[1]);
            long id = Long.parseLong(linkWithReply.substring(3, 9));

//            String content = getContent();
//            String content_rendered  = getContentRendered();

            NodeModel nodeModel = new NodeModel();

            //  <a class="node" href="/go/career">职场话题</a>
            String nodeTitle = item.getElementsByClass("node").text();
            String nodeName = item.getElementsByClass("node").attr("href").substring(4);
            nodeModel.setTitle(nodeTitle);
            nodeModel.setName(nodeName);

            MemberModel memberModel = new MemberModel();
//            <a href="/member/wineway">
// <img src="//v2" class="avatar" border="0" align="default" style="max-width: 48px; max-height: 48px;"></a>
            String username = item.getElementsByTag("a").first().attr("href").substring(8);
            memberModel.setUsername(username);

            String smallItem = item.getElementsByClass("small fade").first().text();
            XLog.i(smallItem);
            long created;
            if (!smallItem.contains("最后回复")) {
                created = -1L;
            } else {
                String createdOriginal = item.getElementsByClass("small fade").first().text()
                        .split("•")[2].trim();
                created = TimeHelper.toLong(createdOriginal);
            }
            topicModel.setReplies(replies);
            topicModel.setContent("");
            topicModel.setContentRendered("");
            topicModel.setTitle(title);
            topicModel.setNode(nodeModel);
            topicModel.setMember(memberModel);
            topicModel.setId(id);
            topicModel.setCreated(created);
            topics.add(topicModel);
        }
        return topics;
    }
}