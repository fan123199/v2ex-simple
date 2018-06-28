package im.fdx.v2ex.ui.member

import android.os.Parcel
import android.os.Parcelable
import im.fdx.v2ex.model.BaseModel
import im.fdx.v2ex.ui.main.Topic

/**
 * Created by fdx on 2017/7/16.
 * fdx will maintain it
 */
data class MemberReplyModel(var id: String? = "",
                            var topic: Topic = Topic(),
                            var content: String? = null,
                            var create: Long = 0L) : BaseModel(), Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MemberReplyModel> = object : Parcelable.Creator<MemberReplyModel> {
            override fun createFromParcel(source: Parcel): MemberReplyModel = MemberReplyModel(source)
            override fun newArray(size: Int): Array<MemberReplyModel?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readParcelable<Topic>(Topic::class.java.classLoader),
            source.readString(),
            source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeParcelable(topic, 0)
        dest.writeString(content)
        dest.writeLong(create)
    }
}