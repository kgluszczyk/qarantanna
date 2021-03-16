package com.gluszczykk.qarantanna

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Config(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "logoUrl") @field:Json(name = "logoURL") val logoUrl: String,
    @ColumnInfo(name = "videoUrl") @field:Json(name = "videoURL") val videoUrl: String,
    @ColumnInfo(name = "soundUrl") @field:Json(name = "soundURL") val musicUrl: String
) : Parcelable