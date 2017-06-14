package im.fdx.v2ex.ui.node

import android.os.Parcel
import android.os.Parcelable

import im.fdx.v2ex.model.BaseModel

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

data class NodeModel(var id: String,
                     var name: String,
                     var url: String,
                     var title: String,
                     var title_alternative: String,
                     var topics: Int,
                     var stars: Int,
                     var created: Long,
                     var avatar_mini: String?,
                     var avatar_normal: String?,
                     var avatar_large: String?,
                     var header: String?) : BaseModel(), Parcelable {


    constructor() : this("", "", "", "", "", 0, 0, 0, "", "", "", "")

    constructor(name: String) : this("", name, "", "", "", 0, 0, 0, "", "", "", "")

    val avatarMiniUrl: String
        get() = "http:$avatar_mini"

    val avatarNormalUrl: String
        get() = "http:${avatar_normal}"

    val avatarLargeUrl: String
        get() = "http:${avatar_large}"

    override fun toString() = "$title / $name"

    override fun parse() = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<NodeModel> = object : Parcelable.Creator<NodeModel> {
            override fun createFromParcel(source: Parcel): NodeModel = NodeModel(source)
            override fun newArray(size: Int): Array<NodeModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(url)
        dest.writeString(title)
        dest.writeString(title_alternative)
        dest.writeInt(topics)
        dest.writeInt(stars)
        dest.writeLong(created)
        dest.writeString(avatar_mini)
        dest.writeString(avatar_normal)
        dest.writeString(avatar_large)
        dest.writeString(header)
    }
}
