package im.fdx.v2ex.ui.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.elvishew.xlog.XLog
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.main.MyDiffCallback
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.ui.member.MemberActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.getPair
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.logd
import im.fdx.v2ex.view.GoodTextView
import im.fdx.v2ex.view.Popup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_reply_view.*
import okhttp3.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.IOException

/**
 * Created by fdx on 15-9-7.
 * 详情页的Adapter。
 * todo 把 allList 分离
 */
class DetailsAdapter(private val mContext: Context,
                     private val callback: DetailsAdapter.AdapterCallback,
                     val mAllList: MutableList<BaseModel> = mutableListOf())
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var verifyCode: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_HEADER -> TopicWithCommentsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_topic_with_comments, parent, false))
                TYPE_ITEM -> ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_reply_view, parent, false))
                TYPE_FOOTER -> FooterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_load_more, parent, false))
                else -> throw RuntimeException(" No type that matches $viewType + Make sure using types correctly")
            }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_HEADER -> {

                val mainHolder = holder as TopicWithCommentsViewHolder
                val topic = mAllList[position] as Topic
                mainHolder.tvTitle.text = topic.title
                mainHolder.tvTitle.maxLines = 4
                mainHolder.tvContent.isSelected = true
                mainHolder.tvContent.setGoodText(topic.content_rendered)
                logd(topic.content_rendered)
                //            Log.i(TAG, topic.getContent());
                //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //                mainHolder.tvContent.setTransitionName("header");
                //            }

                mainHolder.tvReplyNumber.text = topic.replies.toString()
                mainHolder.tvAuthor.text = topic.member?.username
                mainHolder.tvNode.text = topic.node?.title
                mainHolder.tvCreated.text = TimeUtil.getRelativeTime(topic.created)
                mainHolder.ivAvatar.load(topic.member?.avatarNormalUrl)

                if (topic.comments.isNotEmpty()) {
                    mainHolder.ll.removeAllViews()
                    topic.comments.forEach {
                        val view = LayoutInflater.from(mContext).inflate(R.layout.item_comments, mainHolder.ll, false)
                        val th = CommentsViewHolder(view)
                        th.tvCTitle.text = it.title
                        th.tvCTime.text = TimeUtil.getRelativeTime(it.created)
                        th.tvCContent.setGoodText(it.content)
                        mainHolder.ll.addView(view)
                    }
                }


                val l = TopicsRVAdapter.MyOnClickListener(mContext, topic)
                mainHolder.tvNode.setOnClickListener(l)
                mainHolder.ivAvatar.setOnClickListener(l)

            }
            TYPE_FOOTER -> {
                val tvMore = (holder as FooterViewHolder).tvLoadMore
                tvMore.text = "加载更多"
                tvMore.setOnClickListener { callback.onMethodCallback(2, -1) }
            }
            TYPE_ITEM -> {
                val itemVH = holder as ItemViewHolder
                val replyItem = mAllList[position] as ReplyModel
                replyItem.isLouzu = replyItem.member?.username == (mAllList[0] as Topic).member?.username
                if (position == itemCount - 1) {
                    itemVH.divider.visibility = View.GONE
                }

                if (MyApp.get().isLogin()) {
                    (mContext as Activity).registerForContextMenu(itemVH.itemView)

                    itemVH.itemView.setOnCreateContextMenuListener { menu, _, _ ->
                        val menuInflater = mContext.menuInflater
                        menuInflater.inflate(R.menu.menu_reply, menu)

                        val menuListener = MenuItem.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.menu_reply -> reply(replyItem, position)
                                R.id.menu_thank -> thank(replyItem, itemVH)
                                R.id.menu_copy -> copyText(replyItem)
                            }
                            false
                        }

                        menu.findItem(R.id.menu_reply).setOnMenuItemClickListener(menuListener)
                        menu.findItem(R.id.menu_thank).setOnMenuItemClickListener(menuListener)
                        menu.findItem(R.id.menu_copy).setOnMenuItemClickListener(menuListener)
                    }
                    itemVH.iv_thanks.setOnClickListener { thank(replyItem, itemVH) }
                    itemVH.tv_thanks.setOnClickListener { thank(replyItem, itemVH) }
                    itemVH.iv_reply.setOnClickListener { reply(replyItem, position) }
                }

                XLog.i(replyItem.content_rendered)
                itemVH.tv_reply_time.text = TimeUtil.getRelativeTime(replyItem.created)
                itemVH.tv_replier.text = replyItem.member?.username
                itemVH.tv_thanks.text = replyItem.thanks.toString()
                itemVH.tv_reply_content.setGoodText(replyItem.content_rendered)
                itemVH.tv_reply_row.text = "#$position"
                itemVH.iv_reply_avatar.load(replyItem.member?.avatarLargeUrl)
                itemVH.iv_reply_avatar.setOnClickListener {
                    mContext.startActivity<MemberActivity>(Keys.KEY_USERNAME to replyItem.member!!.username)
                }
                if (replyItem.member?.username == (mAllList[0] as Topic).member?.username) {
                    itemVH.tv_louzu.visibility = View.VISIBLE
                } else {
                    itemVH.tv_louzu.visibility = View.GONE
                }

                if (replyItem.isThanked) {
                    itemVH.iv_thanks.imageTintList = ContextCompat.getColorStateList(mContext, R.color.primary)
                    itemVH.iv_thanks.isClickable = false
                    itemVH.tv_thanks.isClickable = false
                } else {
                    itemVH.iv_thanks.imageTintList = null
                }

                itemVH.tv_reply_content.popupListener = object : Popup.PopupListener {
                    override fun onClick(v: View, url: String) {
                        val username = url.split("/").last()
                        var index = replyItem.content.getPair(username)
                        if (index <= 0 || index > position) { //5
                            mAllList.forEachIndexed { i, baseModel ->
                                if (i in 1 until position && (baseModel as ReplyModel).member?.username == username) {
                                    index = i
                                }
                            }
                        }
                        if (index <= 0 || index > position) {
                            return
                        }
                        Popup(mContext).show(v, mAllList[index] as ReplyModel, index, View.OnClickListener {
                            callback.onMethodCallback(-1, index)
                        })
                    }

                }
            }
        }
    }

    private fun copyText(replyItem: ReplyModel) {
        XLog.d("I click menu copy")
        val manager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = ClipData.newPlainText("item", replyItem.content)
    }

    private fun thank(replyItem: ReplyModel, itemVH: ItemViewHolder): Boolean {
        XLog.tag(TAG).d("hehe" + verifyCode)
        if (verifyCode == null) {
            return true
        }
        val body = FormBody.Builder().add("t", verifyCode!!).build()

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .url("https://www.v2ex.com/thank/reply/${replyItem.id}")
                .post(body)
                .build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetManager.dealError(mContext)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                if (response.code() == 200) {
                    (mContext as Activity).runOnUiThread {
                        mContext.toast("感谢成功")
                        replyItem.thanks = replyItem.thanks + 1
                        itemVH.tv_thanks.text = (replyItem.thanks).toString()
                        itemVH.tv_thanks.isClickable = false
                        itemVH.iv_thanks.imageTintList = ContextCompat.getColorStateList(mContext, R.color.primary)
                        itemVH.iv_thanks.isClickable = false
                        replyItem.isThanked = true
                    }
                } else {
                    NetManager.dealError(mContext, response.code())
                }
            }
        })
        return false
    }

    private fun reply(replyItem: ReplyModel, position: Int) {
        val editText: EditText = (mContext as Activity).findViewById(R.id.et_post_reply)
        val text = "@${replyItem.member!!.username} " +
                if (MyApp.get().mPrefs.getBoolean("pref_add_row", false)) "#$position " else ""
        if (!editText.text.toString().contains(text)) {
            val spanString = SpannableString(text)
            val span = ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primary))
            spanString.setSpan(span, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            editText.append(spanString)
        }
        editText.setSelection(editText.length())
        editText.requestFocus()
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun getItemCount() = mAllList.size + 1

    override fun getItemViewType(position: Int) = when {
        position == 0 && itemCount > 1 -> TYPE_HEADER
        position == itemCount - 1 -> TYPE_FOOTER
        else -> TYPE_ITEM
    }

    fun updateItems(newItems: List<BaseModel>) {
        if (mAllList.isEmpty()) {
            mAllList.addAll(newItems)
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mAllList, newItems))
            mAllList.clear()
            mAllList.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun addItems(newItems: List<BaseModel>) {
        val old = mAllList.toList()
        mAllList.addAll(newItems)
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(old, mAllList))
        diffResult.dispatchUpdatesTo(this)
    }

    //我重用了MainAdapter中的MainViewHolder

    class ItemViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    }

    class TopicWithCommentsViewHolder(itemView: View) : TopicsRVAdapter.MainViewHolder(itemView) {
        internal var ll: LinearLayout = itemView.findViewById(R.id.ll_comments)
    }

    class CommentsViewHolder(itemView: View) {
        internal var tvCTitle: TextView = itemView.findViewById(R.id.tv_comment_id)
        internal var tvCTime: TextView = itemView.findViewById(R.id.tv_comment_time)
        internal var tvCContent: GoodTextView = itemView.findViewById(R.id.tv_comment_content)
    }


    private class FooterViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvLoadMore: TextView = itemView.findViewById(R.id.tv_load_more)
    }

    interface AdapterCallback {
        fun onMethodCallback(type: Int, position: Int)
    }

    companion object {
        private val TAG = DetailsAdapter::class.java.simpleName
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }
}
