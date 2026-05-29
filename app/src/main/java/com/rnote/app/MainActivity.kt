package com.rnote.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.rnote.app.navigation.RNoteNavGraph
import com.rnote.app.ui.theme.RNoteTheme

class MainActivity : ComponentActivity() {

    private val showUpdateDialog = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForAppUpdate()

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

                if (showUpdateDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog.value = false },
                        title = {
                            Text(text = stringResource(R.string.update_available_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.update_available_message))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showUpdateDialog.value = false
                                    openGooglePlayStore()
                                }
                            ) {
                                Text(text = stringResource(R.string.update_now))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showUpdateDialog.value = false }) {
                                Text(text = stringResource(R.string.later))
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                showUpdateDialog.value = true
            }
        }
    }

    private fun openGooglePlayStore() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            setPackage("com.android.vending")
        }

        runCatching {
            startActivity(marketIntent)
        }.onFailure {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(webIntent)
        }
    }
}
