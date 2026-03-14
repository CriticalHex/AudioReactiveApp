package com.audioreactive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onColorSelected: (Color) -> Unit = {}
) {
    var hue by rememberSaveable { mutableFloatStateOf(180f) }
    var saturation by rememberSaveable { mutableFloatStateOf(1f) }
    var value by rememberSaveable { mutableFloatStateOf(1f) }

    val selectedColor = remember(hue, saturation, value) {
        Color.hsv(
            hue = hue,
            saturation = saturation,
            value = value
        )
    }

    LaunchedEffect(selectedColor) {
        onColorSelected(selectedColor)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Visualizer Color",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Preview")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                color = selectedColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                    )

                    Column {
                        Text("Hue: ${hue.toInt()}°")
                        Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            valueRange = 0f..360f
                        )
                    }

                    Column {
                        Text("Saturation: ${(saturation * 100).toInt()}%")
                        Slider(
                            value = saturation,
                            onValueChange = { saturation = it },
                            valueRange = 0f..1f
                        )
                    }

                    Column {
                        Text("Brightness: ${(value * 100).toInt()}%")
                        Slider(
                            value = value,
                            onValueChange = { value = it },
                            valueRange = 0f..1f
                        )
                    }

                    Text(
                        text = "Selected color will be saved and used later.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
