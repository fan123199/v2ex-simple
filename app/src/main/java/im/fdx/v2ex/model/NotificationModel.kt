package im.fdx.v2ex.model

import android.os.Parcelable
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.member.MemberModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by fdx on 2017/3/24.
 */

@Parcelize
class NotificationModel(var time: String? = "",
                        var replyPosition: String? = "",
                        var type: String? = "",
                        var topic: Topic? = Topic(),
                        var member: MemberModel? = MemberModel(),
                        var content: String? = "",
                        var id: String? = "") : BaseModel(), Parcelable {
    override fun parse() = null
}
