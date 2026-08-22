package com.bakjoul.testwithings.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bakjoul.testwithings.R
import com.bakjoul.testwithings.ui.composable.SearchField
import com.bakjoul.testwithings.ui.composable.SmallButton
import com.bakjoul.testwithings.ui.snackbar.LocalSnackbarController
import com.bakjoul.testwithings.ui.snackbar.SnackbarEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSelectionValidated: ((List<String>) -> Unit)
) {
    val state by viewModel.state.collectAsState()

    val snackbarController = LocalSnackbarController.current
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()

            lastItem != null &&
            lastItem.index == layoutInfo.totalItemsCount - 1 &&
            lastItem.offset.y + lastItem.size.height <= layoutInfo.viewportEndOffset
        }
            .distinctUntilChanged()
            .collect { isAtBottom ->
            if (isAtBottom) {
                viewModel.loadNextPage()
            }
        }
    }

    val loadMoreErrorMessage = stringResource(R.string.load_more_error)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeViewEvent.LoadMoreError -> {
                    snackbarController.sendEvent(
                        SnackbarEvent(
                            message = loadMoreErrorMessage
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SearchField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    onSearch = { viewModel.onSearchButtonClicked() },
                    onClear = { viewModel.onClearQueryButtonClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.results.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append(stringResource(R.string.results_for))
                                    append(" ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(state.activeSearchQuery)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )

                            Text(
                                text = if (state.selectedImageUrls.isNotEmpty()) {
                                    pluralStringResource(
                                        R.plurals.selected_images_count,
                                        state.selectedImageUrls.size,
                                        state.selectedImageUrls.size
                                    )
                                } else {
                                    stringResource(R.string.select_images)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }

                        SmallButton(
                            verticalPadding = 6.dp,
                            enabled = state.selectedImageUrls.isNotEmpty(),
                            text = stringResource(R.string.clear_selection),
                            textSize = 14.sp,
                            onClick = { viewModel.clearSelection() }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.selectedImageUrls.size >= 2) {
                FloatingActionButton(
                    onClick = {
                        onSelectionValidated(state.selectedImageUrls.toList())
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_check_24),
                        contentDescription = stringResource(R.string.validate_button_desc)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (!state.isSearchErrorVisible) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.results,
                        key = { it.id }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    viewModel.onImageSelected(it.largeImageUrl)
                                }
                        ) {
                            val isInSelectedImages = it.largeImageUrl in state.selectedImageUrls

                            AsyncImage(
                                model = it.previewUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.baseline_broken_image_24),
                                alpha = if (isInSelectedImages) 0.7f else 1f
                            )

                            Icon(
                                painter = if (isInSelectedImages) {
                                    painterResource(R.drawable.outline_check_circle_24)
                                } else {
                                    painterResource(R.drawable.outline_circle_24)
                                },
                                contentDescription = if (isInSelectedImages) {
                                    stringResource(R.string.image_selected_desc)
                                } else {
                                    stringResource(R.string.image_unselected_desc)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            if (state.isLoading || state.isLoadingNextPage) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (state.isSearchErrorVisible && !state.isLoading) {
                Text(
                    text = stringResource(R.string.search_error),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
