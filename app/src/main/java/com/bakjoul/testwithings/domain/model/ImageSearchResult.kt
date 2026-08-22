package com.bakjoul.testwithings.domain.model

sealed interface ImageSearchResult {
    data class Success(val images: List<ImageResult>) : ImageSearchResult
    data class Error(val exception: Exception) : ImageSearchResult
}
