package com.bakjoul.testwithings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.bakjoul.testwithings.navigation.Detail
import com.bakjoul.testwithings.navigation.Home
import com.bakjoul.testwithings.navigation.TopLevelBackStack
import com.bakjoul.testwithings.ui.home.HomeScreen
import com.bakjoul.testwithings.ui.theme.TestWithingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestWithingsTheme {
                //HomeScreen()
                val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }

                NavDisplay(
                    backStack = topLevelBackStack.backStack,
                    onBack = { topLevelBackStack.removeLast() },
                    entryProvider = entryProvider {
                        entry<Home> {
                            HomeScreen(
                                /*onSelectionValidated = {
                                    topLevelBackStack.add(Detail)
                                }*/
                            )
                        }

                        entry<Detail> {
                            //DetailScreen()
                        }
                    }
                )
            }
        }
    }
}
