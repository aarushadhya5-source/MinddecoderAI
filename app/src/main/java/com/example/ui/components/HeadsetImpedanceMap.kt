package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NeuralChannel

@Composable
fun HeadsetImpedanceMap(
    channels: List<NeuralChannel>,
    modifier: Modifier = Modifier
) {
    var selectedNodeName by remember { mutableStateOf("Fp1") }
    val selectedChannel = channels.find { it.name == selectedNodeName } ?: channels.firstOrNull()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10-20 EEG SENSOR IMPEDANCE MAP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "8 CHANNELS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Head Outline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B0D13)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val headRadius = height * 0.38f

                    // Skull outline
                    drawCircle(
                        color = Color(0xFF1F283B),
                        center = Offset(centerX, centerY),
                        radius = headRadius,
                        style = Stroke(width = 3f)
                    )

                    // Nose indicator (Nasion)
                    val nosePath = Path().apply {
                        moveTo(centerX - 12f, centerY - headRadius)
                        lineTo(centerX, centerY - headRadius - 16f)
                        lineTo(centerX + 12f, centerY - headRadius)
                    }
                    drawPath(nosePath, Color(0xFF1F283B), style = Stroke(width = 3f))

                    // Left/Right ears
                    drawCircle(color = Color(0xFF1F283B), radius = 10f, center = Offset(centerX - headRadius - 6f, centerY), style = Stroke(width = 2f))
                    drawCircle(color = Color(0xFF1F283B), radius = 10f, center = Offset(centerX + headRadius + 6f, centerY), style = Stroke(width = 2f))
                }

                // Node Positions map (Percentages on head layout)
                val nodePositions = listOf(
                    "Fp1" to Pair(0.38f, 0.22f),
                    "Fp2" to Pair(0.62f, 0.22f),
                    "C3" to Pair(0.32f, 0.50f),
                    "C4" to Pair(0.68f, 0.50f),
                    "T3" to Pair(0.18f, 0.50f),
                    "T4" to Pair(0.82f, 0.50f),
                    "O1" to Pair(0.38f, 0.78f),
                    "O2" to Pair(0.62f, 0.78f)
                )

                Box(modifier = Modifier.matchParentSize()) {
                    nodePositions.forEach { (nodeName, pos) ->
                        val chan = channels.find { it.name == nodeName }
                        val impedance = chan?.impedanceOhm ?: 4000
                        val isSelected = (selectedNodeName == nodeName)

                        val statusColor = when {
                            impedance < 4000 -> MaterialTheme.colorScheme.primary // Excellent < 4 kOhm
                            impedance < 6000 -> Color(0xFFFFD600) // Fair
                            else -> Color(0xFFFF3366) // Poor
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (pos.first * 320).dp,
                                    top = (pos.second * 160).dp
                                )
                                .size(if (isSelected) 28.dp else 22.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = if (isSelected) 0.4f else 0.2f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = statusColor,
                                    shape = CircleShape
                                )
                                .clickable { selectedNodeName = nodeName },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nodeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSelected) 10.sp else 8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Node Detail Card
            if (selectedChannel != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0B0D13))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CHANNEL ${selectedChannel.name}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${selectedChannel.region})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "CONTACT IMPEDANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${selectedChannel.impedanceOhm} Ω",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedChannel.impedanceOhm < 4000) MaterialTheme.colorScheme.primary else Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedChannel.impedanceOhm < 4000) "EXCELLENT LINK" else "FAIR LINK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
