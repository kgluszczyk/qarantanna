package com.gluszczykk.qarantanna

import retrofit2.http.GET

interface ConfigService {
    @GET("config")
    suspend fun getConfig(): Config
}