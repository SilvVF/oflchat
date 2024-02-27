package io.silv.oflchat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.CrossfadeTransition
import io.silv.oflchat.R
import io.silv.oflchat.ui.StringProvider
import io.silv.oflchat.viewmodels.DiscoverScreenModel

object DiscoverTab: Tab {

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            title = stringResource(R.string.discover_tab_title),
            icon = rememberVectorPainter(image = Icons.Rounded.LocationOn)
        )
    @Composable
    override fun Content() {
        Navigator(
            listOf(
                DiscoverScreen,
                PermissionsScreen
            )
        ) {
            CrossfadeTransition(it)
        }
    }
}


object DiscoverScreen: Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val screenModel = rememberScreenModel { DiscoverScreenModel(stringProvider = StringProvider.create(context)) }

        DisposableEffect(Unit) {
            screenModel.discover()

            onDispose { screenModel.stopDiscovery() }
        }

        DiscoverScreenContent(
            snackbarHostStateProvider = { screenModel.snackbarHostState }
        )
    }
}

@Composable
private fun DiscoverScreenContent(
    snackbarHostStateProvider: () -> SnackbarHostState,
) {

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostStateProvider()) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {

        }
    }
}