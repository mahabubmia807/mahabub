package com.example

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface DubbingApi {
    @Multipart
    @POST("api/v1/self-dub/")
    suspend fun selfDubVideo(
        @Part video: MultipartBody.Part,
        @Part("target_lang") targetLang: RequestBody
    ): Response<DubbingResponse>

    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}

data class DubbingResponse(
    val success: Boolean,
    val message: String? = null,
    val download_url: String? = null,
    val error: String? = null
)
