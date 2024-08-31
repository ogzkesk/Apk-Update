package com.ogzkesk.test

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VersionConfig(
    @SerializedName("version_name")
    val versionName: String,
    @SerializedName("version_code")
    val versionCode: Int,
    @SerializedName("version_url")
    val versionUrl: String
)
