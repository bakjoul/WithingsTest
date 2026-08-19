package com.bakjoul.testwithings.domain

import com.bakjoul.testwithings.domain.model.ImageResult

interface ImageRepository {
    suspend fun searchForImages(query: String, page: Int): List<ImageResult>
}
