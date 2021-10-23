package im.fdx.v2ex.ui.node

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

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

@Entity
@Parcelize
data class Node(
        @PrimaryKey
        @ColumnInfo(name = "node_id")
        var id: String = "",
        @ColumnInfo(name = "node_name")
        var name: String = "",   //英文名称
        @ColumnInfo(name = "node_url")
        var url: String = "",
        @ColumnInfo(name = "node_title")
        var title: String = "",  //展示名称
        var title_alternative: String = "",
        var topics: Int = 0,
        var stars: Int = 0,
        @ColumnInfo(name = "node_created")
        var created: Long = 0,
        @ColumnInfo(name = "node_avatar_normal")
        var avatar_normal: String? = "",
        var header: String? = "",
        var category: String? = "") : Parcelable {


  val avatarNormalUrl: String
    get() = "https:" + Regex("\\?s=\\d{1,3}").replace(avatar_normal?:"", "?s=64")

  val avatarLargeUrl: String
    get() = when {
      avatar_normal?.startsWith("/static") == true -> "https://www.v2ex.com/static/img/node_large.png"
      else -> Regex("\\?s=\\d{1,3}").replace(avatar_normal?:"", "?s=128").replace("normal","large").replace("mini","large")
    }

  override fun toString() = "$title / $name"

}
