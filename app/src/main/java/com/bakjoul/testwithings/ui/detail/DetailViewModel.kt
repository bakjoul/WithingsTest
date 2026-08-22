package com.bakjoul.testwithings.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class DetailViewModel : ViewModel() {

    private val _imageCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private val _loadedImages: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    private val _state: StateFlow<DetailViewState> = combine(
        _imageCount,
        _loadedImages
    ) { count, loadedImages ->
        DetailViewState(
            isLoading = loadedImages.size < count
    )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DetailViewState(
            isLoading = true
        )
    )
    val state: StateFlow<DetailViewState> = _state

    fun setImageCount(count: Int) {
        _imageCount.value = count
    }

    fun onImageLoaded(imageUrl: String) {
        _loadedImages.update { it + imageUrl }
    }
}
