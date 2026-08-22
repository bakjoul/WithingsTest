package com.bakjoul.testwithings.ui.home

import com.bakjoul.testwithings.domain.model.ImageResult

data class HomeViewState(
    val searchQuery: String,
    val activeSearchQuery: String,
    val results: List<ImageResult>,
    val selectedImageUrls: Set<String>,
    val isLoading: Boolean,
    val isLoadingNextPage: Boolean,
    val isSearchErrorVisible: Boolean
)
