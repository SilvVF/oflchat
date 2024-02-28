package io.silv.oflchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import io.silv.oflchat.helpers.ConnectionHelper
import io.silv.oflchat.helpers.PreferenceHelper
import io.silv.oflchat.ui.tabs.ConversationsTab
import io.silv.oflchat.ui.tabs.DiscoverTab
import io.silv.oflchat.ui.tabs.SettingsTab
import io.silv.oflchat.ui.theme.OflchatTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DisposableEffect(Unit) {
                lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        PreferenceHelper.alwaysAdvertise.changes()
                            .collectLatest { advertise ->
                                if (advertise) {
                                    ConnectionHelper.advertise()
                                } else {
                                    ConnectionHelper.stopAdvertising()
                                }
                            }
                    }
                }
                onDispose { ConnectionHelper.stopAdvertising() }
            }


            OflchatTheme {
                // A surface container using the 'background' color from the theme
                TabNavigator(
                    ConversationsTab
                ) { tabNavigator ->
                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val tabs = remember { listOf(ConversationsTab, DiscoverTab, SettingsTab) }

                            NavigationBar(Modifier.height(72.dp)) {
                                tabs.fastForEach { tab ->
                                    NavigationBarItem(
                                        selected = tabNavigator.current == tab,
                                        onClick = { tabNavigator.current = tab },
                                        icon = {
                                            Icon(
                                                painter = tab.options.icon ?: return@NavigationBarItem,
                                                contentDescription = tab.options.title
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Box(
                                Modifier
                                    .padding(paddingValues)
                                    .consumeWindowInsets(paddingValues)
                            ) {
                                TabCrossFadeTransition(tabNavigator)
                            }
                        }
                    }
                }
            }
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
