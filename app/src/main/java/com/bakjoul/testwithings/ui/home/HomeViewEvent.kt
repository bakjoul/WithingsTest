package com.bakjoul.testwithings.ui.home

sealed interface HomeViewEvent {
    data object LoadMoreError : HomeViewEvent
}
