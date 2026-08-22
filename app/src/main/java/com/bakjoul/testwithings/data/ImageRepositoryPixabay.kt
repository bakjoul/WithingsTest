package com.bakjoul.testwithings.data

import com.bakjoul.testwithings.domain.ImageRepository
import com.bakjoul.testwithings.domain.model.ImageResult
import com.bakjoul.testwithings.domain.model.ImageSearchResult
import io.ktor.utils.io.CancellationException

class ImageRepositoryPixabay(private val api: PixabayApi) : ImageRepository {
    override suspend fun searchForImages(
        query: String,
        page: Int
    ): ImageSearchResult {
        return try {
            val images = api
                .searchForImages(query, page)
                .hits
                .map { hit ->
                    ImageResult(
                        id = hit.id,
                        previewUrl = hit.previewURL,
                        largeImageUrl = hit.largeImageURL
                    )
                }
            ImageSearchResult.Success(images)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ImageSearchResult.Error(e)
        }
    }
}
