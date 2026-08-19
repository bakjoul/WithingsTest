package com.bakjoul.testwithings.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponseDto(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImageDto>
)
