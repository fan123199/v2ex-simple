package im.fdx.v2ex.ui.member

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

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
//        "avatar_mini" : "//cdn.v2ex.com/gravatar/273313b23fdf59a7695f46c6ae175776?s=24&d=retro,
//       部分新域名， 部分旧域名   cdn.v2ex.com/avatar/00a6/7ce3/279733_mini.png?m=1546517640
//        "avatar_normal" : "//cdn.v2ex.com/gravatar/273313b23fdf59a7695f46c6ae175776?s=24&d=retro",
//          有坑，现在都是返回小图。
//        "avatar_large" : "//cdn.v2ex.com/gravatar/273313b23fdf59a7695f46c6ae175776?s=24&d=retro",
//        "created" : 1357733451
//        }

@Parcelize
data class Member(
        @ColumnInfo(name = "member_id")
        var id: String = "",
        var username: String = "",
        var tagline: String? = "",
        @ColumnInfo(name = "member_created")
        var created: String = "",
        @ColumnInfo(name = "member_avatar_normal")
        var avatar_normal: String = "",
        var bio: String? = "",
        var github: String? = "",
        var btc: String? = "",
        var location: String? = "",
        var twitter: String? = "",
        var website: String? = ""
) : Parcelable {

  val avatarNormalUrl: String?  //为了减少在sov2ex 的页面的报错，因为他的接口不返回头像
    get() {
        if(avatar_normal == "") return null

        val prefix = if (avatar_normal.startsWith("https:")) "" else "https:"
        return prefix + Regex("\\?s=\\d{1,3}").replace(avatar_normal, "?s=64")
    }

  val avatarLargeUrl: String
    get() {
        val prefix = if (avatar_normal.startsWith("https:")) "" else "https:"
        return prefix + Regex("\\?s=\\d{1,3}").replace(avatar_normal, "?s=128").replace("normal","large").replace("mini","large")
    }
}
