package io.silv.oflchat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen

object SettingsScreen: Screen {

    @Composable
    override fun Content() {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Settings", Modifier.align(Alignment.Center))
        }
    }
}