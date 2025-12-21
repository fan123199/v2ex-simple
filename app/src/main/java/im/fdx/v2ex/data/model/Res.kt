package im.fdx.v2ex.data.model

import com.google.gson.annotations.SerializedName

data class Res(

        @field:SerializedName("code")
        val code: String? = null,

        @field:SerializedName("data")
        val data: Data? = null
)

