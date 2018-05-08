package im.fdx.v2ex.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.network.NetManager.Source.*
import im.fdx.v2ex.ui.LoginActivity
import im.fdx.v2ex.ui.details.ReplyModel
import im.fdx.v2ex.ui.main.Comment
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.fullUrl
import okhttp3.*
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Integer.parseInt
import java.util.*
import java.util.regex.Pattern

/**
 * Created by a708 on 15-8-13.
 * 用于对API和网页处理的类
 *
 * 这是最苦力活的一个类，而且很有可能会继续改变
 */

object NetManager {

    private val TAG = NetManager::class.java.simpleName

    const val HTTPS_V2EX_BASE = "https://www.v2ex.com"

    const val API_HOT = HTTPS_V2EX_BASE + "/api/topics/hot.json"
    const val API_LATEST = HTTPS_V2EX_BASE + "/api/topics/latest.json"

    //以下,接受参数： name: 节点名
    const val API_NODE = HTTPS_V2EX_BASE + "/api/nodes/show.json"


    const val API_TOPIC = HTTPS_V2EX_BASE + "/api/topics/show.json"

    const val DAILY_CHECK = HTTPS_V2EX_BASE + "/mission/daily"

    const val SIGN_UP_URL = HTTPS_V2EX_BASE + "/signup"

    const val SIGN_IN_URL = HTTPS_V2EX_BASE + "/signin"
    //以下,接受以下参数之一：
    //    username: 用户名
    //    id: 用户在 V2EX 的数字 ID
    const val API_USER = HTTPS_V2EX_BASE + "/api/members/show.json"
    //以下接收参数：
    //     topic_id: 主题ID
    // fdx_comment: 坑爹，官网没找到。怪不得没法子
    @Deprecated("不是实时的")
    const val API_REPLIES = HTTPS_V2EX_BASE + "/api/replies/show.json"

    const val URL_ALL_NODE = HTTPS_V2EX_BASE + "/api/nodes/all.json"

    const val URL_ALL_NODE_WEB = HTTPS_V2EX_BASE + "/planes"

    const val URL_FOLLOWING = "$HTTPS_V2EX_BASE/my/following"

    fun parseToNotifications(html: Document): List<NotificationModel> {
        val body = html.body()
        val items = body.getElementsByAttributeValueStarting("id", "n_") ?: return emptyList()

        val notificationModels = ArrayList<NotificationModel>()
        for (item in items) {
            val notification = NotificationModel()

            val notificationId = item.attr("id").substring(2)
            notification.id = notificationId

            val contentElement = item.getElementsByClass("payload").first()
            val content = when {
                contentElement != null -> contentElement.text()
                else -> "" //
            }
            notification.content = content //1/6

            val time = item.getElementsByClass("snow").first().text()
            notification.time = time// 2/6


            //member model
            val memberElement = item.getElementsByTag("a").first()
            val username = memberElement.attr("href").replace("/member/", "")
            val avatarUrl = memberElement.getElementsByClass("avatar").first().attr("src")
            val memberModel = Member()
            memberModel.username = username
            memberModel.avatar_normal = avatarUrl
            notification.member = memberModel // 3/6

            val topicModel = Topic()

            val topicElement = item.getElementsByClass("fade").first()
            //            <a href="/t/348757#reply1">交互式《线性代数》学习资料</a>
            val href = topicElement.getElementsByAttributeValueStarting("href", "/t/").first().attr("href")

            topicModel.title = topicElement.getElementsByAttributeValueStarting("href", "/t/").first().text()

            val p = Pattern.compile("(?<=/t/)\\d+(?=#)")
            val matcher = p.matcher(href)
            if (matcher.find()) {
                val topicId = matcher.group()
                topicModel.id = topicId
            }

            val p2 = Pattern.compile("(?<=reply)d+\\b")
            val matcher2 = p2.matcher(href)
            if (matcher2.find()) {
                val replies = matcher2.group()
                notification.replyPosition = replies
            }
            notification.topic = topicModel //4/6

            val originalType = topicElement.ownText()
            notification.type =
                    when {
                        originalType.contains("感谢了你在主题") ->
                            "感谢了你:"
                        originalType.contains("回复了你")
                        -> "回复了你:"
                        originalType.contains("提到了你")
                        -> "提到了你:"
                        originalType.contains("收藏了你发布的主题")
                        -> "收藏了你发布的主题:"
                        else -> originalType

                    }

            notificationModels.add(notification)
        }
        return notificationModels
    }

