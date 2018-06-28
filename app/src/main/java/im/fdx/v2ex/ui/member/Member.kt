package im.fdx.v2ex.ui.member

import android.arch.persistence.room.ColumnInfo
import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import kotlinx.android.parcel.Parcelize

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

@Parcelize
data class Member(
        @ColumnInfo(name = "member_id")
        var id: String = "",
        var username: String = "",
        var tagline: String = "",
        var avatar_mini: String = "",
        @ColumnInfo(name = "member_created")
        var created: String = "",
        @ColumnInfo(name = "member_avatar_normal")
        var avatar_normal: String = "",
        @ColumnInfo(name = "member_avatar_large")
        var avatar_large: String = "",
        var github: String = "",
        var btc: String = "",
        var location: String = "",
        var bio: String = "",
        var twitter: String = "",
        var website: String = ""
) : BaseModel(), Parcelable {

    val avatarNormalUrl: String
        get() = "http:" + avatar_normal

    val avatarLargeUrl: String
        get() = "http:" + avatar_large

    val avatarMiniUrl: String
        get() = "http:" + avatar_mini

}
