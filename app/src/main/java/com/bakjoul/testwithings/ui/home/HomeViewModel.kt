package com.bakjoul.testwithings.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakjoul.testwithings.domain.SearchForImagesUseCase
import com.bakjoul.testwithings.domain.model.ImageResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val searchForImagesUseCase: SearchForImagesUseCase
) : ViewModel() {

    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val _activeSearchQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val _results: MutableStateFlow<List<ImageResult>> = MutableStateFlow(emptyList())
    private val _selectedImageUrls: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var currentPage = 1
    private var isLoadingNextPage = false
    private var hasMoreResults = true

    private val _state: StateFlow<HomeViewState> = combine(
            _searchQuery,
        _activeSearchQuery,
            _results,
            _selectedImageUrls,
            _isLoading
        ) { query, activeQuery, results, selectedImages, isLoading ->
            HomeViewState(
                searchQuery = query,
                activeSearchQuery = activeQuery,
                results = results,
                selectedImageUrls = selectedImages,
                isLoading = isLoading
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = HomeViewState(
                searchQuery = "",
                activeSearchQuery = "",
                results = emptyList(),
                selectedImageUrls = emptySet(),
                isLoading = false
        )
    )
    val state: StateFlow<HomeViewState> = _state

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun onSearchButtonClicked() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        _activeSearchQuery.update { query }
        currentPage = 1
        hasMoreResults = true

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val results = searchForImagesUseCase(query, currentPage)
                _results.update { results }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onClearQueryButtonClicked() {
        _searchQuery.value = ""
        _activeSearchQuery.value = ""
        _results.value = emptyList()
    }

    fun loadNextPage() {
        if (isLoadingNextPage || !hasMoreResults || _activeSearchQuery.value.isBlank()) return

        isLoadingNextPage = true

        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val results = searchForImagesUseCase(_activeSearchQuery.value, nextPage)

                if (results.isEmpty()) {
                    hasMoreResults = false
                    return@launch
                }

                _results.update { currentResults ->
                    (currentResults + results).distinctBy { it.id }
                }

                currentPage = nextPage
            } finally {
                isLoadingNextPage = false
            }
        }
    }

    fun onImageSelected(imageUrl: String) {
        _selectedImageUrls.update { selected ->
            if (imageUrl in selected) {
                selected - imageUrl
            } else {
                selected + imageUrl
            }
        }
    }

    fun clearSelection() {
        _selectedImageUrls.update { emptySet() }
    }
}
