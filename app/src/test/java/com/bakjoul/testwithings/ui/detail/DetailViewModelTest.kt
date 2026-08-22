package com.bakjoul.testwithings.ui.detail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = DetailViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should stop loading when all images are loaded`() = runTest {
        // Given
        val imageUrls = listOf("image_url_1", "image_url_2", "image_url_3")

        viewModel.state.test {
            awaitItem() // Initial state

            viewModel.setImageCount(imageUrls.size)

            viewModel.onImageLoaded(imageUrls[0])
            viewModel.onImageLoaded(imageUrls[1])

            // When
            viewModel.onImageLoaded(imageUrls[2])
            advanceUntilIdle()

            // Then
            val state = viewModel.state.value
            assertThat(state.isLoading).isFalse()

            cancelAndConsumeRemainingEvents()
        }
    }
}
