package com.hackedbuce.aerialdream.data

import com.google.gson.annotations.SerializedName

data class Asset(
    @SerializedName("url") val url: String,
    @SerializedName("accessibilityLabel") val accessibilityLabel: String,
    @SerializedName("id") val id: String,
    @SerializedName("timeOfDay") val timeOfDay: String
)
