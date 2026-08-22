package com.bakjoul.testwithings.domain

import com.bakjoul.testwithings.domain.model.ImageSearchResult

class SearchForImagesUseCase(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(
        query: String,
        page: Int
    ): ImageSearchResult {
        return imageRepository.searchForImages(query, page)
    }
}
