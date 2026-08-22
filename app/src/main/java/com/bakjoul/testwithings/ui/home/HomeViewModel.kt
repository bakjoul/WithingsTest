package com.bakjoul.testwithings.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakjoul.testwithings.domain.SearchForImagesUseCase
import com.bakjoul.testwithings.domain.model.ImageResult
import com.bakjoul.testwithings.domain.model.ImageSearchResult
import com.bakjoul.testwithings.ui.utils.combine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val _isLoadingNextPage: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isSearchErrorVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var currentPage = 1
    private var hasMoreResults = true
    private var searchJob: Job? = null

    private val _state: StateFlow<HomeViewState> = combine(
        _searchQuery,
        _activeSearchQuery,
        _results,
        _selectedImageUrls,
        _isLoading,
        _isLoadingNextPage,
        _isSearchErrorVisible
    ) { query, activeQuery, results, selectedImages, isLoading, isLoadingNextPage, isSearchErrorVisible ->
        HomeViewState(
            searchQuery = query,
            activeSearchQuery = activeQuery,
            results = results,
            selectedImageUrls = selectedImages,
            isLoading = isLoading,
            isLoadingNextPage = isLoadingNextPage,
            isSearchErrorVisible = isSearchErrorVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = HomeViewState(
            searchQuery = "",
            activeSearchQuery = "",
            results = emptyList(),
            selectedImageUrls = emptySet(),
            isLoading = false,
            isLoadingNextPage = false,
            isSearchErrorVisible = false
        )
    )
    val state: StateFlow<HomeViewState> = _state

    private val _events: MutableSharedFlow<HomeViewEvent> = MutableSharedFlow()
    val events: SharedFlow<HomeViewEvent> = _events.asSharedFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun onSearchButtonClicked() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        _activeSearchQuery.update { query }
        currentPage = 1
        hasMoreResults = true
        _selectedImageUrls.value = emptySet()
        _isSearchErrorVisible.value = false

        _isLoading.value = true

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            try {
                when (val result = searchForImagesUseCase(query, currentPage)) {
                    is ImageSearchResult.Success -> {
                        _results.value = result.images
                    }

                    is ImageSearchResult.Error -> {
                        _results.value = emptyList()
                        _isSearchErrorVisible.value = true
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onClearQueryButtonClicked() {
        searchJob?.cancel()
        searchJob = null

        _searchQuery.value = ""
        _activeSearchQuery.value = ""
        _results.value = emptyList()
        _selectedImageUrls.value = emptySet()
        _isSearchErrorVisible.value = false
    }

    fun loadNextPage() {
        if (_isLoadingNextPage.value || !hasMoreResults || _activeSearchQuery.value.isBlank()) return

        _isLoadingNextPage.value = true

        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1

                when (val result = searchForImagesUseCase(_activeSearchQuery.value, nextPage)) {
                    is ImageSearchResult.Success -> {
                        if (result.images.isEmpty()) {
                            hasMoreResults = false
                            return@launch
                        }

                        _results.update { currentResults ->
                            (currentResults + result.images).distinctBy { it.id }
                        }

                        currentPage = nextPage
                    }

                    is ImageSearchResult.Error -> {
                        _events.emit(HomeViewEvent.LoadMoreError)
                    }
                }
            } finally {
                _isLoadingNextPage.value = false
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

    fun onSuggestionClick(keyword: String) {
        _searchQuery.value = keyword
        onSearchButtonClicked()
    }
}
