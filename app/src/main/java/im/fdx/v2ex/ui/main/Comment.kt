package im.fdx.v2ex.ui.main

import android.os.Parcelable
import im.fdx.v2ex.model.VModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(var title: String = "",
                   var created: Long = 0,
                   var createdOriginal: String = "",
                   var content: String = "") : Parcelable, VModel()