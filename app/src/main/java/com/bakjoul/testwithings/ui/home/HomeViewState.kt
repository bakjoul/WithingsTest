package com.bakjoul.testwithings.ui.home

import com.bakjoul.testwithings.domain.model.ImageResult

data class HomeViewState(
    val searchQuery: String,
    val results: List<ImageResult>,
    val selectedImages: Set<Int>,
    val isLoading: Boolean
)
