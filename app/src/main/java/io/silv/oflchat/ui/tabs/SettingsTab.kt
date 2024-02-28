package io.silv.oflchat.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.silv.oflchat.R

object SettingsTab: Tab {

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 2u,
            title = stringResource(R.string.settings_tab_title),
            icon = rememberVectorPainter(image = Icons.Rounded.Settings)
        )
    @Composable
    override fun Content() {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Settings", Modifier.align(Alignment.Center))
        }
    }
}