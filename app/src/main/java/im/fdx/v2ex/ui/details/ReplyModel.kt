package im.fdx.v2ex.ui.details

import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.ui.member.Member
import kotlinx.android.parcel.Parcelize

/**
 * Created by a708 on 15-9-8.
 * 评论模型，用于传递从JSON获取到的数据。
 * 以后将加入添加评论功能。
 */

//{
//        "id" : 2826846,
//        "thanks" : 0,
//        "content" : "关键你是男还是女？",
//        "content_rendered" : "关键你是男还是女？",
//        "member" : {
//        "id" : 27619,
//        "username" : "hengzhang",
//        "tagline" : "我白天是个民工，晚上就是个有抱负的IT人士。",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_mini.png?m=1413707431",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_normal.png?m=1413707431",
//        "avatar_large" : "//cdn.v2ex.co/avatar/d165/7a2a/27619_large.png?m=1413707431"
//        },
//        "created" : 1453030169,
//        "last_modified" : 1453030169
//        }

@Parcelize
data class ReplyModel(var id: String = "",
                      var content: String = "",
                      var content_rendered: String = "",
                      var thanks: Int = 0,
                      var created: Long = 0,
                      var isThanked: Boolean = false,
                      var member: Member? = null,
                      var isLouzu: Boolean = false
) : BaseModel(), Parcelable {

    override fun toString() = "ReplyModel{content='$content_rendered}"
}
