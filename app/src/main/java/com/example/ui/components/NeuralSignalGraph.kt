package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeuralSignalGraph(
    signals: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    graphHeight: Dp = 160.dp,
    showGrid: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(graphHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF121212))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Grid Lines
            if (showGrid) {
                val gridColor = Color(0x12FFFFFF)

                // Horizontal grid lines
                for (i in 1..4) {
                    val y = height * (i / 5f)
                    drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                }

                // Vertical grid lines
                for (i in 1..8) {
                    val x = width * (i / 9f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                }
            }

            // Center zero axis
            drawLine(
                color = Color(0xFF00FFC2).copy(alpha = 0.2f),
                start = Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                strokeWidth = 1.5f
            )

            if (signals.isEmpty()) return@Canvas

            // 2. Draw Waveform Path
            val spacing = if (signals.size > 1) width / (signals.size - 1) else width
            val path = Path()

            signals.forEachIndexed { i, signal ->
                val x = i * spacing
                // Scale signal to fit within canvas height
                val y = (height / 2f) - (signal * (height / 2.6f))
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Outer glow line
            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.35f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Main sharp signal line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }
}
