package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import com.example.ui.MindDecoderViewModel
import com.example.ui.screens.DecoderScreen
import com.example.ui.screens.HeadsetScreen
import com.example.ui.screens.HistoryAndTrainerScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.theme.MindDecoderTheme

enum class NavTab(val label: String, val icon: ImageVector, val tag: String) {
    TELEMETRY("Telemetry", Icons.Default.GraphicEq, "tab_telemetry"),
    DECODER("Decoder", Icons.Default.Psychology, "tab_decoder"),
    HEADSET("Headset", Icons.Default.Headset, "tab_headset"),
    TRAINER("Trainer", Icons.Default.SelfImprovement, "tab_trainer")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MindDecoderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindDecoderTheme {
                var selectedTab by remember { mutableStateOf(NavTab.TELEMETRY) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .border(width = 1.dp, color = Color(0x1AFFFFFF))
                                .testTag("bottom_navigation_bar"),
                            containerColor = Color(0xFF0D0D0D),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            NavTab.values().forEach { tab ->
                                val isSelected = (selectedTab == tab)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = if (isSelected) Color.Black else Color.Gray
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            NavTab.TELEMETRY -> TelemetryScreen(viewModel = viewModel)
                            NavTab.DECODER -> DecoderScreen(viewModel = viewModel)
                            NavTab.HEADSET -> HeadsetScreen(viewModel = viewModel)
                            NavTab.TRAINER -> HistoryAndTrainerScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
