package com.bakjoul.testwithings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.bakjoul.testwithings.navigation.Detail
import com.bakjoul.testwithings.navigation.Home
import com.bakjoul.testwithings.navigation.TopLevelBackStack
import com.bakjoul.testwithings.ui.detail.DetailScreen
import com.bakjoul.testwithings.ui.home.HomeScreen
import com.bakjoul.testwithings.ui.snackbar.LocalSnackbarController
import com.bakjoul.testwithings.ui.snackbar.SnackbarController
import com.bakjoul.testwithings.ui.theme.PurpleGrey40
import com.bakjoul.testwithings.ui.theme.TestWithingsTheme
import com.bakjoul.testwithings.ui.utils.ObserveAsEvents
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestWithingsTheme {
                val snackbarController = remember { SnackbarController() }

                CompositionLocalProvider(
                    LocalSnackbarController provides snackbarController
                ) {
                    val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavDisplay(
                            backStack = topLevelBackStack.backStack,
                            onBack = { topLevelBackStack.removeLast() },
                            entryProvider = entryProvider {
                                entry<Home> {
                                    HomeScreen(
                                        onSelectionValidated = { imageUrls ->
                                            topLevelBackStack.add(Detail(imageUrls))
                                        }
                                    )
                                }

                                entry<Detail> { detail ->
                                    DetailScreen(
                                        images = detail.imageUrls,
                                        onBack = { topLevelBackStack.removeLast() }
                                    )
                                }
                            }
                        )

                        val snackbarHostState = remember { SnackbarHostState() }
                        val coroutineScope = rememberCoroutineScope()

                        ObserveAsEvents(
                            flow = snackbarController.events,
                            key1 = snackbarHostState
                        ) { event ->
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()

                                snackbarHostState.showSnackbar(
                                    message = event.message,
                                    actionLabel = event.action?.name,
                                    duration = event.duration
                                ).also { result ->
                                    if (result == SnackbarResult.ActionPerformed) {
                                        event.action?.action?.invoke()
                                    }
                                }
                            }
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Snackbar(
                                action = {
                                    it.visuals.actionLabel?.let { actionLabel ->
                                        Text(
                                            text = actionLabel,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .clickable { it.performAction() },
                                            color = Color.White
                                        )
                                    }
                                },
                                containerColor = PurpleGrey40,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = it.visuals.message,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
