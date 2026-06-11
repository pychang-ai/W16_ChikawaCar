package com.example.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin

object ChiikawaDrawers {

    fun drawCharacterWithCar(
        drawScope: DrawScope,
        charType: CharacterType,
        center: Offset,
        radius: Float,
        headingAngle: Float = 0f,
        bounceOffset: Float = 0f,
        isBumping: Boolean = false
    ) {
        drawScope.apply {
            // Keep drawing safe
            val scale = radius / 50f // standard reference radius 50f
            
            // Draw back of the bumper car (antenna)
            rotate(degrees = headingAngle, pivot = center) {
                // Antenna Pole
                val antennaStart = Offset(center.x - 25 * scale, center.y + 10 * scale)
                val antennaEnd = Offset(center.x - 45 * scale, center.y - 15 * scale + bounceOffset)
                drawLine(
                    color = Color(0xFF546E7A),
                    start = antennaStart,
                    end = antennaEnd,
                    strokeWidth = 3f * scale,
                    cap = StrokeCap.Round
                )
                // Antenna Spark/Circle
                drawCircle(
                    color = if (isBumping) Color(0xFFFF5252) else Color(0xFFFFEB3B),
                    radius = 6f * scale,
                    center = antennaEnd
                )

                // Bumper car rubber base ring (shadow / outer bumper)
                drawOval(
                    color = Color(0xFF263238),
                    topLeft = Offset(center.x - 48 * scale, center.y - 38 * scale),
                    size = Size(96 * scale, 80 * scale)
                )

                // Bumper car colored body
                val carBodyColor = when (charType) {
                    CharacterType.CHIIKAWA -> Color(0xFFFF8FA3) // Pastel Pink
                    CharacterType.HACHIWARE -> Color(0xFF29B6F6) // Pastel Light Blue
                    CharacterType.USAGI -> Color(0xFFFFCA28) // Pastel Gold/Yellow
                }
                drawOval(
                    color = carBodyColor,
                    topLeft = Offset(center.x - 42 * scale, center.y - 34 * scale),
                    size = Size(84 * scale, 70 * scale)
                )

                // Seat support / Headrest
                drawRoundRect(
                    color = Color(0xFF455A64),
                    topLeft = Offset(center.x - 38 * scale, center.y + 15 * scale),
                    size = Size(20 * scale, 15 * scale),
                    cornerRadius = CornerRadius(4 * scale, 4 * scale)
                )

                // Inner seat / backing
                drawOval(
                    color = Color(0xFF37474F),
                    topLeft = Offset(center.x - 34 * scale, center.y - 20 * scale),
                    size = Size(68 * scale, 50 * scale)
                )
            }

            // Draw character inside the car (always faces forward relative to angle or looks at user)
            // Let's slightly rotate them by headingAngle so they turn with the car
            rotate(degrees = headingAngle, pivot = center) {
                val yOffset = -8f * scale + bounceOffset
                val charCenter = Offset(center.x, center.y + yOffset)
                
                // 1. Draw Ears
                when (charType) {
                    CharacterType.CHIIKAWA -> {
                        // Small white ears
                        drawCircle(
                            color = Color.White,
                            radius = 11f * scale,
                            center = Offset(charCenter.x - 16 * scale, charCenter.y - 18 * scale)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 11f * scale,
                            center = Offset(charCenter.x + 16 * scale, charCenter.y - 18 * scale)
                        )
                        // Inner pink ear
                        drawCircle(
                            color = Color(0xFFFFD6E0),
                            radius = 6f * scale,
                            center = Offset(charCenter.x - 16 * scale, charCenter.y - 18 * scale)
                        )
                        drawCircle(
                            color = Color(0xFFFFD6E0),
                            radius = 6f * scale,
                            center = Offset(charCenter.x + 16 * scale, charCenter.y - 18 * scale)
                        )
                    }
                    CharacterType.HACHIWARE -> {
                        // Triangular Ears (Blue hair caps)
                        val earPathLeft = Path().apply {
                            moveTo(charCenter.x - 28 * scale, charCenter.y - 10 * scale)
                            lineTo(charCenter.x - 22 * scale, charCenter.y - 36 * scale)
                            lineTo(charCenter.x - 8 * scale, charCenter.y - 18 * scale)
                            close()
                        }
                        val earPathRight = Path().apply {
                            moveTo(charCenter.x + 8 * scale, charCenter.y - 18 * scale)
                            lineTo(charCenter.x + 22 * scale, charCenter.y - 36 * scale)
                            lineTo(charCenter.x + 28 * scale, charCenter.y - 10 * scale)
                            close()
                        }
                        drawPath(earPathLeft, color = Color(0xFF1E88E5))
                        drawPath(earPathRight, color = Color(0xFF1E88E5))

                        // Inner pink ear
                        drawCircle(
                            color = Color(0xFFFFD6E0),
                            radius = 4f * scale,
                            center = Offset(charCenter.x - 20 * scale, charCenter.y - 22 * scale)
                        )
                        drawCircle(
                            color = Color(0xFFFFD6E0),
                            radius = 4f * scale,
                            center = Offset(charCenter.x + 20 * scale, charCenter.y - 22 * scale)
                        )
                    }
                    CharacterType.USAGI -> {
                        // Long Ears standing tall
                        // Left Ear
                        val leftEarPath = Path().apply {
                            moveTo(charCenter.x - 14 * scale, charCenter.y - 15 * scale)
                            quadraticBezierTo(
                                charCenter.x - 20 * scale, charCenter.y - 50 * scale,
                                charCenter.x - 16 * scale, charCenter.y - 54 * scale
                            )
                            quadraticBezierTo(
                                charCenter.x - 10 * scale, charCenter.y - 50 * scale,
                                charCenter.x - 6 * scale, charCenter.y - 15 * scale
                            )
                            close()
                        }
                        // Right Ear
                        val rightEarPath = Path().apply {
                            moveTo(charCenter.x + 6 * scale, charCenter.y - 15 * scale)
                            quadraticBezierTo(
                                charCenter.x + 10 * scale, charCenter.y - 50 * scale,
                                charCenter.x + 16 * scale, charCenter.y - 54 * scale
                            )
                            quadraticBezierTo(
                                charCenter.x + 20 * scale, charCenter.y - 50 * scale,
                                charCenter.x + 14 * scale, charCenter.y - 15 * scale
                            )
                            close()
                        }
                        drawPath(leftEarPath, color = Color(0xFFFFF176))
                        drawPath(rightEarPath, color = Color(0xFFFFF176))

                        // Inner pink ear details
                        val leftInnerEarPath = Path().apply {
                            moveTo(charCenter.x - 13 * scale, charCenter.y - 20 * scale)
                            quadraticBezierTo(
                                charCenter.x - 17 * scale, charCenter.y - 44 * scale,
                                charCenter.x - 14 * scale, charCenter.y - 48 * scale
                            )
                            quadraticBezierTo(
                                charCenter.x - 10 * scale, charCenter.y - 44 * scale,
                                charCenter.x - 9 * scale, charCenter.y - 20 * scale
                            )
                            close()
                        }
                        val rightInnerEarPath = Path().apply {
                            moveTo(charCenter.x + 9 * scale, charCenter.y - 20 * scale)
                            quadraticBezierTo(
                                charCenter.x + 10 * scale, charCenter.y - 44 * scale,
                                charCenter.x + 14 * scale, charCenter.y - 48 * scale
                            )
                            quadraticBezierTo(
                                charCenter.x + 17 * scale, charCenter.y - 44 * scale,
                                charCenter.x + 13 * scale, charCenter.y - 20 * scale
                            )
                            close()
                        }
                        drawPath(leftInnerEarPath, color = Color(0xFFFFD6E0))
                        drawPath(rightInnerEarPath, color = Color(0xFFFFD6E0))
                    }
                }

                // 2. Draw Main Body/Head
                val bodyColor = when (charType) {
                    CharacterType.USAGI -> Color(0xFFFFF176) // Pale Yellow
                    else -> Color.White
                }
                drawOval(
                    color = bodyColor,
                    topLeft = Offset(charCenter.x - 28 * scale, charCenter.y - 20 * scale),
                    size = Size(56 * scale, 45 * scale)
                )

                // 2b. If Hachiware, draw the top hair (blue part separated in center)
                if (charType == CharacterType.HACHIWARE) {
                    val hairPath = Path().apply {
                        moveTo(charCenter.x - 28 * scale, charCenter.y - 5 * scale)
                        quadraticBezierTo(
                            charCenter.x - 26 * scale, charCenter.y - 20 * scale,
                            charCenter.x, charCenter.y - 8 * scale
                        )
                        quadraticBezierTo(
                            charCenter.x + 26 * scale, charCenter.y - 20 * scale,
                            charCenter.x + 28 * scale, charCenter.y - 5 * scale
                        )
                        lineTo(charCenter.x + 28 * scale, charCenter.y - 12 * scale)
                        quadraticBezierTo(
                            charCenter.x, charCenter.y - 24 * scale,
                            charCenter.x - 28 * scale, charCenter.y - 12 * scale
                        )
                        close()
                    }
                    drawPath(hairPath, color = Color(0xFF1E88E5))
                }

                // 3. Draw Eyes
                val eyeRadius = 3f * scale
                val leftEyeCenter = Offset(charCenter.x - 11 * scale, charCenter.y - 3 * scale)
                val rightEyeCenter = Offset(charCenter.x + 11 * scale, charCenter.y - 3 * scale)

                drawCircle(color = Color(0xFF1A1A1A), radius = eyeRadius, center = leftEyeCenter)
                drawCircle(color = Color(0xFF1A1A1A), radius = eyeRadius, center = rightEyeCenter)

                // Eye highlights (white dots)
                drawCircle(color = Color.White, radius = 0.9f * scale, center = Offset(leftEyeCenter.x - 1f * scale, leftEyeCenter.y - 1f * scale))
                drawCircle(color = Color.White, radius = 0.9f * scale, center = Offset(rightEyeCenter.x - 1f * scale, rightEyeCenter.y - 1f * scale))

                // Eyebrows
                val browY = charCenter.y - 9 * scale
                drawArcCurve(
                    drawScope = this,
                    start = Offset(leftEyeCenter.x - 4 * scale, browY),
                    control = Offset(leftEyeCenter.x, browY - 3 * scale),
                    end = Offset(leftEyeCenter.x + 2 * scale, browY),
                    color = Color(0xFF424242),
                    strokeWidth = 1.5f * scale
                )
                drawArcCurve(
                    drawScope = this,
                    start = Offset(rightEyeCenter.x - 2 * scale, browY),
                    control = Offset(rightEyeCenter.x, browY - 3 * scale),
                    end = Offset(rightEyeCenter.x + 4 * scale, browY),
                    color = Color(0xFF424242),
                    strokeWidth = 1.5f * scale
                )

                // 4. Draw Blush (腮紅)
                val blushLeftCenter = Offset(charCenter.x - 17 * scale, charCenter.y + 4 * scale)
                val blushRightCenter = Offset(charCenter.x + 17 * scale, charCenter.y + 4 * scale)
                drawOval(
                    color = Color(0xFFFFCCD5),
                    topLeft = Offset(blushLeftCenter.x - 6 * scale, blushLeftCenter.y - 4 * scale),
                    size = Size(12 * scale, 8 * scale)
                )
                drawOval(
                    color = Color(0xFFFFCCD5),
                    topLeft = Offset(blushRightCenter.x - 6 * scale, blushRightCenter.y - 4 * scale),
                    size = Size(12 * scale, 8 * scale)
                )

                // Tiny blush slashes (comic detail)
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(blushLeftCenter.x - 2 * scale, blushLeftCenter.y - 1 * scale),
                    end = Offset(blushLeftCenter.x - 1 * scale, blushLeftCenter.y + 2 * scale),
                    strokeWidth = 1.2f * scale
                )
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(blushLeftCenter.x + 1 * scale, blushLeftCenter.y - 1 * scale),
                    end = Offset(blushLeftCenter.x + 2 * scale, blushLeftCenter.y + 2 * scale),
                    strokeWidth = 1.2f * scale
                )
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(blushRightCenter.x - 2 * scale, blushRightCenter.y - 1 * scale),
                    end = Offset(blushRightCenter.x - 1 * scale, blushRightCenter.y + 2 * scale),
                    strokeWidth = 1.2f * scale
                )
                drawLine(
                    color = Color(0xFFFF5252),
                    start = Offset(blushRightCenter.x + 1 * scale, blushRightCenter.y - 1 * scale),
                    end = Offset(blushRightCenter.x + 2 * scale, blushRightCenter.y + 2 * scale),
                    strokeWidth = 1.2f * scale
                )

