package im.fdx.v2ex.ui.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.*
import im.fdx.v2ex.view.GoodTextView
import im.fdx.v2ex.view.Popup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_reply_view.*
import okhttp3.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.IOException



private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

const val TYPE_GO_TO_ROW = -1

/**
 * Created by fdx on 15-9-7.
 * 详情页的Adapter。
 */
class TopicDetailAdapter(private val act: FragmentActivity,
                         private val clickMore: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var once: String? = null
    val topics: MutableList<Topic> = MutableList(1) { Topic() }
    val replies: MutableList<Reply> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_HEADER -> TopicWithCommentsViewHolder(LayoutInflater.from(act).inflate(R.layout.item_topic_with_comments, parent, false))
                TYPE_ITEM -> ItemViewHolder(LayoutInflater.from(act).inflate(R.layout.item_reply_view, parent, false))
                else -> throw RuntimeException(" No type that matches $viewType + Make sure using types correctly")
            }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                val topic = topics[0]
                Crashlytics.setString("topic_id", topic.id)
                logd(topic.title)
                logd(topic.content_rendered)
                val mainHolder = holder as TopicWithCommentsViewHolder
                mainHolder.tvTitle.text = topic.title
                mainHolder.tvTitle.maxLines = 4
                mainHolder.tvContent.setGoodText(topic.content_rendered)
                mainHolder.tvContent.isSelected = true
                mainHolder.tvContent.setTextIsSelectable(true)

                mainHolder.tvReplyNumber.text = topic.replies.toString()
                mainHolder.tvAuthor.text = topic.member?.username
                mainHolder.tvNode.text = topic.node?.title
                mainHolder.tvCreated.text = TimeUtil.getRelativeTime(topic.created)
                mainHolder.ivAvatar.load(topic.member?.avatarNormalUrl)

                if (topic.comments.isNotEmpty()) {
                    mainHolder.ll.removeAllViews()
                    mainHolder.dividerComments.isGone = false
                    topic.comments.forEach {
                        val view = LayoutInflater.from(act).inflate(R.layout.item_comments, mainHolder.ll, false)
                        val th = CommentsViewHolder(view)
                        th.tvCTitle.text = it.title
                        th.tvCTime.text = TimeUtil.getRelativeTime(it.created)
                        th.tvCContent.setGoodText(it.content, type = 2)
                        mainHolder.ll.addView(view)
                    }
                }

                mainHolder.tvNode.setOnClickListener{
                    act.startActivity<NodeActivity>(Keys.KEY_NODE_NAME to topic.node?.name!!)
                }
                mainHolder.ivAvatar.setOnClickListener{
                    act.startActivity<MemberActivity>(Keys.KEY_USERNAME to topic.member?.username!!)
                }

            }
            TYPE_ITEM -> {
                val itemVH = holder as ItemViewHolder
                val replyItem = replies[position - 1]
                if (position == itemCount - 1) {
                    itemVH.divider.visibility = View.INVISIBLE
                } else {
                    itemVH.divider.visibility = View.VISIBLE
                }


                act.registerForContextMenu(itemVH.itemView)

                itemVH.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                    val menuInflater = act.menuInflater
                    menuInflater.inflate(R.menu.menu_reply, menu)

                    menu.findItem(R.id.menu_reply).setOnMenuItemClickListener {
                        reply(replyItem, position)
                        true
                    }
                    menu.findItem(R.id.menu_thank).setOnMenuItemClickListener {
                        thank(replyItem, itemVH)
                        true
                    }
                    menu.findItem(R.id.menu_copy).setOnMenuItemClickListener {
                        copyText(replyItem.content)
                        true
                    }
                    menu.findItem(R.id.menu_show_user_all_reply).setOnMenuItemClickListener {
                        showUserAllReply(replyItem)
                        true
                    }

                }

                itemVH.iv_thanks.setOnClickListener { thank(replyItem, itemVH) }
                itemVH.tv_thanks.setOnClickListener { thank(replyItem, itemVH) }
                itemVH.iv_reply.setOnClickListener { reply(replyItem, position) }


                XLog.i(replyItem.content_rendered)

                itemVH.bind(replyItem)
                itemVH.tv_thanks.text = replyItem.thanks.toString()
                itemVH.iv_reply_avatar.setOnClickListener {
                    act.startActivity<MemberActivity>(Keys.KEY_USERNAME to replyItem.member!!.username)
                }

                if (replyItem.isThanked) {
                    itemVH.iv_thanks.imageTintList = ContextCompat.getColorStateList(act, R.color.primary)
                    itemVH.iv_thanks.isClickable = false
                    itemVH.tv_thanks.isClickable = false
                } else {
                    itemVH.iv_thanks.imageTintList = null
                }

                itemVH.tv_reply_content.popupListener = object : Popup.PopupListener {
                    override fun onClick(v: View, url: String) {
                        val username = url.split("/").last()

                        //问题，index可能用户输入不准确，导致了我的这个点击会出现错误。 也有可能是黑名单能影响，导致了
                        //了这个错误，所以，我需要进行大数据排错。
                        var index = replyItem.content.getPair(username)
                        //找不到，或大于，明显不可能，取最接近一个评论
                        if (index == -1 || index > position) {
                            replies.forEachIndexed { i, r ->
                                if (i in 0 until position && r.member?.username == username) {
                                    index = i
                                }
                            }
                        }
                        if (index == -1 || index > position) {
                            return
                        }

                        //自我优化的失败案例
                        if (replies[index].member!!.username != username) {
                            for(i in (index -2)..(index + 2)) {
                                if (replies[i].member!!.username == username ){
                                    index = i
                                    break
                                }
                            }
                        }

                        Popup(act).show(v, replies[index], index, clickMore)
                    }

                }
            }
        }
    }

    private fun showUserAllReply(replyItem: Reply) {

        val theUserReplyList = replies.filter {
            it.member!=null && it.member?.username == replyItem.member?.username
        }

        val bs = BottomListSheet.newInstance(theUserReplyList)
        bs.show(act.supportFragmentManager , "list_of_user_all_reply")
    }

    private fun copyText(content: String) {
        logd("I click menu copy")
        val manager = act.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip( ClipData.newPlainText("item", content))
        act.toast("评论已复制")
    }

    private fun thank(replyItem: Reply, itemVH: ItemViewHolder) {
        logd("once: $once")
        val editText: EditText = act.findViewById(R.id.et_post_reply)
        if (!MyApp.get().isLogin) {
            act.showLoginHint(editText)
            return
        }

        if (once == null) {
            act.toast("请刷新后重试")
            return
        }
        val body = FormBody.Builder().add("once", once!!).build()

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url("https://www.v2ex.com/thank/reply/${replyItem.id}")
                .post(body)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(act)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code == 200) {
                    act.runOnUiThread {
                        act.toast("感谢成功")
                        replyItem.thanks = replyItem.thanks + 1
                        itemVH.tv_thanks.text = (replyItem.thanks).toString()
                        itemVH.iv_thanks.imageTintList = ContextCompat.getColorStateList(act, R.color.primary)
                        itemVH.iv_thanks.isClickable = false
                        replyItem.isThanked = true
                    }
                } else {
                    NetManager.dealError(act, response.code)
                }
            }
        })
        return
    }

    private fun reply(replyItem: Reply, position: Int) {

        val editText: EditText = act.findViewById(R.id.et_post_reply)
        if (!MyApp.get().isLogin) {
            act.showLoginHint(editText)
            return
        }

        val text = "@${replyItem.member!!.username} " +
                if (pref.getBoolean("pref_add_row", false)) "#$position " else ""
        if (!editText.text.toString().contains(text)) {
            val spanString = SpannableString(text)
            val span = ForegroundColorSpan(ContextCompat.getColor(act, R.color.primary))
            spanString.setSpan(span, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            editText.append(spanString)
        }
        editText.setSelection(editText.length())
        editText.requestFocus()
        val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun getItemCount() = 1 + replies.size

    override fun getItemViewType(position: Int) = when {
        position == 0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    fun updateItems(t: Topic, replies: List<Reply>) {
        this.topics.clear()
        this.topics.add(t)
        this.replies.clear()
        this.replies.addAll(replies)
        replies.forEach {
            it.isLouzu = it.member?.username == topics[0].member?.username
        }
        notifyDataSetChanged()
    }

    fun addItems(replies: List<Reply>) {
        replies.forEach {
            it.isLouzu = it.member?.username == topics[0].member?.username
        }
        this.replies.addAll(replies)
        notifyDataSetChanged()
    }

}

class ItemViewHolder(override val containerView: View)
    : RecyclerView.ViewHolder(containerView), LayoutContainer {

    @SuppressLint("SetTextI18n")
    fun bind(data:Reply) {

        tv_reply_content.setGoodText(data.content_rendered , type = 3)
        tv_louzu.visibility = if (data.isLouzu) View.VISIBLE else View.GONE
        tv_reply_row.text = "#$adapterPosition"
        tv_replier.text = data.member?.username
        tv_thanks.text = data.thanks.toString()
        iv_reply_avatar.load(data.member?.avatarNormalUrl)
        tv_reply_time.text = TimeUtil.getRelativeTime(data.created)

    }

}

class TopicWithCommentsViewHolder(itemView: View)
    : TopicsRVAdapter.MainViewHolder(itemView) {
    internal var ll: LinearLayout = itemView.findViewById(R.id.ll_comments)
    internal val dividerComments :View = itemView.findViewById(R.id.divider_comment)
}

class CommentsViewHolder(itemView: View) {
    internal var tvCTitle: TextView = itemView.findViewById(R.id.tv_comment_id)
    internal var tvCTime: TextView = itemView.findViewById(R.id.tv_comment_time)
    internal var tvCContent: GoodTextView = itemView.findViewById(R.id.tv_comment_content)
}
