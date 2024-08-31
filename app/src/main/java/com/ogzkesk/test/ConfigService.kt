package com.ogzkesk.test

import retrofit2.http.GET

interface ConfigService {

    @GET(CONFIG_ENDPOINT)
    suspend fun getConfig(): VersionConfig


    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/ogzkesk/config/main/"
        const val CONFIG_ENDPOINT = "version.json"
    }
}