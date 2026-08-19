package com.bakjoul.testwithings.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }
}
