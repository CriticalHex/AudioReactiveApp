package com.audioreactive.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import com.audioreactive.ui.navigation.bars.SettingsTopBar
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeDefaults
import com.audioreactive.ui.viewmodel.state.LatticeLineDensity
import com.audioreactive.ui.viewmodel.state.LatticeState
import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode
import com.audioreactive.ui.viewmodel.state.VisualizerBarSoundMode
import com.audioreactive.ui.viewmodel.state.VisualizerDefaults
import com.audioreactive.ui.viewmodel.state.VisualizerState
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    latticeState: LatticeState,
    visualizerState: VisualizerState,
    onSetLatticeColorMode: (LatticeColorMode) -> Unit,
    onSetLatticeSolidColor: (Int) -> Unit,
    onSetGyrosDisabled: (Boolean) -> Unit,
    onSetLatticeDisabled: (Boolean) -> Unit,
    onSetBarColorMode: (VisualizerBarColorMode) -> Unit,
    onSetSolidBarColor: (Int) -> Unit,
    onSetBarsDisabled: (Boolean) -> Unit,
    onSetLatticeSpeed: (Float) -> Unit,
    onSetLatticeSensitivity: (Float) -> Unit,
    onSetLatticeLineDensity: (LatticeLineDensity) -> Unit,
    onSetBackgroundImage: () -> Unit,
    onRemoveBackgroundImage: () -> Unit,
    onSetBarRiseSpeed: (Float) -> Unit,
    onSetBarFallSpeed: (Float) -> Unit,
    onSetBarSensitivity: (Float) -> Unit,
    onSetBarSoundMode: (VisualizerBarSoundMode) -> Unit,
    onSetBarMaxHeight: (Float) -> Unit,
    onSetBarCount: (Int) -> Unit,
    onSetBarOpacity: (Float) -> Unit,
    onSetInvertGyroSpin: (Boolean) -> Unit,
    onSetInvertGyroHorizontal: (Boolean) -> Unit,
    onSetInvertGyroVertical: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
) {
    var backHandled by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Lattice", "Bars", "Gyro", "General")

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
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Gray,
                        text = { Text(label) }
                    )
                }
            }

            when (selectedTab) {
                0 -> LatticeTab(
                    latticeState = latticeState,
                    onSetLatticeColorMode = onSetLatticeColorMode,
                    onSetLatticeSolidColor = onSetLatticeSolidColor,
                    onSetLatticeSpeed = onSetLatticeSpeed,
                    onSetLatticeSensitivity = onSetLatticeSensitivity,
                    onSetLatticeLineDensity = onSetLatticeLineDensity
                )
                1 -> BarsTab(
                    visualizerState = visualizerState,
                    onSetBarColorMode = onSetBarColorMode,
                    onSetSolidBarColor = onSetSolidBarColor,
                    onSetBarRiseSpeed = onSetBarRiseSpeed,
                    onSetBarFallSpeed = onSetBarFallSpeed,
                    onSetBarSensitivity = onSetBarSensitivity,
                    onSetBarSoundMode = onSetBarSoundMode,
                    onSetBarMaxHeight = onSetBarMaxHeight,
                    onSetBarCount = onSetBarCount,
                    onSetBarOpacity = onSetBarOpacity
                )
                2 -> GyroTab(
                    latticeState = latticeState,
                    onSetGyrosDisabled = onSetGyrosDisabled,
                    onSetInvertGyroSpin = onSetInvertGyroSpin,
                    onSetInvertGyroHorizontal = onSetInvertGyroHorizontal,
                    onSetInvertGyroVertical = onSetInvertGyroVertical
                )
                3 -> GeneralTab(
                    latticeState = latticeState,
                    visualizerState = visualizerState,
                    onSetLatticeDisabled = onSetLatticeDisabled,
                    onSetBarsDisabled = onSetBarsDisabled,
                    onResetToDefaults = onResetToDefaults,
                    onSetBackgroundImage = onSetBackgroundImage,
                    onRemoveBackgroundImage = onRemoveBackgroundImage,
                )
            }
        }
    }
}

