package com.rnote.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rnote.app.navigation.RNoteNavGraph
import com.rnote.app.ui.theme.RNoteTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RNoteTheme {
                val prefs = getSharedPreferences("rnote_prefs", Context.MODE_PRIVATE)
                val isOnboardingCompleted by remember {
                    mutableStateOf(prefs.getBoolean("onboarding_completed", false))
                }

                RNoteNavGraph(
                    isOnboardingCompleted = isOnboardingCompleted,
                    onOnboardingComplete = {
                        prefs.edit().putBoolean("onboarding_completed", true).apply()
                    }
                )
            }
        }
    }
}
