package com.xandron.novapool.model

import com.xandron.novapool.rendering.Vector3

enum class PocketType {
    CORNER,
    SIDE
}

data class Pocket(
    val id: Int,
    val name: String,
    val position: Vector3,
    val type: PocketType,
    val dropRadius: Float = 52f,
    val mouthRadius: Float = 68f
) {
    companion object {
        fun createStandardPockets(tableWidth: Float, tableLength: Float): List<Pocket> {
            val halfW = tableWidth / 2f
            val halfL = tableLength / 2f
            val cornerOffset = 30f

            return listOf(
                Pocket(0, "Top-Left", Vector3(-halfW + cornerOffset, halfL - cornerOffset, 0f), PocketType.CORNER),
                Pocket(1, "Top-Right", Vector3(halfW - cornerOffset, halfL - cornerOffset, 0f), PocketType.CORNER),
                Pocket(2, "Middle-Left", Vector3(-halfW - 5f, 0f, 0f), PocketType.SIDE),
                Pocket(3, "Middle-Right", Vector3(halfW + 5f, 0f, 0f), PocketType.SIDE),
                Pocket(4, "Bottom-Left", Vector3(-halfW + cornerOffset, -halfL + cornerOffset, 0f), PocketType.CORNER),
                Pocket(5, "Bottom-Right", Vector3(halfW - cornerOffset, -halfL + cornerOffset, 0f), PocketType.CORNER)
            )
        }
    }
}
