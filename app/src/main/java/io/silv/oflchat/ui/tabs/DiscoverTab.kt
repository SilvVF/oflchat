package io.silv.oflchat.ui.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.oflchat.R
import io.silv.oflchat.ui.screens.DiscoverScreen
import io.silv.oflchat.ui.screens.PermissionsScreen

object DiscoverTab: Tab {

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 1u,
            title = stringResource(R.string.discover_tab_title),
            icon = rememberVectorPainter(image = Icons.Rounded.LocationOn)
        )

    @Composable
    override fun Content() {
        Navigator(
            listOf(
                DiscoverScreen(),
                PermissionsScreen()
            )
        ) {
            FadeTransition(it)
        }
    }
}


