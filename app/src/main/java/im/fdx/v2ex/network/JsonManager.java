package im.fdx.v2ex.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.elvishew.xlog.XLog;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.MemberModel;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.model.TopicModel;
import im.fdx.v2ex.utils.ContentUtils;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.TimeHelper;

import static java.lang.Integer.parseInt;

/**
 * Created by a708 on 15-8-13.
 * 用于对API和网页处理的类
 */


public class JsonManager {

    private static final String TAG = JsonManager.class.getSimpleName();

    public static final String HTTPS_V2EX_BASE = "https://www.v2ex.com";
    public static final String HTTP_V2EX_BASE = "http://www.v2ex.com";
    public static final String API_HOT = HTTPS_V2EX_BASE + "/api/topics/hot.json";
    public static final String API_LATEST = HTTPS_V2EX_BASE + "/api/topics/latest.json";
    //以下,接受参数： name: 节点名
    public static final String API_NODE = HTTPS_V2EX_BASE + "/api/nodes/show.json";


    public static final String API_TOPIC = HTTPS_V2EX_BASE + "/api/topics/show.json";

    public static final String DAILY_CHECK = "https://www.v2ex.com/mission/daily";

    public static final String SIGN_UP_URL = HTTPS_V2EX_BASE + "/signup";

    public static final String SIGN_IN_URL = HTTPS_V2EX_BASE + "/signin";
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    public static final String API_USER = "https://www.v2ex.com/api/members/show.json";
    //以下接收参数：
    //     topic_id: 主题ID
    // fdx_comment: 坑爹，官网没找到。怪不得没法子
    public static final String API_REPLIES = "https://www.v2ex.com/api/replies/show.json";

    public static final int MY_TIMEOUT_MS = 4000;

    public static final int MY_MAX_RETRIES = 1;
    public static final String URL_ALL_NODE =HTTPS_V2EX_BASE + "/api/nodes/all.json";
    public static final int FROM_HOME = 0;
    public static final int FROM_NODE = 1;

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

    public static List<TopicModel> parseTopicLists(Document html, int source) {
        ArrayList<TopicModel> topics = new ArrayList<>();

        //用okhttp模拟登录的话获取不到Content内容，必须再一步。

//        Document html = Jsoup.parse(string);
        Element body = html.body();

        Elements items;
        switch (source) {
            case FROM_HOME:
                items = body.getElementsByClass("cell item");
                break;
            case FROM_NODE:
                items = body.getElementsByAttributeValueStarting("class", "cell from");
                break;
            default:
                items = body.getElementsByClass("cell item");
        }
        for (Element item :
                items) {
            TopicModel topicModel = new TopicModel();
            String title = item.getElementsByClass("item_title").first().text();

            String linkWithReply = item.getElementsByClass("item_title").first()
                    .getElementsByTag("a").first().attr("href");
            int replies = Integer.parseInt(linkWithReply.split("reply")[1]);
            Pattern p = Pattern.compile("(?<=/t/)\\d+");
            Matcher matcher = p.matcher(linkWithReply);

            long id;
            if (matcher.find()) {
                id = Long.parseLong(matcher.group());
            } else {
                return Collections.emptyList();
            }


            NodeModel nodeModel = new NodeModel();
            if (source == FROM_HOME) {
                //  <a class="node" href="/go/career">职场话题</a>
                String nodeTitle = item.getElementsByClass("node").text();
                String nodeName = item.getElementsByClass("node").attr("href").substring(4);
                nodeModel.setTitle(nodeTitle);
                nodeModel.setName(nodeName);

            } else if (source == FROM_NODE) {
                Element header = body.getElementsByClass("header").first();
                String strHeader = header.text();
                String nodeTitle = "";
                if (strHeader.contains("›")) {
                    nodeTitle = strHeader.split("›")[1].split(" ")[1].trim();
                    XLog.tag(TAG).d("nodeTitle: " + nodeTitle);
                }

                Elements elements = html.head().getElementsByTag("script");
                Element script = elements.last();
                //注意，script 的tag 不含 text。
                String strScript = script.html();
                String nodeName = strScript.split("\"")[1];
                nodeModel.setTitle(nodeTitle);
                nodeModel.setName(nodeName);
            }


            topicModel.setNode(nodeModel);

            MemberModel memberModel = new MemberModel();
//            <a href="/member/wineway">
// <img src="//v2" class="avatar" border="0" align="default" style="max-width: 48px; max-height: 48px;"></a>
            String username = item.getElementsByTag("a").first().attr("href").substring(8);

            String avatarLarge = item.getElementsByClass("avatar").attr("src");
            memberModel.setUsername(username);
            memberModel.setAvatar_large(avatarLarge);
            memberModel.setAvatar_normal(avatarLarge.replace("large", "normal"));


            String smallItem = item.getElementsByClass("small fade").first().text();
//            XLog.d("small: " + smallItem);
            long created;
            if (!smallItem.contains("最后回复")) {
                created = -1L;
            } else {
                String createdOriginal = "";
                switch (source) {
                    case FROM_HOME:
                        createdOriginal = smallItem.split("•")[2];
                        break;
                    case FROM_NODE:
                        createdOriginal = smallItem.split("•")[1];
                        break;
                }
                created = TimeHelper.toLong(createdOriginal);
            }
            topicModel.setReplies(replies);
            topicModel.setContent("");
            topicModel.setContentRendered("");
            topicModel.setTitle(title);

            topicModel.setMember(memberModel);
            topicModel.setId(id);
            topicModel.setCreated(created);
            topics.add(topicModel);
        }
        return topics;
    }

