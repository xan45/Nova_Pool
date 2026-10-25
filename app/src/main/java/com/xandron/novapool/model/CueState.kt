package com.xandron.novapool.model

import com.xandron.novapool.rendering.Vector3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CueState(
    var angle: Float = (PI / 2.0).toFloat(), // Radians: 0 points right (+X), PI/2 points up (+Y)
    var power: Float = 0f, // 0.0 to 1.0
    var isCharging: Boolean = false,
    var isStriking: Boolean = false,
    var strikeProgress: Float = 0f, // 0.0 to 1.0 animation progress
    var spinOffset: Vector3 = Vector3(0f, 0f, 0f), // X: english (-1 to 1), Y: top/back spin (-1 to 1)
    var elevationAngle: Float = 0.15f, // Radians elevation
    var cueLength: Float = 320f
) {
    fun getDirectionVector(): Vector3 {
        return Vector3(
            cos(angle.toDouble()).toFloat(),
            sin(angle.toDouble()).toFloat(),
            0f
        ).normalize()
    }
}
