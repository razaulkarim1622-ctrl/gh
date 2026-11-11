package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NearbyChatScreen
import com.example.ui.screens.NewChatScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.PrivacySecurityScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.JihadChatTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ChatViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()
            val lang by viewModel.language.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val feedbackMsg by viewModel.userFeedbackMessage.collectAsState()

            LaunchedEffect(feedbackMsg) {
                feedbackMsg?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearFeedback()
                }
            }

            JihadChatTheme(darkTheme = isDark) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        onBackPressed = {
                            if (!viewModel.popBackStack()) {
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    currentScreen: AppScreen,
    viewModel: ChatViewModel,
    onBackPressed: () -> Unit
) {
    val lang by viewModel.language.collectAsState()

    BackHandler(enabled = currentScreen !is AppScreen.Home && currentScreen !is AppScreen.Splash) {
        onBackPressed()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is AppScreen.Splash -> SplashScreen(viewModel, lang)
            is AppScreen.Welcome -> WelcomeScreen(viewModel, lang)
            is AppScreen.Login -> LoginScreen(viewModel, lang)
            is AppScreen.Otp -> OtpVerificationScreen(screen.phoneNumber, viewModel, lang)
            is AppScreen.Register -> RegistrationScreen(screen.phoneNumber, viewModel, lang)
            is AppScreen.Home -> HomeScreen(viewModel, lang)
            is AppScreen.ChatDetail -> ChatDetailScreen(
                conversationId = screen.conversationId,
                title = screen.title,
                isGroup = screen.isGroup,
                isNearby = screen.isNearby,
                viewModel = viewModel,
                lang = lang
            )
            is AppScreen.GroupChat -> ChatDetailScreen(
                conversationId = screen.conversationId,
                title = screen.title,
                isGroup = true,
                isNearby = false,
                viewModel = viewModel,
                lang = lang
            )
            is AppScreen.Contacts -> ContactsScreen(viewModel, lang)
            is AppScreen.NewChat -> NewChatScreen(viewModel, lang)
            is AppScreen.Profile -> ProfileScreen(viewModel, lang)
            is AppScreen.Settings -> SettingsScreen(viewModel, lang)
            is AppScreen.Privacy -> PrivacySecurityScreen(viewModel, lang)
            is AppScreen.NearbyChat -> NearbyChatScreen(viewModel, lang)
        }
    }
}
