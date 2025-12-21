package im.fdx.v2ex.data.model

import android.os.Parcelable
import im.fdx.v2ex.data.model.Topic
import im.fdx.v2ex.data.model.Member
import kotlinx.parcelize.Parcelize

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
                        var id: String? = "") : Parcelable, VModel()


