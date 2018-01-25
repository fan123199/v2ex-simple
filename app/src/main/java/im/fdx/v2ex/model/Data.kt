package im.fdx.v2ex.model

import com.google.gson.annotations.SerializedName

data class Data(

        @field:SerializedName("path")
        val path: String? = null,

        @SerializedName("filename")
        val filename: String? = null,

        @SerializedName("size")
        val size: Int? = null,

        @SerializedName("ip")
        val ip: String? = null,

        @SerializedName("width")
        val width: Int? = null,

        @SerializedName("storename")
        val storename: String? = null,

        @SerializedName("delete")
        val delete: String? = null,

        @SerializedName("hash")
        val hash: String? = null,

        @SerializedName("url")
        val url: String? = null,

        @SerializedName("height")
        val height: Int? = null,

        @SerializedName("timestamp")
        val timestamp: Int? = null,
        val msg: String? = null
)