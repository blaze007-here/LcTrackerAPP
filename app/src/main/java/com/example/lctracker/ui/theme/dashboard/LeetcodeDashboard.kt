package com.example.lctracker.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lctracker.ui.Difficulty
import com.example.lctracker.ui.Problem

@Composable
fun LeetcodeDashboard(problems: List<Problem>) {
    val total = problems.size.coerceAtLeast(1)
    val solved = problems.count { it.isSolved }
    val attempting = problems.count { !it.isSolved }
    val easy = problems.count { it.difficulty == Difficulty.EASY && it.isSolved }
    val totalEasy = problems.count { it.difficulty == Difficulty.EASY }

    val medium = problems.count { it.difficulty == Difficulty.MEDIUM && it.isSolved }
    val totalMedium = problems.count { it.difficulty == Difficulty.MEDIUM }

    val hard = problems.count { it.difficulty == Difficulty.HARD && it.isSolved }
    val totalHard = problems.count { it.difficulty == Difficulty.HARD }

    val ringColors = listOf(
        Color(0xFF16C3B0),
        Color(0xFFFFB300),
        Color(0xFFE53935)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    val sweepAngles = listOf(
                        easy.toFloat() / total * 360f,
                        medium.toFloat() / total * 360f,
                        hard.toFloat() / total * 360f
                    )
                    val startAngles = listOf(0f, sweepAngles[0], sweepAngles[0] + sweepAngles[1])
                    for (i in 0..2) {
                        drawArc(
                            color = ringColors[i],
                            startAngle = startAngles[i] - 90f,
                            sweepAngle = sweepAngles[i],
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$solved",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "/$total Solved",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$attempting Attempting",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyStat("Easy", easy to totalEasy, Color(0xFF16C3B0))
                DifficultyStat("Med.", medium to totalMedium, Color(0xFFFFB300))
                DifficultyStat("Hard", hard to totalHard, Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun DifficultyStat(label: String, pair: Pair<Int, Int>, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(90.dp)
            .height(40.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = color, fontSize = 12.sp)
            Text("${pair.first}/${pair.second}", color = Color.White, fontSize = 12.sp)
        }
    }
}