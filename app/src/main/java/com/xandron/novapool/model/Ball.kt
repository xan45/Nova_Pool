package com.xandron.novapool.model

import androidx.compose.ui.graphics.Color
import com.xandron.novapool.rendering.Vector3

enum class BallType {
    CUE,
    SOLID,
    EIGHT,
    STRIPE
}

enum class BallState {
    ACTIVE,
    POCKETING,
    POTTED
}

data class Ball(
    val id: Int,
    val number: Int,
    val type: BallType,
    var position: Vector3,
    var velocity: Vector3 = Vector3(0f, 0f, 0f),
    var angularVelocity: Vector3 = Vector3(0f, 0f, 0f),
    var rotationX: Float = 0f,
    var rotationY: Float = 0f,
    var rotationZ: Float = 0f,
    val radius: Float = 26f,
    val mass: Float = 1.0f,
    var state: BallState = BallState.ACTIVE,
    var pocketProgress: Float = 0f,
    val primaryColor: Color,
    val stripeColor: Color = Color.White
) {
    val isCueBall: Boolean get() = type == BallType.CUE
    val isEightBall: Boolean get() = type == BallType.EIGHT

    fun isMoving(threshold: Float = 0.05f): Boolean {
        return velocity.lengthSquared() > threshold * threshold
    }

    fun stop() {
        velocity = Vector3.ZERO
        angularVelocity = Vector3.ZERO
    }

    companion object {
        fun getBallColor(number: Int): Color {
            return when (number) {
                0 -> Color.White // Cue ball
                1, 9 -> Color(0xFFFFD700) // Yellow
                2, 10 -> Color(0xFF0055D4) // Blue
                3, 11 -> Color(0xFFE60000) // Red
                4, 12 -> Color(0xFF800080) // Purple
                5, 13 -> Color(0xFFFF6600) // Orange
                6, 14 -> Color(0xFF00802B) // Green
                7, 15 -> Color(0xFF800000) // Maroon
                8 -> Color(0xFF1A1A1A) // Black 8-Ball
                else -> Color.Gray
            }
        }

        fun createDefaultBall(number: Int, position: Vector3): Ball {
            val type = when (number) {
                0 -> BallType.CUE
                8 -> BallType.EIGHT
                in 1..7 -> BallType.SOLID
                else -> BallType.STRIPE
            }
            return Ball(
                id = number,
                number = number,
                type = type,
                position = position,
                primaryColor = getBallColor(number),
                stripeColor = Color.White
            )
        }
    }
}
