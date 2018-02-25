package im.fdx.v2ex.ui.main

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.ui.member.MemberModel
import im.fdx.v2ex.ui.node.NodeModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by a708 on 15-8-18.
 * 主题模型
 */


//http://www.v2ex.com/api/topics/show.json?node_id=1
//[
//
//        {
//        "id" : 251393,
//        "title" : "室友是个女 coder",
//        "url" : "http://www.v2ex.com/t/251393",
//        "content" : "454f",
//        "content_rendered" : "吗？",
//        "replies" : 100,
//        "member" : {
//        "id" : 80938,
//        "username" : "boyhailong",
//        "tagline" : "",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/ad59/185e/80938_mini.png?m=1452315358",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/ad59/185e/80938_normal.png?m=1452315358",
//        "avatar_large" : "//cdn.v2ex.co/avatar/ad59/185e/80938_large.png?m=1452315358"
//        },
//        "node" : {
//        "id" : 320,
//        "name" : "wtf",
//        "title" : "不靠谱茶话会",
//        "title_alternative" : "WTF",
//        "url" : "http://www.v2ex.com/go/wtf",
//        "topics" : 212,
//        "avatar_mini" : "//cdn.v2ex.co/navatar/3207/2254/320_mini.png?m=1435210420",
//        "avatar_normal" : "//cdn.v2ex.co/navatar/3207/2254/320_normal.png?m=1435210420",
//        "avatar_large" : "//cdn.v2ex.co/navatar/3207/2254/320_large.png?m=1435210420"
//        },
//        "created" : 1453030019,
//        "last_modified" : 1453044647,
//        "last_touched" : 1453094527
//        },
//        {some of above}
//    ]
@Entity
@Parcelize
class Topic(
        @PrimaryKey
        var id: String = "",
        var title: String = "",
        var url: String = "",
        var content: String? = null,
        var content_rendered: String? = null,
        var replies: Int = 0,
        @Ignore
        var member: MemberModel? = null,
        @Ignore
        var node: NodeModel? = null,
        var created: Long = 0,
        var last_modified: Long = 0,
        var last_touched: Long = 0,
        @Ignore
        var comments: MutableList<Comment> = mutableListOf()) : BaseModel(), Parcelable {
    constructor(id: String) : this() {
        this.id = id
    }

    override fun toString() = "标题：$title,\n内容：$content"

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null || other !is Topic -> false
            else -> id == other.id
        }
    }

    override fun hashCode() = id.hashCode()

    override fun parse(): Nothing? = null
}

