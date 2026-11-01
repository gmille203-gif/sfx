package com.sfx.viperglass.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.sfx.viperglass.ui.screens.*
import com.sfx.viperglass.ui.theme.ViPERGlassTheme
import dagger.hilt.android.AndroidEntryPoint

data class Tab(val label: String, val icon: ImageVector)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViPERGlassTheme {
                var tab by remember { mutableIntStateOf(0) }
                var master by remember { mutableStateOf(true) }
                var device by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    Tab("Home", Icons.Default.Home),
                    Tab("EQ", Icons.Default.Equalizer),
                    Tab("Lab", Icons.Default.Science),
                    Tab("Player", Icons.Default.MusicNote),
                    Tab("Presets", Icons.Default.Favorite)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            tabs.forEachIndexed { idx, t ->
                                NavigationBarItem(
                                    selected = tab == idx,
                                    onClick = { tab = idx },
                                    icon = { Icon(t.icon, null) },
                                    label = { Text(t.label) }
                                )
                            }
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad)) {
                        when (tab) {
                            0 -> HomeScreen(master, { master = !master }, device, { device = it })
                            1 -> EQScreen()
                            2 -> LabScreen()
                            3 -> PlayerScreen()
                            4 -> PresetsScreen()
                        }
                    }
                }
            }
        }
    }
}
