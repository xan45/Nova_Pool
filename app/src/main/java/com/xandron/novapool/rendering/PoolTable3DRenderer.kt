package com.xandron.novapool.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.xandron.novapool.model.Ball
import com.xandron.novapool.model.BallState
import com.xandron.novapool.model.BallType
import com.xandron.novapool.model.CameraMode
import com.xandron.novapool.model.CueItem
import com.xandron.novapool.model.CueState
import com.xandron.novapool.model.FeltColorOption
import com.xandron.novapool.model.TrajectoryPrediction
import com.xandron.novapool.model.WoodFinishOption
import com.xandron.novapool.physics.PoolPhysicsEngine
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom 3D perspective graphics renderer for Nova Pool table, balls, cue stick, and aiming guide.
 */
class PoolTable3DRenderer {

    private val pocketRingColor = Color(0xFF1F1F1F)
    private val pocketHoleColor = Color(0xFF050505)

    fun render(
        drawScope: DrawScope,
        physics: PoolPhysicsEngine,
        cueState: CueState,
        cameraMode: CameraMode,
        trajectory: TrajectoryPrediction?,
        textMeasurer: TextMeasurer,
        ballInHand: Boolean = false,
        ballInHandBehindHeadstring: Boolean = false,
        equippedCue: CueItem = CueItem.DEFAULT_CUE,
        equippedFelt: FeltColorOption = FeltColorOption.EMERALD_GREEN,
        equippedFrame: WoodFinishOption = WoodFinishOption.WALNUT
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val cueBall = physics.getCueBall()
        val cuePos = cueBall?.position ?: Vector3(0f, -physics.tableLength / 4f, 0f)

        val camera = when (cameraMode) {
            CameraMode.AIM_PERSPECTIVE -> {
                val camDist = 650f
                val camHeight = 450f
                val camX = cuePos.x - cos(cueState.angle.toDouble()).toFloat() * camDist
                val camY = cuePos.y - sin(cueState.angle.toDouble()).toFloat() * camDist
                val fovVal = if (width > height) height * 1.30f else min(width, height) * 1.15f
                Camera3D(
                    eye = Vector3(camX, camY, camHeight),
                    target = Vector3(cuePos.x, cuePos.y + 100f, 0f),
                    fov = fovVal
                )
            }
            CameraMode.TOP_DOWN -> {
                val fovVal = if (width > height) height * 1.15f else min(width, height) * 1.05f
                Camera3D(
                    eye = Vector3(0f, 0f, 2400f),
                    target = Vector3(0f, 0f, 0f),
                    fov = fovVal
                )
            }
            CameraMode.ISOMETRIC_3D -> {
                val fovVal = if (width > height) height * 1.25f else min(width, height) * 1.10f
                Camera3D(
                    eye = Vector3(0f, -1700f, 1300f),
                    target = Vector3(0f, 0f, 0f),
                    fov = fovVal
                )
            }
        }

        renderTableFrame(drawScope, physics, camera, width, height, equippedFrame)
        renderFeltSurface(drawScope, physics, camera, width, height, equippedFelt)
        renderCushionsAndPockets(drawScope, physics, camera, width, height)

        if (ballInHand && ballInHandBehindHeadstring) {
            renderHeadstringHighlight(drawScope, physics, camera, width, height)
        }

        if (trajectory != null && !physics.isAnyBallMoving() && !ballInHand) {
            renderTrajectoryGuide(drawScope, trajectory, camera, width, height, physics.ballRadius)
        }

        val sortedBalls = physics.balls.filter { it.state != BallState.POTTED }.sortedByDescending { ball ->
            val p = camera.project(ball.position, width, height)
            p.zDepth
        }

        for (ball in sortedBalls) {
            renderBall3D(drawScope, ball, camera, width, height, textMeasurer)

            if (ball.isCueBall && ballInHand) {
                renderBallInHandIndicator(drawScope, ball, camera, width, height)
            }
        }

        if (cueBall != null && !physics.isAnyBallMoving() && !ballInHand) {
            renderCueStick(drawScope, cueBall, cueState, camera, width, height, equippedCue)
        }
    }

