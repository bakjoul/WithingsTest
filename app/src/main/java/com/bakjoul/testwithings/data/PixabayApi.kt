package com.bakjoul.testwithings.data

import com.bakjoul.testwithings.BuildConfig
import com.bakjoul.testwithings.data.model.PixabayResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class PixabayApi(private val client: HttpClient) {
    suspend fun searchForImages(
        query: String,
        page: Int,
        perPage: Int = 20
    ): PixabayResponseDto {
        return client
            .get {
                parameter("key", BuildConfig.PIXABAY_API_KEY)
                parameter("q", query)
                parameter("image_type", "photo")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            .body()
    }
}
