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
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isLoadingMore: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var currentPage = 1

    private val _state: StateFlow<HomeViewState> = combine(
            _searchQuery,
            _results,
            _isLoading,
            _isLoadingMore
        ) { query, results, isLoading, isLoadingMore ->
            println("results: $results")

            HomeViewState(
                searchQuery = query,
                results = results,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = HomeViewState(
                searchQuery = "",
                results = emptyList(),
                isLoading = false,
                isLoadingMore = false
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
}