    private fun renderTableFrame(
        drawScope: DrawScope,
        physics: PoolPhysicsEngine,
        camera: Camera3D,
        width: Float,
        height: Float,
        equippedFrame: WoodFinishOption
    ) {
        val halfW = physics.tableWidth / 2f + 120f
        val halfL = physics.tableLength / 2f + 120f

        val corners3D = listOf(
            Vector3(-halfW, halfL, -40f),
            Vector3(halfW, halfL, -40f),
            Vector3(halfW, -halfL, -40f),
            Vector3(-halfW, -halfL, -40f)
        )

        val pts2D = corners3D.map { camera.project(it, width, height) }
        val path = Path().apply {
            moveTo(pts2D[0].x, pts2D[0].y)
            lineTo(pts2D[1].x, pts2D[1].y)
            lineTo(pts2D[2].x, pts2D[2].y)
            lineTo(pts2D[3].x, pts2D[3].y)
            close()
        }

        drawScope.drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(equippedFrame.outerColor, equippedFrame.innerColor, equippedFrame.outerColor)
            )
        )
    }

    private fun renderFeltSurface(
        drawScope: DrawScope,
        physics: PoolPhysicsEngine,
        camera: Camera3D,
        width: Float,
        height: Float,
        equippedFelt: FeltColorOption
    ) {
        val halfW = physics.tableWidth / 2f
        val halfL = physics.tableLength / 2f

        val corners3D = listOf(
            Vector3(-halfW, halfL, 0f),
            Vector3(halfW, halfL, 0f),
            Vector3(halfW, -halfL, 0f),
            Vector3(-halfW, -halfL, 0f)
        )

        val pts2D = corners3D.map { camera.project(it, width, height) }
        val feltPath = Path().apply {
            moveTo(pts2D[0].x, pts2D[0].y)
            lineTo(pts2D[1].x, pts2D[1].y)
            lineTo(pts2D[2].x, pts2D[2].y)
            lineTo(pts2D[3].x, pts2D[3].y)
            close()
        }

        val center2D = camera.project(Vector3(0f, 0f, 0f), width, height)

        drawScope.drawPath(
            path = feltPath,
            brush = Brush.radialGradient(
                colors = listOf(equippedFelt.centerColor, equippedFelt.edgeColor),
                center = Offset(center2D.x, center2D.y),
                radius = max(width, height) * 0.7f
            )
        )

        val headLeft = camera.project(Vector3(-physics.minX, -physics.tableLength / 4f, 0f), width, height)
        val headRight = camera.project(Vector3(physics.minX, -physics.tableLength / 4f, 0f), width, height)

        drawScope.drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(headLeft.x, headLeft.y),
            end = Offset(headRight.x, headRight.y),
            strokeWidth = 2f
        )

        val footSpot = camera.project(Vector3(0f, physics.tableLength / 4f, 0f), width, height)
        drawScope.drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = 6f * footSpot.scale,
            center = Offset(footSpot.x, footSpot.y)
        )
    }

    private fun renderCushionsAndPockets(
        drawScope: DrawScope,
        physics: PoolPhysicsEngine,
        camera: Camera3D,
        width: Float,
        height: Float
    ) {
        for (pocket in physics.pockets) {
            val p2D = camera.project(pocket.position, width, height)
            val rScreen = pocket.mouthRadius * p2D.scale

            drawScope.drawCircle(
                color = pocketRingColor,
                radius = rScreen,
                center = Offset(p2D.x, p2D.y)
            )

            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(pocketHoleColor, Color.Black),
                    center = Offset(p2D.x, p2D.y),
                    radius = rScreen * 0.85f
                ),
                radius = rScreen * 0.85f,
                center = Offset(p2D.x, p2D.y)
            )
        }
    }

    private fun renderBall3D(
        drawScope: DrawScope,
        ball: Ball,
        camera: Camera3D,
        width: Float,
        height: Float,
        textMeasurer: TextMeasurer
    ) {
        val screenPos = camera.project(ball.position, width, height)
        var scale = screenPos.scale
        val r = ball.radius * scale

        val alpha = if (ball.state == BallState.POCKETING) {
            scale *= (1.0f - ball.pocketProgress * 0.6f)
            1.0f - ball.pocketProgress
        } else {
            1.0f
        }

        val center = Offset(screenPos.x, screenPos.y)

        if (ball.state == BallState.ACTIVE) {
            val shadowOffset = Offset(10f * scale, 12f * scale)
            drawScope.drawOval(
                color = Color.Black.copy(alpha = 0.35f * alpha),
                topLeft = center + shadowOffset - Offset(r, r * 0.7f),
                size = Size(r * 2f, r * 1.4f)
            )
        }

        drawScope.drawCircle(
            color = ball.primaryColor.copy(alpha = alpha),
            radius = r,
            center = center
        )

        if (ball.type == BallType.STRIPE) {
            val stripePath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center.x - r,
                        center.y - r * 0.5f,
                        center.x + r,
                        center.y + r * 0.5f
                    )
                )
            }
            drawScope.drawPath(
                path = stripePath,
                color = Color.White.copy(alpha = alpha)
            )
        }

        if (ball.number != 0 && r > 8f) {
            val badgeRadius = r * 0.45f
            drawScope.drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = badgeRadius,
                center = center
            )

            val textResult = textMeasurer.measure(
                text = ball.number.toString(),
                style = TextStyle(
                    fontSize = (r * 0.55f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = alpha)
                )
            )
            drawScope.drawText(
                textLayoutResult = textResult,
                topLeft = center - Offset(textResult.size.width / 2f, textResult.size.height / 2f)
            )
        } else if (ball.number == 0 && r > 8f) {
            drawScope.drawCircle(
                color = Color.Red.copy(alpha = 0.7f * alpha),
                radius = r * 0.15f,
                center = center
            )
        }

        val highlightCenter = center - Offset(r * 0.35f, r * 0.35f)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.65f * alpha), Color.Transparent),
                center = highlightCenter,
                radius = r * 0.45f
            ),
            radius = r * 0.45f,
            center = highlightCenter
        )
    }

    private fun renderTrajectoryGuide(
        drawScope: DrawScope,
        trajectory: TrajectoryPrediction,
        camera: Camera3D,
        width: Float,
        height: Float,
        ballRadius: Float
    ) {
        if (trajectory.cueBallPath.isEmpty()) return

        val cuePath2D = trajectory.cueBallPath.map { camera.project(it, width, height) }

        for (i in 0 until cuePath2D.size - 1) {
            val p1 = cuePath2D[i]
            val p2 = cuePath2D[i + 1]

            drawScope.drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(p1.x, p1.y),
                end = Offset(p2.x, p2.y),
                strokeWidth = 3f * p1.scale,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        }

        trajectory.ghostBallPosition?.let { ghostPos ->
            val ghost2D = camera.project(ghostPos, width, height)
            val ghostR = ballRadius * ghost2D.scale

            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = ghostR,
                center = Offset(ghost2D.x, ghost2D.y),
                style = Stroke(width = 3f * ghost2D.scale)
            )
        }

        if (trajectory.targetBallPath.size >= 2) {
            val t1 = camera.project(trajectory.targetBallPath[0], width, height)
            val t2 = camera.project(trajectory.targetBallPath[1], width, height)

            drawScope.drawLine(
                color = Color(0xFFFFD700).copy(alpha = 0.9f),
                start = Offset(t1.x, t1.y),
                end = Offset(t2.x, t2.y),
                strokeWidth = 4f * t1.scale,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        }
    }

    private fun renderCueStick(
        drawScope: DrawScope,
        cueBall: Ball,
        cueState: CueState,
        camera: Camera3D,
        width: Float,
        height: Float,
        equippedCue: CueItem = CueItem.DEFAULT_CUE
    ) {
        val pullBack = cueState.power * 120f + (if (cueState.isStriking) (1f - cueState.strikeProgress) * 120f else 0f)
        val offsetDist = cueBall.radius + 15f + pullBack

        val dirX = cos(cueState.angle.toDouble()).toFloat()
        val dirY = sin(cueState.angle.toDouble()).toFloat()

        val tip3D = Vector3(
            cueBall.position.x - dirX * offsetDist,
            cueBall.position.y - dirY * offsetDist,
            cueBall.position.z + 10f
        )

        val end3D = Vector3(
            cueBall.position.x - dirX * (offsetDist + cueState.cueLength),
            cueBall.position.y - dirY * (offsetDist + cueState.cueLength),
            cueBall.position.z + 40f
        )

        val tip2D = camera.project(tip3D, width, height)
        val end2D = camera.project(end3D, width, height)

        drawScope.drawLine(
            brush = Brush.linearGradient(
                colors = listOf(equippedCue.primaryColor, equippedCue.secondaryColor, equippedCue.accentColor),
                start = Offset(tip2D.x, tip2D.y),
                end = Offset(end2D.x, end2D.y)
            ),
            start = Offset(tip2D.x, tip2D.y),
            end = Offset(end2D.x, end2D.y),
            strokeWidth = 10f * tip2D.scale,
            cap = StrokeCap.Round
        )

        val ferrule3D = Vector3(
            cueBall.position.x - dirX * (offsetDist + 12f),
            cueBall.position.y - dirY * (offsetDist + 12f),
            cueBall.position.z + 10f
        )
        val ferrule2D = camera.project(ferrule3D, width, height)
        drawScope.drawLine(
            color = equippedCue.tipColor,
            start = Offset(tip2D.x, tip2D.y),
            end = Offset(ferrule2D.x, ferrule2D.y),
            strokeWidth = 10f * tip2D.scale,
            cap = StrokeCap.Butt
        )
    }

    private fun renderHeadstringHighlight(
        drawScope: DrawScope,
        physics: PoolPhysicsEngine,
        camera: Camera3D,
        width: Float,
        height: Float
    ) {
        val headLeft = camera.project(Vector3(-physics.minX, -physics.tableLength / 4f, 0f), width, height)
        val headRight = camera.project(Vector3(physics.minX, -physics.tableLength / 4f, 0f), width, height)

        drawScope.drawLine(
            color = Color(0xFFFFD700).copy(alpha = 0.85f),
            start = Offset(headLeft.x, headLeft.y),
            end = Offset(headRight.x, headRight.y),
            strokeWidth = 4f * headLeft.scale,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
        )
    }

    private fun renderBallInHandIndicator(
        drawScope: DrawScope,
        ball: Ball,
        camera: Camera3D,
        width: Float,
        height: Float
    ) {
        val screenPos = camera.project(ball.position, width, height)
        val r = ball.radius * screenPos.scale * 1.6f
        val center = Offset(screenPos.x, screenPos.y)

        drawScope.drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
            radius = r,
            center = center,
            style = Stroke(width = 4f * screenPos.scale)
        )

        drawScope.drawLine(
            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
            start = center - Offset(r * 1.3f, 0f),
            end = center + Offset(r * 1.3f, 0f),
            strokeWidth = 2f * screenPos.scale
        )
        drawScope.drawLine(
            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
            start = center - Offset(0f, r * 1.3f),
            end = center + Offset(0f, r * 1.3f),
            strokeWidth = 2f * screenPos.scale
        )
    }
}
