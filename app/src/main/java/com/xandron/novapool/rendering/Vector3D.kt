package com.xandron.novapool.rendering

import kotlin.math.sqrt

/**
 * 3D Vector representation for physics calculations and 3D graphics projection.
 */
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vector3(x / scalar, y / scalar, z / scalar)
    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z

    fun lengthSquared(): Float = x * x + y * y + z * z

    fun length(): Float = sqrt(lengthSquared())

    fun normalize(): Vector3 {
        val len = length()
        return if (len > 0.00001f) this / len else ZERO
    }

    fun distanceTo2D(v: Vector3): Float {
        val dx = x - v.x
        val dy = y - v.y
        return sqrt(dx * dx + dy * dy)
    }

    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
    }
}

/**
 * 2D Projected Point on Screen with depth scale.
 */
data class ScreenPoint(
    val x: Float,
    val y: Float,
    val scale: Float,
    val zDepth: Float
)

/**
 * 3D Camera for perspective projection rendering.
 */
data class Camera3D(
    val eye: Vector3 = Vector3(0f, -1600f, 1400f),
    val target: Vector3 = Vector3(0f, 0f, 0f),
    val up: Vector3 = Vector3(0f, 0f, 1f),
    val fov: Float = 800f
) {
    /**
     * Projects a 3D world coordinate (x,y,z) to 2D Canvas screen coordinates (screenX, screenY).
     */
    fun project(
        worldPos: Vector3,
        screenWidth: Float,
        screenHeight: Float
    ): ScreenPoint {
        val forward = (target - eye).normalize()
        val right = Vector3(
            forward.y * up.z - forward.z * up.y,
            forward.z * up.x - forward.x * up.z,
            forward.x * up.y - forward.y * up.x
        ).normalize()
        val cameraUp = Vector3(
            right.y * forward.z - right.z * forward.y,
            right.z * forward.x - right.x * forward.z,
            right.x * forward.y - right.y * forward.x
        ).normalize()

        val rel = worldPos - eye

        val camX = rel.dot(right)
        val camY = rel.dot(cameraUp)
        val camZ = rel.dot(forward)

        val depth = if (camZ < 10f) 10f else camZ
        val scale = fov / depth

        val screenX = screenWidth / 2f + camX * scale
        val screenY = screenHeight / 2f - camY * scale

        return ScreenPoint(screenX, screenY, scale, depth)
    }
}