                // 5. Draw Mouth
                when (charType) {
                    CharacterType.CHIIKAWA -> {
                        // Cute nervous inverted triangle / down curved mouth
                        val mouthPath = Path().apply {
                            moveTo(charCenter.x - 3 * scale, charCenter.y + 3 * scale)
                            quadraticBezierTo(
                                charCenter.x, charCenter.y + 6 * scale,
                                charCenter.x + 3 * scale, charCenter.y + 3 * scale
                            )
                        }
                        drawPath(
                            path = mouthPath,
                            color = Color(0xFF1A1A1A),
                            style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
                        )
                    }
                    CharacterType.HACHIWARE -> {
                        // Smiley Cat-like mouth
                        val mouthPath = Path().apply {
                            moveTo(charCenter.x - 4 * scale, charCenter.y + 2 * scale)
                            quadraticBezierTo(charCenter.x - 2 * scale, charCenter.y + 4 * scale, charCenter.x, charCenter.y + 2 * scale)
                            quadraticBezierTo(charCenter.x + 2 * scale, charCenter.y + 4 * scale, charCenter.x + 4 * scale, charCenter.y + 2 * scale)
                        }
                        drawPath(
                            path = mouthPath,
                            color = Color(0xFF1A1A1A),
                            style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
                        )
                    }
                    CharacterType.USAGI -> {
                        // Energetic screaming open mouth "Ura!"
                        drawOval(
                            color = Color(0xFFD32F2F),
                            topLeft = Offset(charCenter.x - 4 * scale, charCenter.y + 1 * scale),
                            size = Size(8 * scale, 9 * scale)
                        )
                        // Tongue inside
                        drawOval(
                            color = Color(0xFFFF8A80),
                            topLeft = Offset(charCenter.x - 3 * scale, charCenter.y + 5 * scale),
                            size = Size(6 * scale, 5 * scale)
                        )
                        // Mouth outline
                        drawOval(
                            color = Color(0xFF1A1A1A),
                            topLeft = Offset(charCenter.x - 4 * scale, charCenter.y + 1 * scale),
                            size = Size(8 * scale, 9 * scale),
                            style = Stroke(width = 1.5f * scale)
                        )
                    }
                }

