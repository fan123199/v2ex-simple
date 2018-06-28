package im.fdx.v2ex.ui.node

import android.arch.persistence.room.ColumnInfo
import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by a708 on 16-1-17.
 * V2ex 节点模型
 * 其中 name 是 key value
 */

///api/topics/show.json
//
//        参数（选其一）
//        username	根据用户名取该用户所发表主题
//        node_id	根据节点id取该节点下所有主题
//        node_name	根据节点名取该节点下所有主题

//------------------
//{
//        "id" : 90,
//        "name" : "python",
//        "url" : "http://www.v2ex.com/go/python",
//        "title" : "Python",
//        "title_alternative" : "Python",
//        "topics" : 4272,
//        "stars" : 3234,
//
//        "header" : "这里讨论各种 Python 语言编程话题，也包括 Django，Tornado 等框架的讨论。这里是一个能够帮助你解决实际问题的地方。",
//
//
//        "footer" : null,
//
//        "created" : 1278683336,
//        "avatar_mini" : "//cdn.v2ex.co/navatar/8613/985e/90_mini.png?m=1452823690",
//        "avatar_normal" : "//cdn.v2ex.co/navatar/8613/985e/90_normal.png?m=1452823690",
//        "avatar_large" : "//cdn.v2ex.co/navatar/8613/985e/90_large.png?m=1452823690"
//        }

@Parcelize
data class Node(
        @ColumnInfo(name = "node_id")
        var id: String = "",
        var name: String = "",
        @ColumnInfo(name = "node_url")
        var url: String = "",
        @ColumnInfo(name = "node_title")
        var title: String = "",
        var title_alternative: String = "",
        var topics: Int = 0,
        var stars: Int = 0,
        @ColumnInfo(name = "node_created")
        var created: Long = 0,
        @ColumnInfo(name = "node_avatar_mini")
        var avatar_mini: String = "",
        @ColumnInfo(name = "node_avatar_normal")

        var avatar_normal: String = "",
        @ColumnInfo(name = "node_avatar_large")

        var avatar_large: String = "",
        var header: String = "") : BaseModel(), Parcelable {

    val avatarMiniUrl: String
        get() = "http:$avatar_mini"

    val avatarNormalUrl: String
        get() = "http:$avatar_normal"

    val avatarLargeUrl: String
        get() = when {
            !avatar_large.startsWith("/static") -> "http:$avatar_large"
            else -> "https://www.v2ex.com/static/img/node_large.png"
        }

    override fun toString() = "$title / $name"

}
