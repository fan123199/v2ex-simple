package im.fdx.v2ex.network

import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.network.Parser.Source.*
import im.fdx.v2ex.ui.details.Reply
import im.fdx.v2ex.ui.main.Comment
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.ui.member.MemberReplyModel
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.fullUrl
import im.fdx.v2ex.utils.extensions.getNum
import im.fdx.v2ex.utils.extensions.logd
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*
import java.util.regex.Pattern

class Parser(private val htmlStr: String) {

    private val doc: Document = Jsoup.parse(htmlStr)

    fun parseTopicLists(source: Source): List<Topic> {

        val topics = ArrayList<Topic>()

        //用okhttp模拟登录的话获取不到Content内容，必须再一步。

        val body = doc.body()

        val items = when (source) {
          FROM_HOME, FROM_MEMBER -> body.getElementsByClass("cell item")
          FROM_NODE -> body.getElementsByAttributeValueStarting("class", "cell from")
          else -> null
        }
        if (items != null) {
            for (item in items) {
                val topicModel = Topic()
                val title = item.getElementsByClass("item_title")?.first()?.text()?:""

                val linkWithReply = item.getElementsByClass("item_title")?.first()
                        ?.getElementsByTag("a")?.first()?.attr("href")?:""
                val replies = Integer.parseInt(linkWithReply.split("reply".toRegex())[1])

                val regex = Regex("(?<=/t/)\\d+")
                val id: String = regex.find(linkWithReply)?.value ?: return emptyList()

                val nodeModel = Node()
                when (source) {
                    FROM_HOME, FROM_MEMBER -> {
                        //  <a class="node" href="/go/career">职场话题</a>
                        val nodeTitle = item.getElementsByClass("node")?.text()
                        val nodeName = item.getElementsByClass("node")?.attr("href")?.substring(4)
                        nodeModel.title = nodeTitle?:""
                        nodeModel.name = nodeName?:""

                    }
                    //            <a href="/member/wineway">
                    // <img src="//v2" class="avatar" ></a>
                    FROM_NODE -> {
                        val strHeader = body.getElementsByClass("node_header")?.first()?.text()?:""
                        var nodeTitle = ""
                        if (strHeader.contains("›")) {
                            nodeTitle = strHeader.split("›".toRegex())[1].split(" ".toRegex())[1].trim { it <= ' ' }
                        }

                        val elements = doc.head().getElementsByTag("script")
                        val script = elements.last()
                        //注意，script 的tag 不含 text。
                        val strScript = script.html()
                        val nodeName = strScript.split("\"".toRegex())[1]
                        nodeModel.title = nodeTitle
                        nodeModel.name = nodeName
                    }
                    else -> {
                    }
                }


                val memberModel = Member()
                //            <a href="/member/wineway">
                // <img src="//v2" class="avatar" border="0" align="default" style="max-width: 48px; max-height: 48px;"></a>
//            val username = item.getElementsByTag("a").first().attr("href").substring(8)

                val username = Regex("(?<=/member/)\\w+").find(item.html())?.value?:""

                val avatarLarge = item.getElementsByClass("avatar")?.attr("src")?.replace("large", "normal")?:""
                memberModel.username = username
                memberModel.avatar_normal = avatarLarge

//            FROM_HOME &nbsp;•&nbsp; <strong><a href="/member/nodwang">nodwang</a></strong> &nbsp;•&nbsp; 2 分钟前 &nbsp;•&nbsp; 最后回复来自
//             FROM_NODE   &nbsp;•&nbsp; 6 小时 15 分钟前 &nbsp;•&nbsp; 最后回复来自
                val created = when (source) {
                    FROM_HOME, FROM_MEMBER -> {
                        val smallItem = item.getElementsByClass("topic_info")?.first()?.ownText()?:""
                        when {
                            smallItem.contains("最后回复") -> TimeUtil.toUtcTime(smallItem.split("•")[2])
                            else -> -1L
                        }
                    }
                    FROM_NODE -> {
                        val smallItem = item.getElementsByClass("topic_info")?.first()?.ownText()?:""
                        when {
                            smallItem.contains("最后回复") -> TimeUtil.toUtcTime(smallItem.split("•")[1])
                            else -> -1L
                        }
                    }
                    else -> 0L
                }
                topicModel.node = nodeModel
                topicModel.replies = replies
                topicModel.content = ""
                topicModel.content_rendered = ""
                topicModel.title = title
                topicModel.member = memberModel
                topicModel.id = id
                topicModel.created = created
                topics.add(topicModel)
            }
        }
        return topics
    }


