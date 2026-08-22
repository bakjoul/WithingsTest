package com.bakjoul.testwithings.ui.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.bakjoul.testwithings.domain.SearchForImagesUseCase
import com.bakjoul.testwithings.domain.model.ImageResult
import com.bakjoul.testwithings.domain.model.ImageSearchResult
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private val searchForImagesUseCase: SearchForImagesUseCase = mock()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should update results when search succeeds`() = runTest {
        // Given
        val query = "cats"
        everySuspend {
            searchForImagesUseCase.invoke(
                query,
                1
            )
        } returns getSuccessfulImageSearchResult()
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            // When
            viewModel.onSearchQueryChanged(query)
            viewModel.onSearchButtonClicked()
            advanceUntilIdle() // Wait for the search to complete

            val state = viewModel.state.value // Get the current state

            // Then
            assertThat(state.results).hasSize(3)

            cancelAndConsumeRemainingEvents() // Cancel the flow collection and consume remaining events
        }

        verifySuspend { searchForImagesUseCase.invoke(query, 1) }
    }

    @Test
    fun `should show search error when search fails`() = runTest {
        // Given
        val query = "cats"
        val exception = IOException("Network error")
        everySuspend { searchForImagesUseCase.invoke(query, 1) } returns ImageSearchResult.Error(
            exception
        )
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            // When
            viewModel.onSearchQueryChanged(query)
            viewModel.onSearchButtonClicked()
            advanceUntilIdle() // Wait for the search to complete

            val state = viewModel.state.value // Get the current state

            // Then
            assertThat(state.results).isEmpty()
            assertThat(state.isSearchErrorVisible).isTrue()
            assertThat(state.isLoading).isFalse()

            cancelAndConsumeRemainingEvents() // Cancel the flow collection and consume remaining events
        }

        verifySuspend { searchForImagesUseCase.invoke(query, 1) }
    }

    @Test
    fun `should append results when loading next page succeeds`() = runTest {
        // Given
        val query = "cats"
        everySuspend {
            searchForImagesUseCase.invoke(
                query,
                1
            )
        } returns getSuccessfulImageSearchResult()
        everySuspend {
            searchForImagesUseCase.invoke(
                query,
                2
            )
        } returns getOtherSuccessfulImageSearchResult()
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            viewModel.onSearchQueryChanged(query)
            viewModel.onSearchButtonClicked()
            advanceUntilIdle() // Wait for the first search to complete

            // When
            viewModel.loadNextPage()
            advanceUntilIdle() // Wait for the next page to load

            val state = viewModel.state.value // Get the current state

            // Then
            assertThat(state.results).hasSize(6)
            assertThat(state.isLoadingNextPage).isFalse()

            cancelAndConsumeRemainingEvents() // Cancel the flow collection and consume remaining events
        }

        verifySuspend { searchForImagesUseCase.invoke(query, 1) }
        verifySuspend { searchForImagesUseCase.invoke(query, 2) }
    }

    @Test
    fun `should emit load more error when loading next page fails`() = runTest {
        // Given
        val query = "cats"
        everySuspend {
            searchForImagesUseCase.invoke(
                query,
                1
            )
        } returns getSuccessfulImageSearchResult()
        everySuspend { searchForImagesUseCase.invoke(query, 2) } returns ImageSearchResult.Error(
            IOException("Network error")
        )
        initViewModel()

        viewModel.events.test {
            // First load results
            viewModel.onSearchQueryChanged(query)
            viewModel.onSearchButtonClicked()
            advanceUntilIdle()

            // When
            viewModel.loadNextPage()
            advanceUntilIdle() // Wait for the next page to load

            // Then
            assertThat(awaitItem()).isEqualTo(HomeViewEvent.LoadMoreError)

            cancelAndConsumeRemainingEvents() // Cancel the flow collection and consume remaining events
        }

        verifySuspend { searchForImagesUseCase.invoke(query, 1) }
        verifySuspend { searchForImagesUseCase.invoke(query, 2) }
    }

    @Test
    fun `should not search when query is blank`() = runTest {
        // Given
        initViewModel()
        viewModel.onSearchQueryChanged("  ")

        // When
        viewModel.onSearchButtonClicked()

        // Then
        verifySuspend(VerifyMode.not) { searchForImagesUseCase.invoke(any(), any()) }
    }

    @Test
    fun `should select image when image is not selected`() = runTest {
        // Given
        val imageUrl = "image_url"
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            // When
            viewModel.onImageSelected(imageUrl)

            // Then
            val state = awaitItem()
            assertThat(state.selectedImageUrls).containsOnly(imageUrl)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should deselect image when image is already selected`() = runTest {
        // Given
        val imageUrl = "image_url"
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            // When
            viewModel.onImageSelected(imageUrl) // Select the image
            awaitItem() // Wait for the state update after selection

            viewModel.onImageSelected(imageUrl) // Deselect the image

            // Then
            val state = awaitItem()
            assertThat(state.selectedImageUrls).isEmpty()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should clear selected images`() = runTest {
        // Given
        val imageUrl1 = "image_url_1"
        val imageUrl2 = "image_url_2"
        initViewModel()

        viewModel.state.test {
            awaitItem() // Initial value

            // When
            viewModel.onImageSelected(imageUrl1) // Select the first image
            awaitItem() // Wait for the state update after selection

            viewModel.onImageSelected(imageUrl2) // Select the second image
            awaitItem() // Wait for the state update after selection

            viewModel.clearSelection() // Clear selected images

            // Then
            val state = awaitItem()
            assertThat(state.selectedImageUrls).isEmpty()

            cancelAndConsumeRemainingEvents()
        }
    }

    // region IN
    private fun initViewModel() {
        viewModel = HomeViewModel(
            searchForImagesUseCase = searchForImagesUseCase
        )
    }

    private fun getSuccessfulImageSearchResult(): ImageSearchResult {
        return ImageSearchResult.Success(
            images = listOf(
                ImageResult(id = 1, previewUrl = "previewUrl1", largeImageUrl = "largeImageUrl1"),
                ImageResult(id = 2, previewUrl = "previewUrl2", largeImageUrl = "largeImageUrl2"),
                ImageResult(id = 3, previewUrl = "previewUrl3", largeImageUrl = "largeImageUrl3")
            )
        )
    }

    private fun getOtherSuccessfulImageSearchResult(): ImageSearchResult {
        return ImageSearchResult.Success(
            images = listOf(
                ImageResult(id = 4, previewUrl = "previewUrl4", largeImageUrl = "largeImageUrl4"),
                ImageResult(id = 5, previewUrl = "previewUrl5", largeImageUrl = "largeImageUrl5"),
                ImageResult(id = 6, previewUrl = "previewUrl6", largeImageUrl = "largeImageUrl6")
            )
        )
    }
    // endregion IN
}