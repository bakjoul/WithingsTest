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
    private val _results: MutableStateFlow<List<ImageResult>> = MutableStateFlow(emptyList())
    private val _selectedImages: MutableStateFlow<Set<Int>> = MutableStateFlow(emptySet())
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var currentPage = 1
    private var isLoadingNextPage = false
    private var hasMoreResults = true

    private val _state: StateFlow<HomeViewState> = combine(
            _searchQuery,
            _results,
            _selectedImages,
            _isLoading
        ) { query, results, selectedImages, isLoading ->
            println("results: $results")

            HomeViewState(
                searchQuery = query,
                results = results,
                selectedImages = selectedImages,
                isLoading = isLoading
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = HomeViewState(
                searchQuery = "",
                results = emptyList(),
                selectedImages = emptySet(),
                isLoading = false
        )
    )
    val state: StateFlow<HomeViewState> = _state

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun onSearchButtonClicked() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val results = searchForImagesUseCase(_searchQuery.value, currentPage)
                _results.update { results }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onClearQueryButtonClicked() {
        _searchQuery.update { "" }
    }

    fun loadNextPage() {
        if (isLoadingNextPage || !hasMoreResults) return
        isLoadingNextPage = true

        viewModelScope.launch {

            try {
                val nextPage = currentPage + 1
                val results = searchForImagesUseCase(_searchQuery.value, nextPage)

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

    fun onImageSelected(id: Int) {
        _selectedImages.update { selected ->
            if (id in selected) {
                selected - id
            } else {
                selected + id
            }
        }
    }

    fun onValidateSelectionButtonClicked() {

    }
}
