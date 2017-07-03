package im.fdx.v2ex.ui.main

import android.os.Parcel
import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.model.MemberModel
import im.fdx.v2ex.ui.node.NodeModel

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
class TopicModel(var id: String = "", var title: String = "", var url: String = "",
                 var content: String? = null, var content_rendered: String? = null, var replies: Int = 0,
                 var member: MemberModel? = null, var node: NodeModel? = null, var created: Long = 0,
                 var last_modified: Long = 0, var last_touched: Long = 0) : BaseModel(), Parcelable {

    constructor(id: String) : this() {
        this.id = id
    }

    override fun toString() = "标题：$title,\n内容：$content"

    override fun equals(other: Any?): Boolean {
        when {
            this === other -> return true
            other == null || other !is TopicModel -> return false
            else -> return title == other.title
        }
    }

    override fun hashCode() = title.hashCode()

    override fun parse() = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<TopicModel> = object : Parcelable.Creator<TopicModel> {
            override fun createFromParcel(source: Parcel): TopicModel = TopicModel(source)
            override fun newArray(size: Int): Array<TopicModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readParcelable<MemberModel>(MemberModel::class.java.classLoader),
            source.readParcelable<NodeModel>(NodeModel::class.java.classLoader),
            source.readLong(),
            source.readLong(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(title)
        dest.writeString(url)
        dest.writeString(content)
        dest.writeString(content_rendered)
        dest.writeInt(replies)
        dest.writeParcelable(member, 0)
        dest.writeParcelable(node, 0)
        dest.writeLong(created)
        dest.writeLong(last_modified)
        dest.writeLong(last_touched)
    }
}