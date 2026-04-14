package com.audioreactive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.audioreactive.ui.navigation.bars.SettingsTopBar
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.intent.LatticeIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    latticeViewModel: LatticeViewModel,
    visualizerViewModel: VisualizerViewModel
) {
    val latticeState by latticeViewModel.stateFlow.collectAsState()
    val visualizerState by visualizerViewModel.stateFlow.collectAsState()
    val scrollState = rememberScrollState()

    var backHandled by rememberSaveable { mutableStateOf(false) }

    val latticeSelectedColor = remember(latticeState.solidColorArgb) {
        Color(latticeState.solidColorArgb)
    }

    val barSelectedColor = remember(visualizerState.solidBarColorArgb) {
        Color(visualizerState.solidBarColorArgb)
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            SettingsTopBar(
                onBack = {
                    if (!backHandled) {
                        backHandled = true
                        onBack()
                    }
                },
                isBackEnabled = !backHandled
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Lattice Color",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                latticeViewModel.dispatcher.invoke(
                                    LatticeIntent.SetLatticeColorMode(LatticeColorMode.DEFAULT)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (latticeState.latticeColorMode == LatticeColorMode.DEFAULT) Color.White else Color.DarkGray,
                                contentColor =
                                    if (latticeState.latticeColorMode == LatticeColorMode.DEFAULT) Color.Black else Color.White
                            )
                        ) {
                            Text("Default")
                        }

                        Button(
                            onClick = {
                                latticeViewModel.dispatcher.invoke(
                                    LatticeIntent.SetLatticeColorMode(LatticeColorMode.SOLID)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (latticeState.latticeColorMode == LatticeColorMode.SOLID) Color.White else Color.DarkGray,
                                contentColor =
                                    if (latticeState.latticeColorMode == LatticeColorMode.SOLID) Color.Black else Color.White
                            )
                        ) {
                            Text("Solid Color")
                        }
                    }

                    Text(
                        text = if (latticeState.latticeColorMode == LatticeColorMode.DEFAULT) {
                            "The lattice will use its animated default colors."
                        } else {
                            "The lattice will use the selected solid color."
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (latticeState.latticeColorMode == LatticeColorMode.SOLID) {
                        Text(
                            text = "Preview",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = latticeSelectedColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HarmonyColorPicker(
                                modifier = Modifier.size(280.dp),
                                harmonyMode = ColorHarmonyMode.NONE,
                                onColorChanged = { hsvColor: HsvColor ->
                                    latticeViewModel.dispatcher.invoke(
                                        LatticeIntent.SetSolidColor(
                                            hsvColor.toColor().toArgb()
                                        )
                                    )
                                }
                            )
                        }

                        Text(
                            text = "Tap the color wheel to choose your solid lattice color.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text(
                text = "Visualizer Bar Color",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                visualizerViewModel.dispatcher.invoke(
                                    VisualizerIntent.SetBarColorMode(VisualizerBarColorMode.DEFAULT)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (visualizerState.barColorMode == VisualizerBarColorMode.DEFAULT) Color.White else Color.DarkGray,
                                contentColor =
                                    if (visualizerState.barColorMode == VisualizerBarColorMode.DEFAULT) Color.Black else Color.White
                            )
                        ) {
                            Text("Default")
                        }

                        Button(
                            onClick = {
                                visualizerViewModel.dispatcher.invoke(
                                    VisualizerIntent.SetBarColorMode(VisualizerBarColorMode.SOLID)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (visualizerState.barColorMode == VisualizerBarColorMode.SOLID) Color.White else Color.DarkGray,
                                contentColor =
                                    if (visualizerState.barColorMode == VisualizerBarColorMode.SOLID) Color.Black else Color.White
                            )
                        ) {
                            Text("Solid Color")
                        }
                    }

                    Text(
                        text = if (visualizerState.barColorMode == VisualizerBarColorMode.DEFAULT) {
                            "The visualizer bars will use their default colors."
                        } else {
                            "The visualizer bars will use the selected solid color."
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (visualizerState.barColorMode == VisualizerBarColorMode.SOLID) {
                        Text(
                            text = "Preview",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    color = barSelectedColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HarmonyColorPicker(
                                modifier = Modifier.size(280.dp),
                                harmonyMode = ColorHarmonyMode.NONE,
                                onColorChanged = { hsvColor: HsvColor ->
                                    visualizerViewModel.dispatcher.invoke(
                                        VisualizerIntent.SetSolidBarColor(
                                            hsvColor.toColor().toArgb()
                                        )
                                    )
                                }
                            )
                        }

                        Text(
                            text = "Tap the color wheel to choose your solid bar color.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text(
                text = "Display Options",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SettingSwitchRow(
                        title = "Disable Gyros",
                        checked = latticeState.disableGyros,
                        onCheckedChange = {
                            latticeViewModel.dispatcher.invoke(
                                LatticeIntent.SetGyrosDisabled(it)
                            )
                        }
                    )

                    SettingSwitchRow(
                        title = "Disable Lattice",
                        checked = latticeState.disableLattice,
                        onCheckedChange = {
                            latticeViewModel.dispatcher.invoke(
                                LatticeIntent.SetLatticeDisabled(it)
                            )
                        }
                    )

                    SettingSwitchRow(
                        title = "Disable Bars",
                        checked = visualizerState.disableBars,
                        onCheckedChange = {
                            visualizerViewModel.dispatcher.invoke(
                                VisualizerIntent.SetBarsDisabled(it)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
