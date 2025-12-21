package im.fdx.v2ex.data.model

import android.os.Parcelable
import im.fdx.v2ex.data.model.Topic
import kotlinx.parcelize.Parcelize

/**
 * Created by fdx on 2017/7/16.
 * fdx will maintain it
 */
@Parcelize
data class MemberReplyModel(var id: String? = "",
                            var topic: Topic = Topic(),
                            var content: String? = null,
                            var createdOriginal: String  = "",
                            var create: Long = 0L) : Parcelable


