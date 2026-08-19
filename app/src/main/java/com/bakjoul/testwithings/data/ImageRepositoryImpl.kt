package com.bakjoul.testwithings.data

import com.bakjoul.testwithings.domain.ImageRepository
import com.bakjoul.testwithings.domain.model.ImageResult

class ImageRepositoryImpl(private val api: PixabayApi) : ImageRepository {
    override suspend fun searchForImages(query: String): List<ImageResult> {
        return api.searchForImages(query)
            .hits
            .map { hit ->
                ImageResult(
                    id = hit.id,
                    previewUrl = hit.previewURL,
                    largeImageUrl = hit.largeImageURL
                )
        }
    }
}