    fun getAllNode(): MutableList<Node> {
        val allNodes = mutableListOf<Node>()
        val body = doc.body()
        val main = body.getElementsByAttributeValue("id", "Main").getOrNull(0)
        val boxes: Elements? = main?.getElementsByClass("box")

        boxes?.filterIndexed { index, _ -> index > 0 }?.forEach {
            val title = it.getElementsByClass("header")?.first()?.ownText()?:""
            val nodeElements = it.getElementsByClass("inner")?.first()?.getElementsByClass("item_node")
            if (nodeElements != null) {
                for (item in nodeElements) {
                    val node = Node()
                    val name = item.attr("href").replace("/go/", "")
                    node.name = name
                    node.title = item.text()
                    node.category = title
                    allNodes.add(node)
                }
            }
        }
        return allNodes
    }


    fun getMember(): Member {
        val rightbar = doc.getElementById("Rightbar")
        val memberElement = rightbar?.getElementsByTag("a")?.first()
        val username = memberElement?.attr("href")?.replace("/member/", "")?:""
        val avatarUrl = memberElement?.getElementsByClass("avatar")?.first()?.attr("src")?:""
        val memberModel = Member()
        memberModel.username = username
        memberModel.avatar_normal = avatarUrl
        return memberModel
    }


    fun getTotalPageInMember() = Regex("(?<=全部回复第\\s\\d\\s页 / 共 )\\d+").find(htmlStr)?.value?.toInt() ?: 0


    fun getOneNode(): Node {

        val nodeModel = Node()
        //        Document html = Jsoup.parse(response);
        val body = doc.body()
        val header = body.getElementsByClass("node_header").first()
        val contentElement = header.getElementsByClass("node_info").first().getElementsByTag("span").last()
        val content = contentElement.text()
        val number = header.getElementsByTag("strong").first().text()
        val strTitle = header.getElementsByClass("node_info").first().ownText().trim()

        if (header.getElementsByTag("img").first() != null) {
            val avatarLarge = header.getElementsByTag("img").first().attr("src")
            nodeModel.avatar_normal = avatarLarge.replace("large", "normal")
        }

        val elements = doc.head().getElementsByTag("script")
        val script = elements.last()
        //注意，script 的tag 不含 text。
        val strScript = script.html()
        val nodeName = strScript.split("\"".toRegex())[1]

        nodeModel.name = nodeName
        nodeModel.title = strTitle
        nodeModel.topics = Integer.parseInt(number)
        nodeModel.header = content

        return nodeModel
    }


    fun isNodeFollowed() = Regex("unfavorite/node/\\d{1,8}\\?once=").containsMatchIn(htmlStr)
    //        /favorite/node/557?once=46345
    fun getOnce(): String? = Regex("favorite/node/\\d{1,8}\\?once=\\d{1,10}").find(htmlStr)?.value

    fun getOnceNum() = doc.getElementsByAttributeValue("name", "once").first()?.attr("value") ?: "0"
    fun getOnceNum2() = Regex("(?<=<input type=\"hidden\" name=\"once\" value=\")(\\d+)").find(htmlStr)?.value


    fun isTopicFavored(): Boolean {
        val p = Pattern.compile("un(?=favorite/topic/\\d{1,10}\\?t=)")
        val matcher = p.matcher(doc.outerHtml())
        return matcher.find()
    }

    fun isTopicThanked(): Boolean {
        val p = Pattern.compile("thankTopic\\(\\d{1,10}")
        val matcher = p.matcher(doc.outerHtml())
        return !matcher.find()
    }

    fun getPageValue(): IntArray {

        val currentPage: Int
        val totalPage: Int
        val pageInput = doc.getElementsByClass("page_input").first() ?: return intArrayOf(-1, -1)
        currentPage = (pageInput.attr("value")).toIntOrNull()?:-1
        totalPage = (pageInput.attr("max")).toIntOrNull()?:-1
        return intArrayOf(currentPage, totalPage)

    }


    fun parseToNode(): ArrayList<Node> {

        val nodeModels = ArrayList<Node>()
        val items = doc.getElementsByClass("grid_item")

        for (item in items) {
            val nodeModel = Node()
            val id = item.attr("id").substring(2)
            nodeModel.id = id

            val title = item.getElementsByTag("div").first().ownText().trim { it <= ' ' }
            nodeModel.title = title
            val name = item.attr("href").replace("/go/", "")
            nodeModel.name = name

            val num = item.getElementsByTag("span").first().ownText().trim { it <= ' ' }

            nodeModel.topics = Integer.parseInt(num)

            val imageUrl = item.getElementsByTag("img").first().attr("src")
            nodeModel.avatar_normal = imageUrl
            nodeModels.add(nodeModel)
        }

        return nodeModels
    }

