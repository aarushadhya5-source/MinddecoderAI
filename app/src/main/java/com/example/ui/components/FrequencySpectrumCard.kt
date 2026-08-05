package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FrequencyBands

@Composable
fun FrequencySpectrumCard(
    bands: FrequencyBands,
    modifier: Modifier = Modifier
) {
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
                    text = "NEURAL FREQUENCY SPECTRUM (FFT)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "250 Hz REALTIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                FrequencyBarItem(
                    label = "DELTA",
                    range = "0.5-4 Hz",
                    power = bands.deltaPower,
                    barColor = Color(0xFF3B82F6)
                )
                FrequencyBarItem(
                    label = "THETA",
                    range = "4-8 Hz",
                    power = bands.thetaPower,
                    barColor = Color(0xFFA044FF)
                )
                FrequencyBarItem(
                    label = "ALPHA",
                    range = "8-12 Hz",
                    power = bands.alphaPower,
                    barColor = Color(0xFF00FFC2)
                )
                FrequencyBarItem(
                    label = "BETA",
                    range = "12-30 Hz",
                    power = bands.betaPower,
                    barColor = Color(0xFF00D1FF)
                )
                FrequencyBarItem(
                    label = "GAMMA",
                    range = "30-100 Hz",
                    power = bands.gammaPower,
                    barColor = Color(0xFFFFD600)
                )
            }
        }
    }
}

@Composable
private fun FrequencyBarItem(
    label: String,
    range: String,
    power: Float,
    barColor: Color
) {
    val animatedPower by animateFloatAsState(
        targetValue = power.coerceIn(0.05f, 1.0f),
        animationSpec = tween(durationMillis = 200),
        label = "powerAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "${(power * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0B0D13)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedPower)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )

        Text(
            text = range,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp
        )
    }
}
