package im.fdx.v2ex.ui.topic

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.databinding.ItemReplyViewBinding
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.pref
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.ui.node.NodeActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.getRowNum
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.utils.extensions.showLoginHint
import im.fdx.v2ex.view.GoodTextView
import im.fdx.v2ex.view.Popup
import im.fdx.v2ex.view.typeComment
import im.fdx.v2ex.view.typeReply
import okhttp3.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.IOException


private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

/**
 * Created by fdx on 15-9-7.
 * 详情页的Adapter。
 */
class TopicAdapter(private val act: FragmentActivity,
                   private val clickMore: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var once: String? = null
    val topics: MutableList<Topic> = MutableList(1) { Topic() }
    val replies: MutableList<Reply> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_HEADER -> TopicWithCommentsViewHolder(LayoutInflater.from(act).inflate(R.layout.item_topic_with_comments, parent, false))
                TYPE_ITEM -> ItemViewHolder(ItemReplyViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                else -> throw RuntimeException(" No type that matches $viewType + Make sure using types correctly")
            }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, p: Int) {
        val position = holder.bindingAdapterPosition
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                val topic = topics[0]
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
                        th.tvCContent.setGoodText(it.content, type = typeComment)
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
                    itemVH.binding.divider.visibility = View.INVISIBLE
                } else {
                    itemVH.binding.divider.visibility = View.VISIBLE
                }

                itemVH.bind(replyItem)

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

                    menu.findItem(R.id.menu_show_user_conversation).setOnMenuItemClickListener {
                        showUserConversation(replyItem)
                        true
                    }

                }

                itemVH.binding.ivThanks.setOnClickListener { thank(replyItem, itemVH) }
                itemVH.binding.tvThanks.setOnClickListener { thank(replyItem, itemVH) }
                itemVH.binding.ivReply.setOnClickListener { reply(replyItem, position) }


                itemVH.binding.ivReplyAvatar.setOnClickListener {
                    act.startActivity<MemberActivity>(Keys.KEY_USERNAME to replyItem.member!!.username)
                }

                if (replyItem.isThanked) {
                    itemVH.binding.ivThanks.imageTintList = ContextCompat.getColorStateList(act, R.color.primary)
                    itemVH.binding.ivThanks.isClickable = false
                    itemVH.binding.tvThanks.isClickable = false
                } else {
                    itemVH.binding.ivThanks.imageTintList = null
                }

                itemVH.binding.tvReplyContent.popupListener = object : Popup.PopupListener {
                    override fun onClick(v: View, url: String) {
                        val username = url.split("/").last()

                        //问题，index可能用户输入不准确，导致了我的这个点击会出现错误。 也有可能是黑名单能影响，导致了
                        //了这个错误，所以，我需要进行大数据排错。
                        //rowNum 是真是的楼层数， 但是在数组的index = rowNum -1
                        var rowNum = replyItem.content.getRowNum(username)
                        //找不到，或大于，明显不可能，取最接近一个评论
                        if (rowNum == -1 || rowNum > position) {
                            replies.forEachIndexed { i, r ->
                                if (i in 0 until position && r.member?.username == username) {
                                    rowNum = i + 1
                                }
                            }
                        }
                        if (rowNum == -1 || rowNum > position) {
                            return
                        }
                        Popup(act).show(v, replies[rowNum -1], rowNum, clickMore)
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

    //todo
    private fun showUserConversation(replyItem: Reply) {

        val oneName = replyItem.member?.username?:""
        var theOtherName =  ""
        if (replyItem.content_rendered.contains("v2ex.com/member/")) {//说明有对话

            var find = """(?<=v2ex\.com/member/)\w+""".toRegex().find(replyItem.content_rendered)
            find?.let {
                theOtherName = it.value
            }

            val theUserReplyList = replies.filter {
                val username = it.member?.username
                (username == oneName && hasRelate(theOtherName, it)) //需要是和other相关的
                        || (username == theOtherName && hasRelate(oneName, it))  //需要是和本楼相关的
            }

            if (theUserReplyList.isEmpty()) {
                return
            }
            val bs = BottomListSheet.newInstance(theUserReplyList)
            bs.show(act.supportFragmentManager, "user_conversation")
        }
    }

    fun hasRelate(name:String, item: Reply) : Boolean{
        var find = """v2ex\.com/member/$name""".toRegex().find(item.content_rendered)
        find?.let {
            return true
        }
        return false
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
                        itemVH.binding.tvThanks.text = (replyItem.thanks).toString()
                        itemVH.binding.ivThanks.imageTintList = ContextCompat.getColorStateList(act, R.color.primary)
                        itemVH.binding.ivThanks.isClickable = false
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

    override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    fun updateItems(t: Topic, replies: List<Reply>) {
        this.topics.clear()
        this.topics.add(t)
        this.replies.clear()
        this.replies.addAll(replies)


        replies.forEachIndexed { index ,it ->
            it.isLouzu = it.member?.username == topics[0].member?.username
            it.showTime = TimeUtil.getRelativeTime(it.created)
            it.rowNum = index + 1
        }
        notifyDataSetChanged()
    }

    fun addItems(replies: List<Reply>) {
        replies.forEachIndexed { index, it ->
            it.isLouzu = it.member?.username == topics[0].member?.username
            it.showTime = TimeUtil.getRelativeTime(it.created)
            it.rowNum = index + 1
        }
        this.replies.addAll(replies)
        notifyDataSetChanged()
    }

}

class ItemViewHolder(var binding: ItemReplyViewBinding)
    : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("SetTextI18n")
    fun bind(data:Reply) {

        binding.tvReplyContent.setGoodText(data.content_rendered , type = typeReply)
        binding.tvLouzu.visibility = if (data.isLouzu) View.VISIBLE else View.GONE
        binding.tvReplyRow.text = "# ${data.rowNum}"
        binding.tvReplier.text = data.member?.username
        binding.tvThanks.text = data.thanks.toString()
        binding.ivReplyAvatar.load(data.member?.avatarNormalUrl)
        binding.tvReplyTime.text = data.showTime

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
