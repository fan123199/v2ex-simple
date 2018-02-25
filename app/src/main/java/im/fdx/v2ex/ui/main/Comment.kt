package im.fdx.v2ex.ui.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(var title: String = "",
                   var created: Long = 0,
                   var content: String = "") : Parcelable