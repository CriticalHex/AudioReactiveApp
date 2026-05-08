package com.audioreactive.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeDefaults
import com.audioreactive.ui.viewmodel.state.LatticeLineDensity
import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode
import com.audioreactive.ui.viewmodel.state.VisualizerBarSoundMode
import com.audioreactive.ui.viewmodel.state.VisualizerDefaults
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "settings")
data class Settings(
    val latticeColorMode: LatticeColorMode = LatticeDefaults.COLOR_MODE,
    val solidColorArgb: Int = LatticeDefaults.SOLID_COLOR_ARGB,
    val disableLattice: Boolean = LatticeDefaults.DISABLE_LATTICE,
    val disableGyros: Boolean = LatticeDefaults.DISABLE_GYROS,
    val speed: Float = LatticeDefaults.SPEED,
    val sensitivity: Float = LatticeDefaults.SENSITIVITY,
    val lineDensity: LatticeLineDensity = LatticeDefaults.LINE_DENSITY,
    val invertGyroSpin: Boolean = LatticeDefaults.INVERT_GYRO_SPIN,
    val invertGyroHorizontal: Boolean = LatticeDefaults.INVERT_GYRO_HORIZONTAL,
    val invertGyroVertical: Boolean = LatticeDefaults.INVERT_GYRO_VERTICAL,
    val customImage: Boolean = VisualizerDefaults.CUSTOM_IMAGE,
    val barColorMode: VisualizerBarColorMode = VisualizerDefaults.BAR_COLOR_MODE,
    val solidBarColorArgb: Int = VisualizerDefaults.SOLID_BAR_COLOR_ARGB,
    val disableBars: Boolean = VisualizerDefaults.DISABLE_BARS,
    val barRiseSpeed: Float = VisualizerDefaults.BAR_RISE_SPEED,
    val barFallSpeed: Float = VisualizerDefaults.BAR_FALL_SPEED,
    val barSensitivity: Float = VisualizerDefaults.BAR_SENSITIVITY,
    val barSoundMode: VisualizerBarSoundMode = VisualizerDefaults.BAR_SOUND_MODE,
    val barMaxHeight: Float = VisualizerDefaults.BAR_MAX_HEIGHT,
    val barCount: Int = VisualizerDefaults.BAR_COUNT,
    val barOpacity: Float = VisualizerDefaults.BAR_OPACITY,
    @PrimaryKey val id: Int = 1,
)
