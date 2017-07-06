package im.fdx.v2ex.ui.details

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.elvishew.xlog.XLog
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.MyApp
import im.fdx.v2ex.R
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.network.HttpHelper
import im.fdx.v2ex.network.NetManager
import im.fdx.v2ex.ui.main.TopicModel
import im.fdx.v2ex.ui.main.TopicsRVAdapter
import im.fdx.v2ex.utils.TimeUtil
import im.fdx.v2ex.utils.extensions.load
import im.fdx.v2ex.utils.extensions.toast
import im.fdx.v2ex.view.GoodTextView
import okhttp3.*
import java.io.IOException

/**
 * Created by fdx on 15-9-7.
 * 详情页的Adapter。
 */
class DetailsAdapter(private val mContext: Context, private val mAllList: List<BaseModel>, private val callback: DetailsAdapter.AdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var verifyCode: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return TopicsRVAdapter.MainViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_topic_view, parent, false))
            TYPE_ITEM -> return ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_reply_view, parent, false))
            TYPE_FOOTER -> return FooterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_load_more, parent, false))
            else -> throw RuntimeException(" No type that matches $viewType + Make sure using types correctly")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_HEADER -> {

                val mainHolder = holder as TopicsRVAdapter.MainViewHolder
                val topic = mAllList[position] as TopicModel
                mainHolder.tvTitle.text = topic.title
                mainHolder.tvTitle.maxLines = 4
                mainHolder.tvContent.isSelected = true
                mainHolder.tvContent.setGoodText(topic.content_rendered)
                Log.i(TAG, topic.content_rendered)
                //            Log.i(TAG, topic.getContent());
                //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //                mainHolder.tvContent.setTransitionName("header");
                //            }

                mainHolder.tvReplyNumber.text = topic.replies.toString()
                mainHolder.tvAuthor.text = topic.member!!.username
                mainHolder.tvNode.text = topic.node!!.title
                mainHolder.tvCreated.text = TimeUtil.getRelativeTime(topic.created)
                mainHolder.ivAvatar.load(topic.member!!.avatarNormalUrl)

                val l = TopicsRVAdapter.MyOnClickListener(mContext, topic)
                mainHolder.tvNode.setOnClickListener(l)
                mainHolder.ivAvatar.setOnClickListener(l)

            }
            TYPE_FOOTER -> {
                val tvMore = (holder as FooterViewHolder).tvLoadMore
                tvMore.text = "加载更多"
                tvMore.setOnClickListener { callback.onMethodCallback(2) }
            }
            TYPE_ITEM -> {
                val itemVH = holder as ItemViewHolder
                val replyItem = mAllList[position] as ReplyModel
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
                    itemVH.tvReply.setOnClickListener { reply(replyItem, position) }
                }

                itemVH.tvReplyTime.text = TimeUtil.getRelativeTime(replyItem.created)
                itemVH.tvReplier.text = replyItem.member!!.username
                itemVH.tvThanks.text = replyItem.thanks.toString()
                itemVH.ivThank.setOnClickListener { thank(replyItem, itemVH) }
                itemVH.tvThanks.setOnClickListener { thank(replyItem, itemVH) }

                if (replyItem.isThanked) {
                    itemVH.ivThank.imageTintList = ContextCompat.getColorStateList(mContext, R.color.primary)
                    itemVH.ivThank.isClickable = false
                    itemVH.tvThanks.isClickable = false
                } else {
                    itemVH.ivThank.imageTintList = null
                }

                itemVH.tvContent.setGoodText(replyItem.content_rendered)

                XLog.i(replyItem.content_rendered)
                itemVH.tvRow.text = "#$position"
                itemVH.ivUserAvatar.load(replyItem.member!!.avatarLargeUrl)
                itemVH.ivUserAvatar.setOnClickListener {
                    val itProfile = Intent("im.fdx.v2ex.intent.profile")
                    itProfile.putExtra("username", replyItem.member!!.username)
                    mContext.startActivity(itProfile)
                }
            }
        }
    }

    private fun copyText(replyItem: ReplyModel) {
        XLog.d("I click menu copy")
        val manager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.primaryClip = ClipData.newPlainText("wtf", replyItem.content)
    }

    private fun thank(replyItem: ReplyModel, itemVH: ItemViewHolder): Boolean {
        XLog.tag(TAG).d("hehe" + verifyCode!!)
        if (verifyCode == null) {
            return true
        }
        val body = FormBody.Builder().add("t", verifyCode).build()

        HttpHelper.OK_CLIENT.newCall(Request.Builder()
                .headers(HttpHelper.baseHeaders)
                .url("https://www.v2ex.com/thank/reply/" + replyItem.id)
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
                        itemVH.tvThanks.text = (replyItem.thanks).toString()
                        itemVH.ivThank.imageTintList = ContextCompat.getColorStateList(mContext, R.color.primary)
                        itemVH.ivThank.isClickable = false
                        itemVH.tvThanks.isClickable = false
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
            val span: ForegroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.primary))
            spanString.setSpan(span, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            editText.append(spanString)
        }
        editText.setSelection(editText.length())
        editText.requestFocus()
    }


    override fun getItemCount(): Int {
        return mAllList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && itemCount > 1) {
            return TYPE_HEADER
        } else if (position == itemCount - 1) {
            return TYPE_FOOTER
        } else
            return TYPE_ITEM
    }

    //我重用了MainAdapter中的MainViewHolder

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvReplier: TextView = itemView.findViewById(R.id.tv_replier)
        internal var tvReplyTime: TextView = itemView.findViewById(R.id.tv_reply_time)
        internal var tvContent: GoodTextView = itemView.findViewById(R.id.tv_reply_content)
        internal var tvRow: TextView = itemView.findViewById(R.id.tv_reply_row)
        internal var tvThanks: TextView = itemView.findViewById(R.id.tv_thanks)
        internal var tvReply: TextView = itemView.findViewById(R.id.tv_reply)
        internal var ivThank: ImageView = itemView.findViewById(R.id.iv_thanks)
        internal var ivUserAvatar: CircleImageView = itemView.findViewById(R.id.iv_reply_avatar)
        internal var divider: View = itemView.findViewById(R.id.divider)
    }

    private class FooterViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvLoadMore: TextView = itemView.findViewById(R.id.tv_load_more)
    }

    interface AdapterCallback {
        fun onMethodCallback(type: Int)
    }

    companion object {
        val TYPE_FOOTER = 2
        private val TAG = DetailsAdapter::class.java.simpleName
        private val TYPE_HEADER = 0
        private val TYPE_ITEM = 1
    }
}