    fun getReplies(): ArrayList<Reply> {

        val replyModels = ArrayList<Reply>()

        val items = doc.getElementsByAttributeValueStarting("id", "r_")
        for (item in items) {

            //            <div id="r_4157549" class="cell">
            val id = item.id().substring(2)

            val replyModel = Reply()
            val memberModel = Member()
            val avatar = item.getElementsByClass("avatar").attr("src")
            val username = item.getElementsByTag("strong").first().getElementsByAttributeValueStarting("href", "/member/").first().text()

            memberModel.avatar_normal = avatar
            memberModel.username = username

            val thanks = when (val thanksOriginal = item.getElementsByClass("small fade")?.text()?:"") {
                "" -> 0
                else -> (thanksOriginal.replace("♥ ", "").trim()).toIntOrNull()?:0
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

    fun getVerifyCode(): String? {

        // <a href="/favorite/topic/349111?t=eghsuwetutngpadqplmlnmbndvkycaft" class="tb">加入收藏</a>
        val element: Element = doc.getElementsByClass("topic_buttons")?.first()?.getElementsByTag("a")?.first()
                ?: return null
        val p = Pattern.compile("(?<=favorite/topic/\\d{1,10}\\?t=)\\w+")
        val matcher = p.matcher(element.outerHtml())
        return if (matcher.find()) {
            matcher.group()
        } else
            null
    }

    fun parseResponseToTopic(topicId: String): Topic {
        val topicModel = Topic(topicId)

        val title = doc.getElementsByTag("h1").first().text()
        val contentElementOrg = doc.getElementsByClass("topic_content").first()

        val commentsEle = doc.getElementsByClass("subtle")
        val comments = commentsEle.map {
            Comment().apply {
                this.title = it.getElementsByClass("fade").text().split("·")[0].trim()
                created = TimeUtil.toUtcTime(it.getElementsByClass("fade").text().split("·")[1].trim())
                content = it.getElementsByClass("topic_content").html()
            }
        }

        topicModel.comments = comments.toMutableList()


        val preElems = contentElementOrg?.select("pre")
        if (preElems != null) {
            for (elem in preElems) {
                elem.html(elem.html().replace("\n", "<br/>")/*.replace(" ", "&nbsp;")*/)
            }
        }

        val content = contentElementOrg?.text() ?: ""
        topicModel.content = content
        val contentRendered = contentElementOrg?.html() ?: ""

        topicModel.content_rendered = contentRendered.fullUrl()

        val headerTopic = doc.getElementById("Main").getElementsByClass("header").first()
        val createdUnformed = headerTopic.getElementsByClass("gray").first().ownText() // · 44 分钟前用 iPhone 发布 · 192 次点击 &nbsp;

        val time = createdUnformed.split("·")[1]
        val created = TimeUtil.toUtcTime(time)

        var replyNum = ""
        val grays = doc.getElementsByClass("gray")
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
            hasReply -> Integer.parseInt(replyNum)
            else -> 0
        }


        val member = Member()
        val username = headerTopic
                .getElementsByAttributeValueStarting("href", "/member/").text()
        member.username = username
        val largeAvatar = headerTopic
                .getElementsByClass("avatar").attr("src")
        member.avatar_normal = largeAvatar
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

    fun getUserReplies(): List<MemberReplyModel> {
        val list = mutableListOf<MemberReplyModel>()

        val elements = doc.getElementsByAttributeValue("id", "Main")?.first()?.getElementsByClass("box")?.first()
        elements?.let {

            for (e in elements.getElementsByClass("dock_area")) {
                val model = MemberReplyModel()
                val titleElement = e.getElementsByAttributeValueContaining("href", "/t/").first()
                val title = titleElement.text()
                val fakeId = titleElement.attr("href").removePrefix("/t/")
                val create = e.getElementsByClass("fade").first().ownText()
                model.topic.title = title
                model.topic.id = fakeId.split("#")[0]
                model.create = TimeUtil.toUtcTime(create)
                val contentElement: Element? = e.nextElementSibling()
                val content = contentElement?.getElementsByClass("reply_content")?.first()
                model.content = content?.html()
                list.add(model)
            }
        }
        return list
    }

    //        <input type="number" class="page_input" autocomplete="off" value="1" min="1" max="8"
    fun getTotalPageForTopics() = Regex("(?<=max=\")\\d{1,8}").find(htmlStr)?.value?.toInt() ?: 0


    fun getErrorMsg(): String {
        logd(htmlStr)
        val message = doc.getElementsByClass("problem") ?: return "未知错误"
        return message.text().trim()
    }


    fun parseDailyOnce(): String? {

        val onceElement = doc.getElementsByAttributeValue("value", "领取 X 铜币").first()
                ?: return null
//        location.href = '/mission/daily/redeem?once=83270';
        val onceOriginal = onceElement.attr("onClick")
        return onceOriginal.getNum()
    }


    fun parseToNotifications(): List<NotificationModel> {
        val body = doc.body()
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
      FROM_HOME, FROM_NODE, FROM_MEMBER, FROM_SEARCH
    }

}