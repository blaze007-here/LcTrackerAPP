package com.example.lctracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.lctracker.ui.LcTrackerApp
import com.example.lctracker.ui.theme.LctrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LctrackerTheme {
                LcTrackerApp()
            }
        }
    }
}