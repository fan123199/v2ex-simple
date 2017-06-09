package im.fdx.v2ex.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by a708 on 16-1-16.
 * V2ex 的 个人信息 模型

 * username 和 id 都是 key value
 */

//{
//        "status" : "found",
//        "id" : 32044,
//        "url" : "http://www.v2ex.com/member/cxshun",
//        "username" : "cxshun",
//        "website" : "http://www.chenxiaoshun.com/",
//        "twitter" : "cxshun",
//        "psn" : "",
//        "github" : "cxshun",
//        "btc" : "",
//        "location" : "",
//        "tagline" : "",
//        "bio" : "",
//        "avatar_mini" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_mini.png?m=1369031007",
//        "avatar_normal" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_normal.png?m=1369031007",
//        "avatar_large" : "//cdn.v2ex.co/avatar/4b75/1bc7/32044_large.png?m=1369031007",
//        "created" : 1357733451
//        }

data class MemberModel(
        var id: String = "",
        var username: String = "",
        var tagline: String = "",
        var avatar_mini: String = "",
        var created: String = "",
        var avatar_normal: String = "",
        var avatar_large: String = "",
        var github: String = "",
        var btc: String = "",
        var location: String = "",
        var bio: String = "",
        var twitter: String = "",
        var website: String = ""
) : BaseModel(), Parcelable {

    override fun parse() = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MemberModel> = object : Parcelable.Creator<MemberModel> {
            override fun createFromParcel(source: Parcel): MemberModel = MemberModel(source)
            override fun newArray(size: Int): Array<MemberModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(username)
        dest.writeString(tagline)
        dest.writeString(avatar_mini)
        dest.writeString(created)
        dest.writeString(avatar_normal)
        dest.writeString(avatar_large)
        dest.writeString(github)
        dest.writeString(btc)
        dest.writeString(location)
        dest.writeString(bio)
        dest.writeString(twitter)
        dest.writeString(website)
    }
    //    constructor(id: Long, username: String) {
//        this.id = id
//        this.username = username
//    }


    val avatarNormalUrl: String
        get() = "http:" + avatar_normal

    val avatarLargeUrl: String
        get() = "http:" + avatar_large

    val avatarMiniUrl: String
        get() = "http:" + avatar_mini

}
