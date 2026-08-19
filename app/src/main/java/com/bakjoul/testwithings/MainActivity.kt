package com.bakjoul.testwithings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bakjoul.testwithings.ui.home.HomeScreen
import com.bakjoul.testwithings.ui.theme.TestWithingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestWithingsTheme {
                HomeScreen()
            }
        }
    }
}
