package com.bakjoul.testwithings.domain

import com.bakjoul.testwithings.domain.model.ImageSearchResult

interface ImageRepository {
    suspend fun searchForImages(query: String, page: Int): ImageSearchResult
}
