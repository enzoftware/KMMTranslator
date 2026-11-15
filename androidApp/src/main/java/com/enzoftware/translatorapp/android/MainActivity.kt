package com.enzoftware.translatorapp.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.presentation.Route
import com.enzoftware.translatorapp.android.core.presentation.StartupTheme
import com.enzoftware.translatorapp.android.settings.presentation.SettingsScreen
import com.enzoftware.translatorapp.android.settings.presentation.SettingsViewModel
import com.enzoftware.translatorapp.android.settings.presentation.appVersionInfo
import com.enzoftware.translatorapp.android.translate.presentation.AndroidTranslateViewModel
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreen
import com.enzoftware.translatorapp.android.voice_to_text.presentation.AndroidVoiceToTextViewModel
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoiceToTextRoute
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val startupTheme = StartupTheme(this)
        // Pre-Android 12 the splash is drawn in-app from this theme; 12+ uses the app night mode.
        if (startupTheme.isDark) setTheme(R.style.AppTheme_Starting_Dark)
        // Keep the splash up until the saved theme is known, so a Dark user never sees a Light frame.
        installSplashScreen().setKeepOnScreenCondition { !settingsViewModel.isLoaded.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLoaded by settingsViewModel.isLoaded.collectAsStateWithLifecycle()
            if (!isLoaded) return@setContent
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val isDark = settings.theme.isDark

            LaunchedEffect(isDark) { startupTheme.remember(isDark) }

            // The app theme is chosen in Settings (not by the system), so the system bar icons follow it.
            DisposableEffect(isDark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            CozyTheme(darkTheme = isDark) {
                TranslateRoot(settingsViewModel = settingsViewModel)
            }
        }
    }
}

private const val VOICE_RESULT_KEY = "voiceResult"

@Composable
fun TranslateRoot(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Route.Translate,
    ) {
        composable<Route.Translate> { entry ->
            val viewModel = hiltViewModel<AndroidTranslateViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val voiceResult by entry.savedStateHandle
                .getStateFlow<String?>(VOICE_RESULT_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(voiceResult) {
                voiceResult?.let { result ->
                    viewModel.onEvent(TranslateEvent.SubmitVoiceResult(result))
                    entry.savedStateHandle[VOICE_RESULT_KEY] = null
                }
            }

            TranslateScreen(
                state = state,
                settings = settings,
                onEvent = { event ->
                    when (event) {
                        is TranslateEvent.RecordAudio -> entry.ifResumed {
                            navController.navigate(Route.VoiceToText(state.fromLanguage.language.code))
                        }

                        else -> viewModel.onEvent(event)
                    }
                },
                onOpenSettings = { entry.ifResumed { navController.navigate(Route.Settings) } },
            )
        }

        composable<Route.VoiceToText>(
            enterTransition = { slideInVertically(CozyMotion.cozy()) { it / 3 } + fadeIn() },
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically(CozyMotion.cozy()) { it / 3 } + fadeOut() },
        ) { entry ->
            val route = entry.toRoute<Route.VoiceToText>()
            val viewModel = hiltViewModel<AndroidVoiceToTextViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            VoiceToTextRoute(
                state = state,
                languageCode = route.languageCode,
                onResult = { result ->
                    entry.ifResumed {
                        navController.previousBackStackEntry?.savedStateHandle?.set(VOICE_RESULT_KEY, result)
                        navController.popBackStack()
                    }
                },
                onEvent = { event ->
                    when (event) {
                        is VoiceToTextEvent.Close -> entry.ifResumed { navController.popBackStack() }
                        else -> viewModel.onEvent(event)
                    }
                },
            )
        }

        composable<Route.Settings>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, CozyMotion.cozy()) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, CozyMotion.cozy()) },
        ) { entry ->
            val context = LocalContext.current
            val versionInfo = remember { context.appVersionInfo() }
            SettingsScreen(
                settings = settings,
                versionInfo = versionInfo,
                onUserNameChange = settingsViewModel::onUserNameChange,
                onThemeChange = settingsViewModel::onThemeChange,
                onBack = { entry.ifResumed { navController.popBackStack() } },
            )
        }
    }
}

/**
 * Runs [action] only while this destination is the resumed one. Double taps and taps during an
 * exit transition would otherwise navigate twice: pushing a duplicate screen, or popping the start
 * destination and leaving an empty NavHost.
 */
private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}
