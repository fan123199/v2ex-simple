package im.fdx.v2ex.network

import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.network.Parser.Source.*
import im.fdx.v2ex.ui.topic.Reply
import im.fdx.v2ex.ui.main.Comment
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.member.Member
import im.fdx.v2ex.ui.member.MemberReplyModel
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.decodeEmail
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
          FROM_HOME, FROM_MEMBER, FROM_FAVOR -> body.getElementsByClass("cell item")
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
                    FROM_HOME, FROM_MEMBER, FROM_FAVOR -> {
                        //  <a class="node" href="/go/career">职场话题</a>
                        val nodeTitle = item.getElementsByClass("node")?.text()
                        val nodeName = item.getElementsByClass("node")?.attr("href")?.substring(4)
                        nodeModel.title = nodeTitle?:""
                        nodeModel.name = nodeName?:""

                    }
                    //            <a href="/member/wineway">
                    // <img src="//v2" class="avatar" ></a>
                    FROM_NODE -> {
                        val strHeader = body.getElementsByClass("node-header")?.first()?.text()?:""
                        var nodeTitle = ""
                        if (strHeader.contains("›")) {
                            nodeTitle = strHeader.split("›".toRegex())[1].split(" ".toRegex())[1].trim()
                        }

                        val elements = doc.head().getElementsByTag("script")
                        val script = elements.last()
                        //注意，script 的tag 不含 text。
                        val strScript = script?.html()
                        val nodeName = strScript?.split("\"".toRegex())?.get(1)?:""
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

                val avatarLarge = item.getElementsByClass("avatar")?.attr("src")?.replace("xxxlarge", "normal")?.replace("xxlarge", "normal")?:""
                memberModel.username = username
                memberModel.avatar_normal = avatarLarge

//            FROM_HOME &nbsp;•&nbsp; <strong><a href="/member/nodwang">nodwang</a></strong> &nbsp;•&nbsp; 2 分钟前 &nbsp;•&nbsp; 最后回复来自
//             FROM_NODE   &nbsp;•&nbsp; 6 小时 15 分钟前 &nbsp;•&nbsp; 最后回复来自
                var created: Long = 0L
                var createdOriginal:String = ""
                when (source) {
                    FROM_HOME, FROM_MEMBER, FROM_NODE, FROM_FAVOR -> {
                        val smallItem = item.getElementsByClass("topic_info")?.first()?.getElementsByAttribute("title")
                        if (smallItem !=null) {
                            created = TimeUtil.toUtcTime2(smallItem.attr("title"))
                            createdOriginal = smallItem.text()
                        }
                    }
                    else -> created = 0L
                }
                topicModel.node = nodeModel
                topicModel.replies = replies
                topicModel.content = ""
                topicModel.content_rendered = ""
                topicModel.title = title
                topicModel.member = memberModel
                topicModel.id = id
                topicModel.created = created
                topicModel.createdOriginal = createdOriginal.removeVia()
                topics.add(topicModel)
            }
        }
        return topics
    }


    fun getAllNode(): MutableList<Node> {
        val allNodes = mutableListOf<Node>()
        val body = doc.body()
        val boxes: Elements? = body.getElementsByClass("box")

        boxes?.filterIndexed { index, _ -> index > 0 }?.forEach {
            val title = it.getElementsByClass("header").first()?.ownText()?:""
            val nodeElements = it.getElementsByClass("inner").first()?.getElementsByClass("item_node")
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
        val rightbar = doc.getElementById("Rightbar")?.getElementsByClass("cell")?.first()
        val avatarUrl = rightbar?.getElementsByTag("img")?.first()?.attr("src")?:""
        val username = rightbar?.getElementsByTag("img")?.first()?.attr("alt")?:""
        val memberModel = Member()
        memberModel.username = username
        memberModel.avatar_normal = avatarUrl
        return memberModel
    }


    fun getTotalPageInMember() = Regex("(?<=全部回复第\\s\\d\\s页 / 共 )\\d+").find(htmlStr)?.value?.toInt() ?: 0


    fun getNodeInfo(nodeName: String): Node {

        val nodeModel = Node()
        //        Document html = Jsoup.parse(response);
        val body = doc.body()
        val header = body.getElementsByClass("node-header").first()
        if (header?.getElementsByTag("img")?.first() != null) {
            val avatarLarge = header.getElementsByTag("img").first()?.attr("src")
            nodeModel.avatar_normal = avatarLarge?.replace("xxxlarge", "normal")?.replace("xxlarge", "normal")
        }
        val number = header?.getElementsByTag("strong")?.first()?.text()
        val content = header?.getElementsByClass("intro")?.first()?.text()?:""
        val strTitle = header?.getElementsByClass("node-breadcrumb")?.first()?.ownText()?.trim()?:""
        nodeModel.name = nodeName
        nodeModel.title = strTitle
        nodeModel.topics = number?.toIntOrNull()?:0
        nodeModel.header = content

        return nodeModel
    }


    fun isNodeFollowed() = Regex("unfavorite/node/\\d{1,8}\\?once=").containsMatchIn(htmlStr)
    //        /favorite/node/557?once=46345
    fun getOnce(): String? = Regex("favorite/node/\\d{1,8}\\?once=\\d{1,10}").find(htmlStr)?.value

    fun getOnceNum() = doc.getElementsByAttributeValue("name", "once").first()?.attr("value") ?: "0"
    fun getOnceNum2() = Regex("(?<=<input type=\"hidden\" name=\"once\" value=\")(\\d+)").find(htmlStr)?.value


    fun isTopicFavored(): Boolean {
        val p = Pattern.compile("un(?=favorite/topic/\\d{1,10}\\?once=)")
        val matcher = p.matcher(htmlStr)
        return matcher.find()
    }

    fun isTopicThanked(): Boolean {
        val p = Pattern.compile("thankTopic\\(\\d{1,10}")
        val matcher = p.matcher(htmlStr)
        return !matcher.find()
    }

    fun isIgnored(): Boolean {
        val p = Pattern.compile("un(?=ignore/topic/\\d{1,10})")
        val matcher = p.matcher(htmlStr)
        return matcher.find()
    }

    fun getPageValue(): IntArray {
        //注释的是移动端的方法
//        val currentPage: Int = doc.getElementsByClass("page_current")?.first()?.ownText()?.toIntOrNull()?: -1
//        val totalPage: Int = doc.getElementsByClass("page_normal")?.first()?.ownText()?.toIntOrNull()?:-1
        val currentPage: Int
        val totalPage: Int
        val pageInput = doc.getElementsByClass("page_input").first() ?: return intArrayOf(-1, -1)
        currentPage = (pageInput.attr("value")).toIntOrNull()?:-1
        totalPage = (pageInput.attr("max")).toIntOrNull()?:-1
        return intArrayOf(currentPage, totalPage)

    }


    fun parseToNode(): ArrayList<Node> {

        val nodeModels = ArrayList<Node>()
        val items = doc.getElementById("my-nodes")?.children() ?: return nodeModels
        for (item in items) {
            val nodeModel = Node()
            val id = item.attr("id").substring(2)
            nodeModel.id = id

            val title = item.getElementsByClass("fav-node-name").first()?.ownText()?.trim()?:""
            nodeModel.title = title
            val name = item.attr("href").replace("/go/", "")
            nodeModel.name = name

            val num = item.getElementsByTag("span")[1].ownText().trim()

            nodeModel.topics = Integer.parseInt(num)

            val imageUrl = item.getElementsByTag("img").first()?.attr("src")?:""
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
            val username = item.getElementsByTag("strong").first()?.getElementsByAttributeValueStarting("href", "/member/")?.first()?.text()?:""

            memberModel.avatar_normal = avatar
            memberModel.username = username
            val thanksOriginal = item.getElementsByClass("small fade")?.first()?.ownText()?:""
            val thanks = thanksOriginal.trim().toIntOrNull()?:0
            val thanked = item.getElementsByClass("thank_area thanked").first()
            replyModel.isThanked = thanked != null && "感谢已发送" == thanked.text()

            val createdOriginal = item.getElementsByClass("ago").text()
            val createUtcStr = item.getElementsByClass("ago").attr("title")
            val replyContent = item.getElementsByClass("reply_content").first()


            //<a href="/cdn-cgi/l/email-protection#b1ded7d7d8d2d49fd2d9d8c5d0dfd4c3f1d6dcd0d8dd9fd2dedc"><span class="__cf_email__"
// data-cfemail="a8c7cecec1cbcd86cbc0c1dcc9c6cddae8cfc5c9c1c486cbc7c5">[email&#160;protected]</span></a>

//<a href="mailto:office.chitaner@gmail.com">office.chitaner@gmail.com</a>
            val cfemails = replyContent?.getElementsByAttribute("data-cfemail")
            if(cfemails != null) {
                for (cfemail in cfemails) {
                    val p = cfemail.parent()
                    val secret = cfemail.attr("data-cfemail")

                    p?.attr("href" , secret.decodeEmail())
                    p?.child(0)?.remove()
                    p?.append(secret.decodeEmail())
                }
            }

            replyModel.created = TimeUtil.toUtcTime2(createUtcStr)
            replyModel.createdOriginal = createdOriginal.removeVia()
            replyModel.member = memberModel
            replyModel.thanks = thanks


            val i: Int = item.getElementsByClass("no").first()?.text()?.toIntOrNull()?: -1
            replyModel.setRowNum(i)

            replyModel.id = id
            replyModel.content = replyContent?.text() ?: ""
            replyModel.content_rendered = replyContent?.html()?.fullUrl()?:""
            replyModels.add(replyModel)
        }
        return replyModels

    }

    fun parseResponseToTopic(topicId: String): Topic {
        val topicModel = Topic(topicId)

        val title = doc.getElementsByTag("h1").first()?.text()?:""
        val contentElementOrg = doc.getElementsByClass("topic_content").first()

        val commentsEle = doc.getElementsByClass("subtle")
        val comments = commentsEle.map {
            Comment().apply {
                this.title = it.getElementsByClass("fade").text().split("·")[0].trim()
                val elementsByAttribute = it.getElementsByClass("fade").first()?.getElementsByAttribute("title")
                createdOriginal = elementsByAttribute?.text()?:""
                created = TimeUtil.toUtcTime2(elementsByAttribute?.attr("title"))
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

        val cfemails = contentElementOrg?.getElementsByAttribute("data-cfemail")
        if(cfemails != null) {
            for (cfemail in cfemails) {
                val p = cfemail.parent()
                val secret = cfemail.attr("data-cfemail")

                p?.attr("href" , "mailto:" + secret.decodeEmail())
                p?.child(0)?.remove()
                p?.append(secret.decodeEmail())
            }
        }
        val contentRendered = contentElementOrg?.html() ?: ""
        topicModel.content_rendered = contentRendered.fullUrl()

        val headerTopic = doc.getElementById("Main")?.getElementsByClass("header")?.first()
        val elementsByClass = headerTopic?.getElementsByClass("gray")
        val createdUnformed = elementsByClass?.first()?.getElementsByTag("span")?.first() // · 44 分钟前用 iPhone 发布 · 192 次点击 &nbsp;

        // · 54 天前 · 36030 次点击
        // · 9 小时 6 分钟前 via Android · 1036 次点击
        val createdOriginal = createdUnformed?.text()?:""
        val created = TimeUtil.toUtcTime2(createdUnformed?.attr("title"))
        topicModel.createdOriginal = createdOriginal.removeVia()

        var replyNum = ""
        val grays = doc.getElementsByClass("gray")
        var hasReply = false
        for (gray in grays) {
            if (gray.text().contains("条回复")) {
                val wholeText = gray.text()
                val index = wholeText.indexOf("条回复")
                replyNum = wholeText.substring(0, index - 1).trim()
                if (replyNum.isNotEmpty()) {
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
                ?.getElementsByAttributeValueStarting("href", "/member/")?.text()?:""
        member.username = username
        val largeAvatar = headerTopic
                ?.getElementsByClass("avatar")?.attr("src")?:""
        member.avatar_normal = largeAvatar
        val nodeElement = headerTopic
                ?.getElementsByAttributeValueStarting("href", "/go/")?.first()
        val nodeName = nodeElement?.attr("href")?.replace("/go/", "")?:""
        val nodeTitle = nodeElement?.text()?:""
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
                val title = titleElement?.text()?:""
                val fakeId = titleElement?.attr("href")?.removePrefix("/t/")
                val create = e.getElementsByClass("fade").first()?.ownText()
                model.topic.title = title
                model.topic.id = fakeId?.split("#")?.get(0) ?: ""
                model.create = TimeUtil.toUtcTime(create)
                model.createdOriginal = create?:""
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

            val time = item.getElementsByClass("snow").first()?.text()?:""
            notification.time = time// 2/6


            //member model
            val memberElement = item.getElementsByTag("a").first()
            val username = memberElement?.attr("href")?.replace("/member/", "")?:""
            val avatarUrl = memberElement?.getElementsByClass("avatar")?.first()?.attr("src")?:""
            val memberModel = Member()
            memberModel.username = username
            memberModel.avatar_normal = avatarUrl
            notification.member = memberModel // 3/6

            val topicModel = Topic()

            val topicElement = item.getElementsByClass("fade").first()
            //            <a href="/t/348757#reply1">交互式《线性代数》学习资料</a>
            val href = topicElement?.getElementsByAttributeValueStarting("href", "/t/")?.first()?.attr("href")

            topicModel.title = topicElement?.getElementsByAttributeValueStarting("href", "/t/")?.first()?.text()?:""

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

            val originalType = topicElement?.ownText()?:""
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
      FROM_HOME, FROM_NODE, FROM_MEMBER, FROM_SEARCH, FROM_FAVOR
    }

}

// 1小时 23 分钟前  via iphone
private fun String.removeVia(): String {

    return if (this.contains("via")) {
        this.substring(0, this.indexOf("via")).trim()
    } else {
        this
    }

}