    public static NodeModel parseToNode(Document html) {

        NodeModel nodeModel = new NodeModel();
//        Document html = Jsoup.parse(response);
        Element body = html.body();
        Element header = body.getElementsByClass("header").first();
        Element contentElement = header.getElementsByClass("f12 gray").first();
        String content = contentElement == null ? "" : contentElement.text();
        String number = header.getElementsByTag("strong").first().text();
        String strHeader = header.text();
        String nodeTitle = "";
        if (strHeader.contains("›")) {
            nodeTitle = strHeader.split("›")[1].split(" ")[1].trim();
            XLog.tag(TAG).d("nodeTitle: " + nodeTitle);
        }
        if (header.getElementsByTag("img").first() != null) {
            String avatarLarge = header.getElementsByTag("img").first().attr("src");
            nodeModel.setAvatar_large(avatarLarge);
            nodeModel.setAvatar_normal(avatarLarge.replace("large", "normal"));
        }

        Elements elements = html.head().getElementsByTag("script");
        Element script = elements.last();
        //注意，script 的tag 不含 text。
        String strScript = script.html();
        String nodeName = strScript.split("\"")[1];

        Log.w("node", "nodeName" + nodeName);
//
//
        nodeModel.setName(nodeName);
        nodeModel.setTitle(nodeTitle);
        nodeModel.setTopics(Integer.parseInt(number));
        nodeModel.setHeader(content);

        return nodeModel;
    }

    //// TODO: 2017/3/16 只有一页回复，这样是不行的
    public static int[] parsePage(Element body) {

        int currentPage = 0;
        int totalPage = 0;
        Element pageInput = body.getElementsByClass("page_input").first();
        try {
            currentPage = Integer.parseInt(pageInput.attr("value"));
            totalPage = Integer.parseInt(pageInput.attr("max"));
        } catch (NumberFormatException e) {

        }
        return new int[]{currentPage, totalPage};


    }

