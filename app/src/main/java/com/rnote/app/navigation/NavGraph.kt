package com.rnote.app.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rnote.app.RNoteApplication
import com.rnote.app.ui.components.PermissionBottomSheet
import com.rnote.app.ui.note.NoteScreen
import com.rnote.app.ui.note.NoteViewModel
import com.rnote.app.ui.notelist.NoteListScreen
import com.rnote.app.ui.notelist.NoteListViewModel
import com.rnote.app.ui.onboarding.OnboardingScreen
import com.rnote.app.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val NOTE_LIST = "note_list"
    const val NOTE_CREATE = "note_create"
    const val NOTE_EDIT = "note_edit/{noteId}"

    fun noteEdit(noteId: String) = "note_edit/$noteId"
}

@Composable
fun RNoteNavGraph(
    isOnboardingCompleted: Boolean,
    onOnboardingComplete: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as RNoteApplication

    val startDestination = Routes.SPLASH

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    val destination = if (isOnboardingCompleted) {
                        Routes.NOTE_LIST
                    } else {
                        Routes.ONBOARDING
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            var showPermissionSheet by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ ->
                onOnboardingComplete()
                navController.navigate(Routes.NOTE_CREATE) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            }

            OnboardingScreen(
                onFinished = {
                    showPermissionSheet = true
                }
            )

            if (showPermissionSheet) {
                PermissionBottomSheet(
                    onAllow = {
                        showPermissionSheet = false
                        if (Build.VERSION.SDK_INT <= 28) {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            onOnboardingComplete()
                            navController.navigate(Routes.NOTE_CREATE) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    },
                    onDeny = {
                        showPermissionSheet = false
                        onOnboardingComplete()
                        navController.navigate(Routes.NOTE_CREATE) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.NOTE_CREATE) {
            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModel.Factory(application.noteRepository)
            )
            LaunchedEffect(Unit) {
                noteViewModel.loadNote(null)
            }

            NoteScreen(
                viewModel = noteViewModel,
                onNavigateBack = {
                    navController.navigate(Routes.NOTE_LIST) {
                        popUpTo(Routes.NOTE_CREATE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.NOTE_EDIT,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModel.Factory(application.noteRepository)
            )
            LaunchedEffect(noteId) {
                noteViewModel.loadNote(noteId)
            }

            NoteScreen(
                viewModel = noteViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NOTE_LIST) {
            val noteListViewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModel.Factory(application.noteRepository)
            )

            NoteListScreen(
                viewModel = noteListViewModel,
                onNewNote = {
                    navController.navigate(Routes.NOTE_CREATE)
                },
                onNoteClick = { noteId ->
                    navController.navigate(Routes.noteEdit(noteId))
                }
            )
        }
    }
}
