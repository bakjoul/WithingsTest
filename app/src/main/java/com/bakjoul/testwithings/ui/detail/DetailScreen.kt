package com.bakjoul.testwithings.ui.detail

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bakjoul.testwithings.R
import com.bakjoul.testwithings.ui.theme.Purple80
import com.bakjoul.testwithings.ui.utils.filmstripBorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = koinViewModel(),
    images: List<String>,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val pagerState = rememberPagerState(
        pageCount = { images.size }
    )
    val focusedPagerState = rememberPagerState(
        initialPage = pagerState.currentPage,
        pageCount = { images.size }
    )
    var isFocused by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    // Set the image count in the VM
    LaunchedEffect(images) {
        viewModel.setImageCount(images.size)
    }

    // Auto-scroll
    LaunchedEffect(state.isLoading, isFocused, pagerState) {
        if (state.isLoading || isFocused) return@LaunchedEffect

        while (!isFocused) {
            progress.snapTo(0f)

            // Wait for a user interaction
            val userInteracted = withTimeoutOrNull(3.5.seconds) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
                )

                pagerState.interactionSource.interactions.first {
                    it is DragInteraction.Start
                }
                true
            } ?: false

            // If user interacted, skip the auto-scroll and wait for the next iteration
            if (userInteracted) {
                progress.snapTo(0f)
                continue
            }

            // After the timeout, if the user didn't interact, auto-scroll to the next page
            if (!pagerState.isScrollInProgress && !isFocused) {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_24),
                            contentDescription = stringResource(R.string.back_button_icon_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.isLoading) 0f else 1f),
                    beyondViewportPageCount = images.size
                ) { page ->
                    val imageUrl = images[page]

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .filmstripBorder()
                            .clickable {
                                focusedPagerState.requestScrollToPage(pagerState.currentPage)
                                isFocused = true
                            },
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            Log.d("DetailScreen", "Image loaded: $imageUrl")
                            viewModel.onImageLoaded(imageUrl)
                        }
                    )
                }

                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Purple80,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Butt,
                    drawStopIndicator = {}
                )

                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, end = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                )
            }

            AnimatedVisibility(
                visible = isFocused,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = focusedPagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    pagerState.requestScrollToPage(focusedPagerState.currentPage)
                                    isFocused = false
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
