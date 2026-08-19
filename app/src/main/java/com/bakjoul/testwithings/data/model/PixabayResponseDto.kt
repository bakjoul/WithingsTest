package com.bakjoul.testwithings.data.model

data class PixabayResponseDto(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImageDto>
)
