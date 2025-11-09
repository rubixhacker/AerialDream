package com.hackedbuce.aerialdream.data

import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("id") val id: String,
    @SerializedName("assets") val assets: List<Asset>
)
