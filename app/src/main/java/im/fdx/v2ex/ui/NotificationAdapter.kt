package im.fdx.v2ex.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import im.fdx.v2ex.R
import im.fdx.v2ex.model.NotificationModel
import im.fdx.v2ex.ui.details.DetailsActivity
import im.fdx.v2ex.utils.Keys
import im.fdx.v2ex.utils.extensions.load

/**
 * Created by fdx on 2017/3/24.
 */

class NotificationAdapter(var mContext: Context, var mModels: List<NotificationModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var number = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = NotificationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notiHolder = holder as NotificationViewHolder

        if (position <= number - 1) {
            notiHolder.itemView.foregroundTintMode = PorterDuff.Mode.DST_IN
            notiHolder.itemView.foregroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.transparent))
        }

        val model = mModels[position]

        notiHolder.itemView.setOnClickListener {
            val intentDetail = Intent(mContext, DetailsActivity::class.java)
            intentDetail.putExtra(Keys.KEY_TOPIC_ID, model.topic?.id)
            mContext.startActivity(intentDetail)
        }

        notiHolder.tvAction.text = model.type
        notiHolder.tvContent.text = model.content
        notiHolder.ivAvatar.load(model.member?.avatarNormalUrl)
        notiHolder.tvUsername.text = model.member?.username
        notiHolder.tvTime.text = model.time
        notiHolder.tvTopicTitle.text = model.topic?.title

        notiHolder.ivAvatar.setOnClickListener {
            val intent = Intent(mContext, MemberActivity::class.java)
            intent.putExtra(Keys.KEY_USERNAME, model.member?.username)
            mContext.startActivity(intent)
        }

    }


    override fun getItemCount() = mModels.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTopicTitle: TextView = itemView.findViewById(R.id.tv_topic_title)
        var tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var tvContent: TextView = itemView.findViewById(R.id.content_notification)
        var tvAction: TextView = itemView.findViewById(R.id.tv_action_notification)
        var ivAvatar: CircleImageView = itemView.findViewById(R.id.iv_avatar_notification)

    }
}