                // 6. Hands holding steering wheel
                // Draw steering wheel (black ring)
                drawCircle(
                    color = Color(0xFF37474F),
                    radius = 8f * scale,
                    center = Offset(charCenter.x, charCenter.y + 16 * scale),
                    style = Stroke(width = 3f * scale)
                )
                // Small hands
                drawCircle(
                    color = bodyColor,
                    radius = 4.5f * scale,
                    center = Offset(charCenter.x - 9 * scale, charCenter.y + 14 * scale)
                )
                drawCircle(
                    color = bodyColor,
                    radius = 4.5f * scale,
                    center = Offset(charCenter.x + 9 * scale, charCenter.y + 14 * scale)
                )
            }
        }
    }

    fun drawApple(drawScope: DrawScope, center: Offset, radius: Float) {
        drawScope.apply {
            val scale = radius / 18f

            // Apple main red body
            drawCircle(
                color = Color(0xFFE53935), // Shiny Apple Red
                radius = radius,
                center = center
            )

            // Red butt-curve of apple (left & right lobes)
            drawCircle(
                color = Color(0xFFC62828),
                radius = radius * 0.9f,
                center = Offset(center.x - 1 * scale, center.y + 2 * scale)
            )

            // Green leaf
            val leafPath = Path().apply {
                moveTo(center.x, center.y - radius * 0.9f)
                quadraticBezierTo(
                    center.x + 12 * scale, center.y - radius * 1.5f,
                    center.x + 10 * scale, center.y - radius * 1.8f
                )
                quadraticBezierTo(
                    center.x + 2 * scale, center.y - radius * 1.3f,
                    center.x, center.y - radius * 0.9f
                )
                close()
            }
            drawPath(leafPath, color = Color(0xFF4CAF50))

            // Brown Stem
            val stemPath = Path().apply {
                moveTo(center.x, center.y - radius * 0.8f)
                quadraticBezierTo(
                    center.x - 4 * scale, center.y - radius * 1.4f,
                    center.x - 3 * scale, center.y - radius * 1.6f
                )
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF795548),
                style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
            )

            // Shiny White Highlight Dot
            drawCircle(
                color = Color.White,
                radius = 3f * scale,
                center = Offset(center.x - 6 * scale, center.y - 6 * scale)
            )
        }
    }

    fun drawGoldenApple(drawScope: DrawScope, center: Offset, radius: Float, sparkleOffset: Float = 0f) {
        drawScope.apply {
            val scale = radius / 18f

            // Golden body
            drawCircle(
                color = Color(0xFFFFD700), // Pure Gold
                radius = radius,
                center = center
            )

            // Inner gloss
            drawCircle(
                color = Color(0xFFFFEE58),
                radius = radius * 0.8f,
                center = Offset(center.x - 1 * scale, center.y + 1 * scale)
            )

            // Sparkles inside
            drawCircle(
                color = Color.White,
                radius = 3f * scale,
                center = Offset(center.x - 5 * scale, center.y - 5 * scale)
            )

            // Sparkle stars around it
            val sparklePoints = 4
            for (i in 0 until sparklePoints) {
                val angle = (i * PI / 2) + sparkleOffset
                val sx = center.x + sin(angle).toFloat() * (radius * 1.5f)
                val sy = center.y + sin(angle + PI / 2).toFloat() * (radius * 1.5f)
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 2.5f * scale,
                    center = Offset(sx, sy)
                )
            }

            // Green leaf
            val leafPath = Path().apply {
                moveTo(center.x, center.y - radius * 0.9f)
                quadraticBezierTo(
                    center.x + 10 * scale, center.y - radius * 1.5f,
                    center.x + 8 * scale, center.y - radius * 1.8f
                )
                quadraticBezierTo(
                    center.x + 2 * scale, center.y - radius * 1.3f,
                    center.x, center.y - radius * 0.9f
                )
                close()
            }
            drawPath(leafPath, color = Color(0xFF81C784))

            // Stem
            val stemPath = Path().apply {
                moveTo(center.x, center.y - radius * 0.8f)
                quadraticBezierTo(
                    center.x - 3 * scale, center.y - radius * 1.3f,
                    center.x - 2 * scale, center.y - radius * 1.5f
                )
            }
            drawPath(
                path = stemPath,
                color = Color(0xFF8D6E63),
                style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
            )
        }
    }

    fun drawStone(drawScope: DrawScope, center: Offset, radius: Float) {
        drawScope.apply {
            val scale = radius / 22f

            // Procedural Stone Polygon (using vertices with slightly rocky styling)
            val vertexOffsets = listOf(
                Offset(0f, -22f),
                Offset(18f, -14f),
                Offset(22f, 4f),
                Offset(12f, 20f),
                Offset(-10f, 22f),
                Offset(-22f, 8f),
                Offset(-18f, -14f)
            )

            val stonePath = Path().apply {
                var first = true
                for (v in vertexOffsets) {
                    val px = center.x + v.x * scale
                    val py = center.y + v.y * scale
                    if (first) {
                        moveTo(px, py)
                        first = false
                    } else {
                        lineTo(px, py)
                    }
                }
                close()
            }

            // Filled stone base
            drawPath(stonePath, color = Color(0xFF90A4AE)) // Rocky slate gray

            // Dark border & shadow
            drawPath(
                path = stonePath,
                color = Color(0xFF455A64),
                style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
            )

            // Draw rocky cracks & facets
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(center.x + 18f * scale, center.y - 14f * scale),
                end = Offset(center.x - 5f * scale, center.y + 2f * scale),
                strokeWidth = 2f * scale
            )
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(center.x - 22f * scale, center.y + 8f * scale),
                end = Offset(center.x - 5f * scale, center.y + 2f * scale),
                strokeWidth = 2f * scale
            )
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(center.x + 12f * scale, center.y + 20f * scale),
                end = Offset(center.x - 5f * scale, center.y + 2f * scale),
                strokeWidth = 2f * scale
            )

            // High light facet for 3D rock feeling
            val highlightPath = Path().apply {
                moveTo(center.x + 0f * scale, center.y - 20f * scale)
                lineTo(center.x + 16f * scale, center.y - 12f * scale)
                lineTo(center.x - 5f * scale, center.y + 0f * scale)
                close()
            }
            drawPath(highlightPath, color = Color(0xFFB0BEC5).copy(alpha = 0.4f))
        }
    }

    private fun drawArcCurve(
        drawScope: DrawScope,
        start: Offset,
        control: Offset,
        end: Offset,
        color: Color,
        strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(control.x, control.y, end.x, end.y)
        }
        drawScope.drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
