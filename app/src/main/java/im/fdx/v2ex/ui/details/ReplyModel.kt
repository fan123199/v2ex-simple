package im.fdx.v2ex.ui.details

import android.os.Parcel
import android.os.Parcelable

import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.model.MemberModel

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

data class ReplyModel(var id: String = "",
                      var content: String = "",
                      var content_rendered: String = "",
                      var thanks: Int = 0,
                      var created: Long = 0,
                      var member: MemberModel = MemberModel()
) : BaseModel(), Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readLong(),
            source.readParcelable<MemberModel>(MemberModel::class.java.classLoader)
    )


    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeString(content)
        dest?.writeString(content_rendered)
        dest?.writeInt(thanks)
        dest?.writeLong(created)
        dest?.writeValue(member)
    }

    override fun toString() = "ReplyModel{content='$content_rendered}"

    override fun parse(): BaseModel? = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ReplyModel> = object : Parcelable.Creator<ReplyModel> {
            override fun createFromParcel(source: Parcel): ReplyModel = ReplyModel(source)
            override fun newArray(size: Int): Array<ReplyModel?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0
}
