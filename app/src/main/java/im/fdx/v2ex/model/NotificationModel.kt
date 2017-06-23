package im.fdx.v2ex.model

import android.os.Parcel
import android.os.Parcelable

import im.fdx.v2ex.ui.main.TopicModel

/**
 * Created by fdx on 2017/3/24.
 */

class NotificationModel(var time: String? = "",
                        var replyPosition: String? = "",
                        var type: String? = "",
                        var topic: TopicModel? = TopicModel(),
                        var member: MemberModel? = MemberModel(),
                        var content: String? = "",
                        var id: String? = "") : BaseModel(), Parcelable {

    override fun toString() = "NotificationModel{type='$type', topic=$topic," +
            " member=$member, content='$content', id='$id', time='$time'}"

    override fun parse() = null

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<NotificationModel> = object : Parcelable.Creator<NotificationModel> {
            override fun createFromParcel(source: Parcel): NotificationModel = NotificationModel(source)
            override fun newArray(size: Int): Array<NotificationModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<TopicModel>(TopicModel::class.java.classLoader),
            source.readParcelable<MemberModel>(MemberModel::class.java.classLoader),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(time)
        dest.writeString(replyPosition)
        dest.writeString(type)
        dest.writeParcelable(topic, 0)
        dest.writeParcelable(member, 0)
        dest.writeString(content)
        dest.writeString(id)
    }
}