    enum class Source {
        FROM_HOME, FROM_NODE, FROM_MEMBER
    }

    var myGson = Gson()


    @Throws(Exception::class)
    fun parseTopicLists(html: Document, source: Source): List<Topic> {
        val topics = ArrayList<Topic>()

        //用okhttp模拟登录的话获取不到Content内容，必须再一步。

        val body = html.body()

        val items = when (source) {
            FROM_HOME, FROM_MEMBER -> body.getElementsByClass("cell item")
            FROM_NODE -> body.getElementsByAttributeValueStarting("class", "cell from")
        }
        for (item in items!!) {
            val topicModel = Topic()
            val title = item.getElementsByClass("item_title").first().text()

            val linkWithReply = item.getElementsByClass("item_title").first()
                    .getElementsByTag("a").first().attr("href")
            val replies = Integer.parseInt(linkWithReply.split("reply".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])

            val regex = Regex("(?<=/t/)\\d+")
            val id: String = regex.find(linkWithReply)?.value ?: return emptyList()

            val nodeModel = Node()
            when (source) {
                FROM_HOME, FROM_MEMBER -> {
                    //  <a class="node" href="/go/career">职场话题</a>
                    val nodeTitle = item.getElementsByClass("node").text()
                    val nodeName = item.getElementsByClass("node").attr("href").substring(4)
                    nodeModel.title = nodeTitle
                    nodeModel.name = nodeName

                }
            //            <a href="/member/wineway">
            // <img src="//v2" class="avatar" ></a>
                FROM_NODE -> {
                    val header = body.getElementsByClass("node_header").first()
                    val strHeader = header.text()
                    var nodeTitle = ""
                    if (strHeader.contains("›")) {
                        nodeTitle = strHeader.split("›".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
                    }

                    val elements = html.head().getElementsByTag("script")
                    val script = elements.last()
                    //注意，script 的tag 不含 text。
                    val strScript = script.html()
                    val nodeName = strScript.split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    nodeModel.title = nodeTitle
                    nodeModel.name = nodeName
                }
            }

            topicModel.node = nodeModel

            val memberModel = Member()
            //            <a href="/member/wineway">
            // <img src="//v2" class="avatar" border="0" align="default" style="max-width: 48px; max-height: 48px;"></a>
//            val username = item.getElementsByTag("a").first().attr("href").substring(8)

            val username = Regex("(?<=/member/)\\w+").find(item.html())?.value!!

            val avatarLarge = item.getElementsByClass("avatar").attr("src")
            memberModel.username = username
            memberModel.avatar_large = avatarLarge
            memberModel.avatar_normal = avatarLarge.replace("large", "normal")


            val created = when (source) {
                FROM_HOME, FROM_MEMBER -> {
                    val smallItem = item.getElementsByClass("topic_info").first().ownText()

                    when {
                        !smallItem.contains("最后回复") -> -1L
                        else -> {
                            TimeUtil.toUtcTime(smallItem.split("•")[2])
                        }
                    }
                }
                FROM_NODE -> {
                    val smallItem = item.getElementsByClass("small fade").first().ownText()

                    when {
                        !smallItem.contains("最后回复") -> -1L
                        else -> {
                            TimeUtil.toUtcTime(smallItem.split("•")[1])
                        }
                    }
                }
            }
            topicModel.replies = replies
            topicModel.content = ""
            topicModel.content_rendered = ""
            topicModel.title = title

            topicModel.member = memberModel
            topicModel.id = id
            topicModel.created = created
            topics.add(topicModel)
        }
        return topics
    }

    @Throws(Exception::class)
    fun parseToNode(html: Document): Node {

        val nodeModel = Node()
        //        Document html = Jsoup.parse(response);
        val body = html.body()
        val header = body.getElementsByClass("node_header").first()
        val contentElement = header.getElementsByClass("node_info").first().child(0)
        val content = if (contentElement == null) "" else contentElement.text()
        val number = header.getElementsByTag("strong").first().text()
        val strHeader = header.getElementsByClass("node_info").first().ownText().trim()

        if (header.getElementsByTag("img").first() != null) {
            val avatarLarge = header.getElementsByTag("img").first().attr("src")
            nodeModel.avatar_large = avatarLarge
            nodeModel.avatar_normal = avatarLarge.replace("large", "normal")
        }

        val elements = html.head().getElementsByTag("script")
        val script = elements.last()
        //注意，script 的tag 不含 text。
        val strScript = script.html()
        val nodeName = strScript.split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        nodeModel.name = nodeName
        nodeModel.title = strHeader
        nodeModel.topics = Integer.parseInt(number)
        nodeModel.header = content

        return nodeModel
    }

    fun getPageValue(body: Element): IntArray {

        var currentPage = 0
        var totalPage = 0
        val pageInput = body.getElementsByClass("page_input").first() ?: return intArrayOf(-1, -1)
        try {
            currentPage = Integer.parseInt(pageInput.attr("value"))
            totalPage = Integer.parseInt(pageInput.attr("max"))
        } catch (ignored: NumberFormatException) {

        }

        return intArrayOf(currentPage, totalPage)


    }


    /**
     * 处理<pre>标签，使其带有一定的格式。
     */
    private fun handlerPreTag(chapter: Element?): Element? {
        if (chapter == null) {
            return null
        }
        val preElems = chapter.select("pre")
        for (elem in preElems) {
            elem.html(elem.html().replace("\n", "<br/>")/*.replace(" ", "&nbsp;")*/)
        }
        return chapter
    }

    /**
     * @param body    网页
     * *
     * @param topicId todo 可以省略
     * *
     * @return Topic
     */
    fun parseResponseToTopic(body: Element, topicId: String): Topic {
        val topicModel = Topic(topicId)

        val title = body.getElementsByTag("h1").first().text()
        val contentElementOrg = body.getElementsByClass("topic_content").first()

        val commentsEle = body.getElementsByClass("subtle")
        val comments = commentsEle.map {
            Comment().apply {
                this.title = it.getElementsByClass("fade").text().split("·")[0].trim()
                created = TimeUtil.toUtcTime(it.getElementsByClass("fade").text().split("·")[1].trim())
                content = it.getElementsByClass("topic_content").html()
            }
        }

        topicModel.comments = comments.toMutableList()

        val contentElement = handlerPreTag(contentElementOrg)

        val content = if (contentElement == null) "" else contentElement.text()
        topicModel.content = content

        val contentRendered = if (contentElement == null) "" else contentElement.html()

        topicModel.content_rendered = contentRendered.fullUrl()

        val headerTopic = body.getElementById("Main").getElementsByClass("header").first()
        val createdUnformed = headerTopic.getElementsByClass("gray").first().ownText() // · 44 分钟前用 iPhone 发布 · 192 次点击 &nbsp;

        val time = createdUnformed.split("·")[1]
        val created = TimeUtil.toUtcTime(time)

        var replyNum = ""
        val grays = body.getElementsByClass("gray")
        var hasReply = false
        for (gray in grays) {
            if (gray.text().contains("回复") && gray.text().contains("|")) {
                val wholeText = gray.text()
                val index = wholeText.indexOf("回复")
                replyNum = wholeText.substring(0, index - 1)
                if (!replyNum.isEmpty()) {
                    hasReply = true
                }
                break
            }
        }

        val replies = when {
            hasReply -> parseInt(replyNum)
            else -> 0
        }


        val member = Member()
        val username = headerTopic
                .getElementsByAttributeValueStarting("href", "/member/").text()
        member.username = username
        val largeAvatar = headerTopic
                .getElementsByClass("avatar").attr("src")
        member.avatar_large = largeAvatar
        member.avatar_normal = largeAvatar.replace("large", "normal")
        val nodeElement = headerTopic
                .getElementsByAttributeValueStarting("href", "/go/").first()
        val nodeName = nodeElement.attr("href").replace("/go/", "")
        val nodeTitle = nodeElement.text()
        val nodeModel = Node()
        nodeModel.name = nodeName
        nodeModel.title = nodeTitle

        topicModel.member = member //done
        topicModel.replies = replies //done
        topicModel.created = created //done
        topicModel.title = title //done
        topicModel.node = nodeModel//done
        return topicModel
    }

    fun parseResponseToReplay(body: Element): ArrayList<ReplyModel> {

        val replyModels = ArrayList<ReplyModel>()

        val items = body.getElementsByAttributeValueStarting("id", "r_")
        for (item in items) {

            //            <div id="r_4157549" class="cell">
            val id = item.id().substring(2)

            val replyModel = ReplyModel()
            val memberModel = Member()
            val avatar = item.getElementsByClass("avatar").attr("src")
            val username = item.getElementsByTag("strong").first().getElementsByAttributeValueStarting("href", "/member/").first().text()

            memberModel.avatar_large = avatar
            memberModel.avatar_normal = avatar.replace("large", "normal")
            memberModel.username = username

            val thanksOriginal = item.getElementsByClass("small fade").text()
            val thanks = when (thanksOriginal) {
                "" -> 0
                else -> parseInt(thanksOriginal.replace("♥ ", "").trim())
            }
            val thanked = item.getElementsByClass("thank_area thanked").first()
            replyModel.isThanked = thanked != null && "感谢已发送" == thanked.text()

            val createdOriginal = item.getElementsByClass("ago").text()
            val replyContent = item.getElementsByClass("reply_content").first()
            replyModel.created = TimeUtil.toUtcTime(createdOriginal)
            replyModel.member = memberModel
            replyModel.thanks = thanks

            replyModel.id = id
            replyModel.content = replyContent.text()
            replyModel.content_rendered = replyContent.html().fullUrl()
            replyModels.add(replyModel)
        }
        return replyModels

    }

    @JvmOverloads
    fun dealError(context: Context?, errorCode: Int = -1, swipe: SwipeRefreshLayout? = null) {

        if (context is Activity && !context.isFinishing) {
            context.runOnUiThread {
                swipe?.isRefreshing = false
                when (errorCode) {
                    -1 -> context.toast(context.getString(R.string.error_network))
                    302 -> context.toast(context.getString(R.string.error_auth_failure))
                    else -> context.toast(context.getString(R.string.error_network))
                }
            }
        }
    }

    fun getErrorMsg(body: String?): String {
        XLog.tag(TAG).d(body)
        val element = Jsoup.parse(body).body()
        val message = element.getElementsByClass("problem") ?: return ""
        return message.text().trim()
    }

    @Throws(Exception::class)
    fun parseOnce(body: Element) = body.getElementsByAttributeValue("name", "once").first()?.attr("value")

    @Throws(Exception::class)
    fun parseToVerifyCode(body: Element): String? {

        //        <a href="/favorite/topic/349111?t=eghsuwetutngpadqplmlnmbndvkycaft" class="tb">加入收藏</a>
        val element: Element?
        try {
            element = body.getElementsByClass("topic_buttons").first().getElementsByTag("a").first()
        } catch (e: NullPointerException) {
            return null
        }

        if (element != null) {
            val p = Pattern.compile("(?<=favorite/topic/\\d{1,10}\\?t=)\\w+")

            val matcher = p.matcher(element.outerHtml())
            if (matcher.find()) {
                return matcher.group()
            }
        }
        return null
    }

    fun showTwoStepDialog(activity: Activity) {
        val dialogEt = LayoutInflater.from(activity).inflate(R.layout.dialog_et, null)
        val etCode = dialogEt.findViewById<EditText>(R.id.et_two_step_code)
        AlertDialog.Builder(activity, R.style.AppTheme_Simple)
                .setTitle("您开启了两步验证")
                .setPositiveButton("验证") { _, _ ->
                    NetManager.finishLogin(etCode.text.toString(), activity)
                }
                .setNegativeButton("退出登录") { _, _ ->
                    HttpHelper.myCookieJar.clear()
                    MyApp.get().setLogin(false)
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(Intent(Keys.ACTION_LOGOUT))
                }
                .setView(dialogEt).show()
    }

    fun finishLogin(code: String, activity: Activity) {
        val twoStepUrl = "https://www.v2ex.com/2fa"
        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url(twoStepUrl)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (response?.code() == 200) {
                    val bodyStr = response.body()?.string()
                    val once = parseOnce(Jsoup.parse(bodyStr)) ?: "0"
                    val body: RequestBody = FormBody.Builder()
                            .add("code", code)
                            .add("once", once).build()
                    HttpHelper.OK_CLIENT.newCall(Request.Builder()
                            .post(body)
                            .url(twoStepUrl)
                            .build()).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            activity.runOnUiThread {
                                if (response?.code() == 302) {
                                    activity.toast("登录成功")
                                    if (activity is LoginActivity) {
                                        activity.finish()
                                    }
                                } else activity.toast("登录失败")
                            }
                        }
                    })
                }
            }
        })
    }

    fun parseToNode(string: String): ArrayList<Node> {
        val element = Jsoup.parse(string).body()

        val nodeModels = ArrayList<Node>()
        val items = element.getElementsByClass("grid_item")

        for (item in items) {
            val nodeModel = Node()
            val id = item.attr("id").substring(2)
            nodeModel.id = id

            val title = item.getElementsByTag("div").first().ownText().trim { it <= ' ' }
            nodeModel.title = title
            val name = item.attr("href").replace("/go/", "")
            nodeModel.name = name

            val num = item.getElementsByTag("span").first().ownText().trim { it <= ' ' }

            nodeModel.topics = parseInt(num)

            val imageUrl = item.getElementsByTag("img").first().attr("src")
            nodeModel.avatar_large = imageUrl
            nodeModels.add(nodeModel)
        }

        return nodeModels
    }

    fun getAllNode(html: String): MutableMap<String, MutableList<Node>> {
        val allNodes = mutableMapOf<String, MutableList<Node>>()

        val document: Document = Jsoup.parse(html)
        val body = document.body()
        val main = body.getElementsByAttributeValue("id", "Main").getOrNull(0)
        val boxes: Elements? = main?.getElementsByClass("box")

        boxes?.filterIndexed { index, _ -> index > 0 }?.forEach {
            val title = it.getElementsByClass("header").first().ownText()

            val nodes = mutableListOf<Node>()
            val nodeElements = it.getElementsByClass("inner").first().getElementsByClass("item_node")
            for (item in nodeElements) {
                val node = Node()
                val name = item.attr("href").replace("/go/", "")
                node.name = name
                node.title = item.text()
                nodes.add(node)
            }

            allNodes[title] = nodes
        }
        return allNodes
    }

    fun parseMember(body: Document): Member {
        //member model

//        https@ //v2ex.assets.uxengine.net/gravatar/afff3555384ccab3cd9c51f9682bef51?s=48&d=retro
        val rightbar = body.getElementById("Rightbar")
        val memberElement = rightbar.getElementsByTag("a").first()
        val username = memberElement.attr("href").replace("/member/", "")
        val avatarUrl = memberElement.getElementsByClass("avatar").first().attr("src")
        val memberModel = Member()
        memberModel.username = username
        memberModel.avatar_normal = avatarUrl
        return memberModel
    }
}