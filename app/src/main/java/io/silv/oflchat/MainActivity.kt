package io.silv.oflchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.ui.screens.ConversationsScreen
import io.silv.oflchat.ui.screens.PermissionsScreen
import io.silv.oflchat.ui.theme.OflchatTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            OflchatTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigator(
                        listOf(
                            ConversationsScreen,
                            PermissionsScreen()
                        )
                    ) { navigator ->
                        CrossfadeTransition(navigator)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectionHelper.startDiscovery()
    }

    override fun onStop() {
        super.onStop()
        ConnectionHelper.stopDiscovery()
    }
}

@Composable
private fun CrossfadeTransition(
    navigator: Navigator,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "Crossfade",
    modifier: Modifier = Modifier,
    content: @Composable (Screen) -> Unit = { it.Content() }
) {
    Crossfade(
        targetState = navigator.lastItem,
        animationSpec = animationSpec,
        modifier = modifier,
        label = label
    ) { screen ->
        navigator.saveableState("transition", screen) {
            content(screen)
        }
    }
}

@Composable
private fun TabCrossFadeTransition(
    navigator: TabNavigator,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "Crossfade",
    content: @Composable (Tab) -> Unit = { it.Content() }
) {

    navigator.saveableState("currentTab") {
        Crossfade(
            targetState = navigator.current,
            animationSpec = animationSpec,
            modifier = modifier,
            label = label
        ) { tab ->
            content(tab)
        }
    }
}
