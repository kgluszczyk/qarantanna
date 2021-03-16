package com.gluszczykk.qarantanna

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Config(
    @field:Json(name = "logoURL") val logoUrl: String,
    @field:Json(name = "videoURL") val videoUrl: String,
    @field:Json(name = "soundURL") val musicUrl: String
) : Parcelable