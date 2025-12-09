
package com.example.moneytrack.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun DonutChartCard(
    title: String,
    data: List<Float>,
    modifier: Modifier = Modifier,
    showCenterHole: Boolean = true
) {
    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Read MaterialTheme colors here (COMPOSABLE SCOPE)
            val baseColor = MaterialTheme.colorScheme.primary
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            val backgroundColor = MaterialTheme.colorScheme.background

            // Build a simple palette from the base color
            val segmentColors: List<Color> = data.indices.map { i ->
                baseColor.copy(alpha = 0.6f + (i % 5) * 0.08f)
            }

            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                val total = data.sum()

                // If nothing to draw, render a neutral ring and exit
                if (total <= 0f) {
                    val ringStroke = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = surfaceVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = ringStroke
                    )
                    return@Canvas
                }

                val arcStroke = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round)
                val radius = min(size.width, size.height) / 2f - arcStroke.width / 2f

                var startAngle = -90f
                data.forEachIndexed { index, value ->
                    if (value <= 0f) return@forEachIndexed
                    val sweep = (value / total) * 360f

                    drawArc(
                        color = segmentColors[index],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = arcStroke
                    )

                    startAngle += sweep
                }

                // Optional center hole to make it a donut
                if (showCenterHole) {
                    val holeRadius = radius - arcStroke.width / 2f
                    drawCircle(
                        color = backgroundColor,
                        radius = holeRadius
                    )
                }
            }
        }
    }
}
