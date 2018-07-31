package im.fdx.v2ex.model

import android.os.Parcelable
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.member.Member
import kotlinx.android.parcel.Parcelize

/**
 * Created by fdx on 2017/3/24.
 */

@Parcelize
class NotificationModel(var time: String? = "",
                        var replyPosition: String? = "",
                        var type: String? = "",
                        var topic: Topic? = Topic(),
                        var member: Member? = Member(),
                        var content: String? = "",
                        var id: String? = "") : Parcelable {
}