    @NonNull
    public static TopicModel parseResponseToTopic(Element body, long topicId) {
        TopicModel topicModel = new TopicModel(topicId);

        String title = body.getElementsByTag("h1").text();
        Element contentElement = body.getElementsByClass("topic_content").first();

        String content = contentElement == null ? "" : contentElement.text();
        String contentRendered = contentElement == null ? "" : contentElement.html();
        String createdUnformed = body.getElementsByClass("header").
                first().getElementsByClass("gray").first().ownText(); // · 44 分钟前用 iPhone 发布 · 192 次点击 &nbsp;

        String time = createdUnformed.split("·")[1];
        XLog.tag(TAG).d(createdUnformed + "||| " + time);
        long created = TimeHelper.toLong(time);
//long created = -1L;

        String replyNum = "";
        Elements grays = body.getElementsByClass("gray");
        boolean hasReply = false;
        for (Element gray :
                grays) {
            if (gray.text().contains("回复") && gray.text().contains("|")) {
                String wholeText = gray.text();
                int index = wholeText.indexOf("回复");
                replyNum = wholeText.substring(0, index - 1);
                if (!TextUtils.isEmpty(replyNum)) {
                    hasReply = true;
                }
                break;
            }
        }

//        XLog.tag(TAG).d("replyNum  = " + replyNum);
        int replies;
        if (!hasReply) {
            replies = 0;
        } else {
            replies = parseInt(replyNum);
        }

        topicModel.setContentRendered(ContentUtils.format(contentRendered)); //done

        MemberModel member = new MemberModel();


        String username = body.getElementsByClass("header").first()
                .getElementsByAttributeValueStarting("href", "/member/").text();
        member.setUsername(username);
        String largeAvatar = body.getElementsByClass("header").first()
                .getElementsByClass("avatar").attr("src");

        member.setAvatar_large(largeAvatar);
        member.setAvatar_normal(largeAvatar.replace("large", "normal"));
//                            member.setId(0);

        Element nodeElement = body.getElementsByClass("header").first()
                .getElementsByAttributeValueStarting("href", "/go/").first();
        String nodeName = nodeElement.attr("href").replace("/href/", "");
        String nodeTitle = nodeElement.text();

        NodeModel nodeModel = new NodeModel();
        nodeModel.setName(nodeName);
        nodeModel.setTitle(nodeTitle);

        topicModel.setMember(member); //done
        topicModel.setReplies(replies); //done
        topicModel.setCreated(created); //done
        topicModel.setTitle(title); //done
        topicModel.setContent(content); //done
        topicModel.setNode(nodeModel);//done
        return topicModel;
    }

    public static List<ReplyModel> parseResponseToReplay(Element body) {

        ArrayList<ReplyModel> replyModels = new ArrayList<>();

        Elements items = body.getElementsByAttributeValueStarting("id", "r_");
        for (Element item :
                items) {

            ReplyModel replyModel = new ReplyModel();
            MemberModel memberModel = new MemberModel();
            String avatar = item.getElementsByClass("avatar").attr("src");
            String username = item.getElementsByTag("strong").first().
                    getElementsByAttributeValueStarting("href", "/member/").first().text();

//            XLog.d(avatar);
            memberModel.setAvatar_large(avatar);
            memberModel.setAvatar_normal(avatar.replace("large", "normal"));
            memberModel.setUsername(username);

            String thanksOriginal = item.getElementsByClass("small fade").text();
            int thanks;
            if (thanksOriginal.equals("")) {
                thanks = 0;
            } else {
                thanks = parseInt(thanksOriginal.replace("♥ ", ""));
            }

            String createdOriginal = item.getElementsByClass("fade small").text();
//            XLog.i(createdOriginal);
            Element replyContent = item.getElementsByClass("reply_content").first();
//            replyModel.setCreated(-1L);
            replyModel.setCreated(TimeHelper.toLong(createdOriginal));
            replyModel.setMember(memberModel);
            replyModel.setThanks(thanks);

            replyModel.setContent(replyContent.text());
            replyModel.setContent_rendered(ContentUtils.format(replyContent.html()));
            replyModels.add(replyModel);
        }
        return replyModels;

    }
}