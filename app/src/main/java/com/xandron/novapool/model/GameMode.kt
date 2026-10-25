package com.xandron.novapool.model

import com.xandron.novapool.rendering.Vector3

enum class GameMode(val displayName: String) {
    EIGHT_BALL("8-Ball"),
    NINE_BALL("9-Ball"),
    PRACTICE("Practice")
}

enum class CameraMode {
    AIM_PERSPECTIVE,
    TOP_DOWN,
    ISOMETRIC_3D
}

data class TrajectoryPrediction(
    val cueBallPath: List<Vector3>,
    val targetBallPath: List<Vector3>,
    val hitBallId: Int? = null,
    val ghostBallPosition: Vector3? = null,
    val cushionHitPoint: Vector3? = null
)
