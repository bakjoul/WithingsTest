package com.bakjoul.testwithings.domain

import com.bakjoul.testwithings.domain.model.ImageResult

class SearchForImagesUseCase(private val imageRepository: ImageRepository) {
    suspend operator fun invoke(
        query: String,
        page: Int
    ): List<ImageResult> {
        return imageRepository.searchForImages(query, page)
    }
}