@Composable
private fun LatticeTab(
    latticeState: LatticeState,
    onSetLatticeColorMode: (LatticeColorMode) -> Unit,
    onSetLatticeSolidColor: (Int) -> Unit,
    onSetLatticeSpeed: (Float) -> Unit,
    onSetLatticeSensitivity: (Float) -> Unit,
    onSetLatticeLineDensity: (LatticeLineDensity) -> Unit
) {
    val scrollState = rememberScrollState()
    var latticeHexText by rememberSaveable { mutableStateOf("") }
    val latticeSelectedColor = remember(latticeState.solidColorArgb) {
        Color(latticeState.solidColorArgb)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Lattice Color")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChoiceButton(
                    label = "Default",
                    isSelected = latticeState.latticeColorMode == LatticeColorMode.DEFAULT,
                    onClick = { onSetLatticeColorMode(LatticeColorMode.DEFAULT) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    label = "Solid Color",
                    isSelected = latticeState.latticeColorMode == LatticeColorMode.SOLID,
                    onClick = { onSetLatticeColorMode(LatticeColorMode.SOLID) },
                    modifier = Modifier.weight(1f)
                )
            }
            ChoiceButton(
                label = "Rainbow",
                isSelected = latticeState.latticeColorMode == LatticeColorMode.DIMENSION_CYCLE,
                onClick = { onSetLatticeColorMode(LatticeColorMode.DIMENSION_CYCLE) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = when (latticeState.latticeColorMode) {
                    LatticeColorMode.DEFAULT -> "The lattice will use its animated default colors."
                    LatticeColorMode.SOLID -> "The lattice will use the selected solid color."
                    LatticeColorMode.DIMENSION_CYCLE -> "Each dimension cycles through the rainbow."
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )

            if (latticeState.latticeColorMode == LatticeColorMode.SOLID) {
                Text("Preview", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(latticeSelectedColor, RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HarmonyColorPicker(
                        modifier = Modifier.size(280.dp),
                        harmonyMode = ColorHarmonyMode.NONE,
                        onColorChanged = { hsv: HsvColor ->
                            onSetLatticeSolidColor(hsv.toColor().toArgb())
                        }
                    )
                }
                OutlinedTextField(
                    value = latticeHexText,
                    onValueChange = { newValue ->
                        latticeHexText = newValue
                        parseHexColor(newValue)?.let { color ->
                            onSetLatticeSolidColor(color.toArgb())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hex Color", color = Color.White) },
                    placeholder = { Text("#00FFFF", color = Color.Gray) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }

        SectionTitle("Lattice Animation")
        SettingsCard {
            SettingSliderRow(
                title = "Speed",
                value = latticeState.speed,
                valueRange = 0.05f..2f,
                valueLabel = String.format("%.2fx", latticeState.speed),
                defaultValue = LatticeDefaults.SPEED,
                defaultLabel = String.format("default %.2fx", LatticeDefaults.SPEED),
                onValueChange = onSetLatticeSpeed
            )
            SettingSliderRow(
                title = "Sensitivity",
                value = latticeState.sensitivity,
                valueRange = 0.1f..3f,
                valueLabel = String.format("%.2fx", latticeState.sensitivity),
                defaultValue = LatticeDefaults.SENSITIVITY,
                defaultLabel = String.format("default %.2fx", LatticeDefaults.SENSITIVITY),
                onValueChange = onSetLatticeSensitivity
            )
            Text(
                text = "Detail (number of edges drawn). Default: ${LatticeDefaults.LINE_DENSITY.name.lowercase()}",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChoiceButton(
                    label = "Low",
                    isSelected = latticeState.lineDensity == LatticeLineDensity.LOW,
                    onClick = { onSetLatticeLineDensity(LatticeLineDensity.LOW) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    label = "Medium",
                    isSelected = latticeState.lineDensity == LatticeLineDensity.MEDIUM,
                    onClick = { onSetLatticeLineDensity(LatticeLineDensity.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    label = "High",
                    isSelected = latticeState.lineDensity == LatticeLineDensity.HIGH,
                    onClick = { onSetLatticeLineDensity(LatticeLineDensity.HIGH) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

    }
}

@Composable
private fun BarsTab(
    visualizerState: VisualizerState,
    onSetBarColorMode: (VisualizerBarColorMode) -> Unit,
    onSetSolidBarColor: (Int) -> Unit,
    onSetBarRiseSpeed: (Float) -> Unit,
    onSetBarFallSpeed: (Float) -> Unit,
    onSetBarSensitivity: (Float) -> Unit,
    onSetBarSoundMode: (VisualizerBarSoundMode) -> Unit,
    onSetBarMaxHeight: (Float) -> Unit,
    onSetBarCount: (Int) -> Unit,
    onSetBarOpacity: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val barSelectedColor = remember(visualizerState.solidBarColorArgb) {
        Color(visualizerState.solidBarColorArgb)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Visualizer Bar Color")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChoiceButton(
                    label = "Default",
                    isSelected = visualizerState.barColorMode == VisualizerBarColorMode.DEFAULT,
                    onClick = { onSetBarColorMode(VisualizerBarColorMode.DEFAULT) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    label = "Solid Color",
                    isSelected = visualizerState.barColorMode == VisualizerBarColorMode.SOLID,
                    onClick = { onSetBarColorMode(VisualizerBarColorMode.SOLID) },
                    modifier = Modifier.weight(1f)
                )
            }
            ChoiceButton(
                label = "Rainbow Cycle",
                isSelected = visualizerState.barColorMode == VisualizerBarColorMode.RAINBOW_CYCLE,
                onClick = { onSetBarColorMode(VisualizerBarColorMode.RAINBOW_CYCLE) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = when (visualizerState.barColorMode) {
                    VisualizerBarColorMode.DEFAULT -> "The visualizer bars will use their default colors."
                    VisualizerBarColorMode.SOLID -> "The visualizer bars will use the selected solid color."
                    VisualizerBarColorMode.RAINBOW_CYCLE -> "The visualizer bars cycle through the rainbow over time."
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )

            if (visualizerState.barColorMode == VisualizerBarColorMode.SOLID) {
                Text("Preview", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(barSelectedColor, RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HarmonyColorPicker(
                        modifier = Modifier.size(280.dp),
                        harmonyMode = ColorHarmonyMode.NONE,
                        onColorChanged = { hsv: HsvColor ->
                            onSetSolidBarColor(hsv.toColor().toArgb())
                        }
                    )
                }
            }
        }

        SectionTitle("Bar Reaction")
        SettingsCard {
            SettingSliderRow(
                title = "Rise Speed",
                value = visualizerState.barRiseSpeed,
                valueRange = 0.1f..3f,
                valueLabel = String.format("%.2fx", visualizerState.barRiseSpeed),
                defaultValue = VisualizerDefaults.BAR_RISE_SPEED,
                defaultLabel = String.format("default %.2fx", VisualizerDefaults.BAR_RISE_SPEED),
                onValueChange = onSetBarRiseSpeed
            )
            SettingSliderRow(
                title = "Fall Speed",
                value = visualizerState.barFallSpeed,
                valueRange = 0.1f..3f,
                valueLabel = String.format("%.2fx", visualizerState.barFallSpeed),
                defaultValue = VisualizerDefaults.BAR_FALL_SPEED,
                defaultLabel = String.format("default %.2fx", VisualizerDefaults.BAR_FALL_SPEED),
                onValueChange = onSetBarFallSpeed
            )
            SettingSliderRow(
                title = "Sensitivity",
                value = visualizerState.barSensitivity,
                valueRange = 0.1f..3f,
                valueLabel = String.format("%.2fx", visualizerState.barSensitivity),
                defaultValue = VisualizerDefaults.BAR_SENSITIVITY,
                defaultLabel = String.format("default %.2fx", VisualizerDefaults.BAR_SENSITIVITY),
                onValueChange = onSetBarSensitivity
            )
            Text(
                text = "Rise Speed controls how quickly bars climb when audio gets louder. Fall Speed controls how quickly they drop when it gets quieter. Sensitivity scales the response to audio.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionTitle("Bar Layout")
        SettingsCard {
            SettingSliderRow(
                title = "Max Height",
                value = visualizerState.barMaxHeight,
                valueRange = 0.1f..1f,
                valueLabel = String.format("%.0f%%", visualizerState.barMaxHeight * 100f),
                defaultValue = VisualizerDefaults.BAR_MAX_HEIGHT,
                defaultLabel = String.format(
                    "default %.0f%%",
                    VisualizerDefaults.BAR_MAX_HEIGHT * 100f
                ),
                onValueChange = onSetBarMaxHeight
            )
            SettingSliderRow(
                title = "Bar Count",
                value = visualizerState.barCount.toFloat(),
                valueRange = VisualizerDefaults.BAR_COUNT_MIN.toFloat()..
                    VisualizerDefaults.BAR_COUNT_MAX.toFloat(),
                valueLabel = "${visualizerState.barCount}",
                defaultValue = VisualizerDefaults.BAR_COUNT.toFloat(),
                defaultLabel = "default ${VisualizerDefaults.BAR_COUNT}",
                onValueChange = { onSetBarCount(it.toInt()) }
            )
            SettingSliderRow(
                title = "Opacity",
                value = visualizerState.barOpacity,
                valueRange = 0.1f..1f,
                valueLabel = String.format("%.0f%%", visualizerState.barOpacity * 100f),
                defaultValue = VisualizerDefaults.BAR_OPACITY,
                defaultLabel = String.format(
                    "default %.0f%%",
                    VisualizerDefaults.BAR_OPACITY * 100f
                ),
                onValueChange = onSetBarOpacity
            )
        }

        SectionTitle("Sound Mode")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChoiceButton(
                    label = "Flat",
                    isSelected = visualizerState.barSoundMode == VisualizerBarSoundMode.FLAT,
                    onClick = { onSetBarSoundMode(VisualizerBarSoundMode.FLAT) },
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    label = "Balanced",
                    isSelected = visualizerState.barSoundMode == VisualizerBarSoundMode.BALANCED,
                    onClick = { onSetBarSoundMode(VisualizerBarSoundMode.BALANCED) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = when (visualizerState.barSoundMode) {
                    VisualizerBarSoundMode.FLAT ->
                        "Flat: bars show raw FFT magnitudes. Lows tend to dominate because most musical energy lives there."
                    VisualizerBarSoundMode.BALANCED ->
                        "Balanced: highs are boosted so trebles can reach similar height to bass when present in the mix."
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "default ${VisualizerDefaults.BAR_SOUND_MODE.name.lowercase().replaceFirstChar { it.uppercase() }}",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun GyroTab(
    latticeState: LatticeState,
    onSetGyrosDisabled: (Boolean) -> Unit,
    onSetInvertGyroSpin: (Boolean) -> Unit,
    onSetInvertGyroHorizontal: (Boolean) -> Unit,
    onSetInvertGyroVertical: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Gyroscope")
        SettingsCard {
            SettingSwitchRow(
                title = "Disable Gyroscope",
                checked = latticeState.disableGyros,
                onCheckedChange = onSetGyrosDisabled
            )
            Text(
                text = "Tilt and rotate your device to move the lattice. Each axis below can be inverted if it feels backwards. (Defaults are pre-tuned to match natural device motion.)",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionTitle("Axis Inversion")
        SettingsCard {
            SettingSwitchRow(
                title = "Invert spin (rotate flat)",
                checked = latticeState.invertGyroSpin,
                onCheckedChange = onSetInvertGyroSpin,
                defaultLabel = "default ${if (LatticeDefaults.INVERT_GYRO_SPIN) "on" else "off"}"
            )
            SettingSwitchRow(
                title = "Invert horizontal tilt",
                checked = latticeState.invertGyroHorizontal,
                onCheckedChange = onSetInvertGyroHorizontal,
                defaultLabel = "default ${if (LatticeDefaults.INVERT_GYRO_HORIZONTAL) "on" else "off"}"
            )
            SettingSwitchRow(
                title = "Invert vertical tilt",
                checked = latticeState.invertGyroVertical,
                onCheckedChange = onSetInvertGyroVertical,
                defaultLabel = "default ${if (LatticeDefaults.INVERT_GYRO_VERTICAL) "on" else "off"}"
            )
        }
    }
}

@Composable
private fun GeneralTab(
    latticeState: LatticeState,
    visualizerState: VisualizerState,
    onSetLatticeDisabled: (Boolean) -> Unit,
    onSetBarsDisabled: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
    onSetBackgroundImage: () -> Unit,
    onRemoveBackgroundImage: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showHowToDialog by rememberSaveable { mutableStateOf(false) }
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Display Options")
        SettingsCard {
            SettingSwitchRow(
                title = "Disable Lattice",
                checked = latticeState.disableLattice,
                onCheckedChange = onSetLatticeDisabled
            )
            SettingSwitchRow(
                title = "Disable Bars",
                checked = visualizerState.disableBars,
                onCheckedChange = onSetBarsDisabled
            )
        }

        Button(
            onClick = { showHowToDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) { Text("How To Use") }

        Button(
            onClick = onSetBackgroundImage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Set Background Image") }

        Button(
            onClick = onRemoveBackgroundImage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Remove Background Image") }

        Button(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB00020),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Reset to Defaults") }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = Color(0xFF111111),
            title = { Text("Reset to Defaults", color = Color.White) },
            text = {
                Text(
                    "This will restore all settings (lattice color, bar reaction, gyroscope inversion, etc.) to their defaults. Continue?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onResetToDefaults()
                }) { Text("Reset", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showHowToDialog) {
        AlertDialog(
            onDismissRequest = { showHowToDialog = false },
            containerColor = Color(0xFF111111),
            title = { Text("How To Use", color = Color.White) },
            text = {
                Column(
                    modifier = Modifier
                        .height(320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("● Tap the screen on the visualizer to show the controls.", color = Color.White)
                    Text("● Use the top controls to start capture, pick audio, or open settings.", color = Color.White)
                    Text("● Lattice tab: change lattice color, animation speed, audio sensitivity, and detail.", color = Color.White)
                    Text("● Bars tab: change bar color, reaction speed, and reaction sensitivity.", color = Color.White)
                    Text("● Gyro tab: enable/disable the gyroscope and invert any axis that feels backwards.", color = Color.White)
                    Text("● General tab: disable visualizers, see this help, or reset everything to defaults.", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHowToDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
    )
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content
        )
    }
}

@Composable
private fun ChoiceButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.DarkGray,
            contentColor = if (isSelected) Color.Black else Color.White
        )
    ) { Text(label) }
}

@Composable
private fun SettingSliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    defaultValue: Float,
    defaultLabel: String? = null,
    onValueChange: (Float) -> Unit
) {
    val span = valueRange.endInclusive - valueRange.start
    val snapWindow = span * 0.04f
    val defaultFraction = ((defaultValue - valueRange.start) / span).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueLabel,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = value,
                onValueChange = { newValue ->
                    val snapped = if (abs(newValue - defaultValue) < snapWindow) {
                        defaultValue
                    } else {
                        newValue
                    }
                    onValueChange(snapped)
                },
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.DarkGray
                )
            )
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 10.dp)
            ) {
                val x = size.width * defaultFraction
                val midY = size.height / 2f
                val halfLen = 7.dp.toPx()
                drawLine(
                    color = Color(0xFFFFC107),
                    start = Offset(x, midY - halfLen),
                    end = Offset(x, midY + halfLen),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        if (defaultLabel != null) {
            Text(
                text = defaultLabel,
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    defaultLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White)
            if (defaultLabel != null) {
                Text(
                    text = defaultLabel,
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
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

private fun parseHexColor(input: String): Color? {
    val cleaned = input.trim().removePrefix("#")
    if (cleaned.length != 6 && cleaned.length != 8) return null
    return try {
        val argb = if (cleaned.length == 6) {
            "FF$cleaned".toLong(16).toInt()
        } else {
            cleaned.toLong(16).toInt()
        }
        Color(argb)
    } catch (_: NumberFormatException) {
        null
    }
}
