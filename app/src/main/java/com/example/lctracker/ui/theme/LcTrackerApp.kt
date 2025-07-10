// File: LcTrackerApp.kt
package com.example.lctracker.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lctracker.data.DataStoreManager
import com.example.lctracker.ui.dashboard.LeetcodeDashboard
import kotlinx.coroutines.launch

enum class Difficulty {
    EASY, MEDIUM, HARD
}

data class Problem(
    val title: String,
    var isSolved: Boolean = false,
    var difficulty: Difficulty = Difficulty.EASY
)

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LcTrackerApp() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    var problems by remember { mutableStateOf(listOf<Problem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newProblemTitle by remember { mutableStateOf("") }
    var newProblemDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    LaunchedEffect(Unit) {
        try {
            dataStoreManager.problemsFlow.collect {
                problems = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredProblems = selectedDifficulty?.let {
        problems.filter { p -> p.difficulty == it }
    } ?: problems

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Problem")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("LeetCode Tracker", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (problems.isNotEmpty()) {
                AnimatedLeetcodeDashboard(problems)
                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Difficulty.values().forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty?.name == diff.name,
                            onClick = {
                                selectedDifficulty = if (selectedDifficulty == diff) null else diff
                            },
                            label = { Text(diff.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn {
                items(filteredProblems) { problem ->
                    ProblemItem(problem) {
                        problems = problems.map {
                            if (it.title == problem.title)
                                it.copy(isSolved = !it.isSolved)
                            else it
                        }
                        scope.launch {
                            dataStoreManager.saveProblems(problems)
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        newProblemTitle = ""
                        newProblemDifficulty = Difficulty.EASY
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newProblemTitle.isNotBlank()) {
                                val updated = problems + Problem(newProblemTitle, false, newProblemDifficulty)
                                problems = updated
                                scope.launch {
                                    dataStoreManager.saveProblems(updated)
                                }
                                newProblemTitle = ""
                                newProblemDifficulty = Difficulty.EASY
                                showDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            newProblemTitle = ""
                            newProblemDifficulty = Difficulty.EASY
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Add New Problem") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newProblemTitle,
                                onValueChange = { newProblemTitle = it },
                                label = { Text("Problem title") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select Difficulty:")
                            Difficulty.values().forEach { difficulty ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { newProblemDifficulty = difficulty }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = newProblemDifficulty == difficulty,
                                        onClick = { newProblemDifficulty = difficulty }
                                    )
                                    Text(difficulty.name)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProblemItem(problem: Problem, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (problem.isSolved)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = problem.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (problem.isSolved) "Solved" else "Pending",
                color = if (problem.isSolved)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AnimatedLeetcodeDashboard(problems: List<Problem>) {
    val total = problems.size.coerceAtLeast(1)
    val easySolved = problems.count { it.difficulty == Difficulty.EASY && it.isSolved }
    val mediumSolved = problems.count { it.difficulty == Difficulty.MEDIUM && it.isSolved }
    val hardSolved = problems.count { it.difficulty == Difficulty.HARD && it.isSolved }
    val solved = problems.count { it.isSolved }
    val attempting = problems.count { !it.isSolved }

    val easyAngle by animateFloatAsState((easySolved / total.toFloat()) * 360f, label = "easyAngle")
    val mediumAngle by animateFloatAsState((mediumSolved / total.toFloat()) * 360f, label = "mediumAngle")
    val hardAngle by animateFloatAsState((hardSolved / total.toFloat()) * 360f, label = "hardAngle")

    val ringColors = listOf(
        Color(0xFF16C3B0), // Easy - teal
        Color(0xFFFFB300), // Medium - yellow
        Color(0xFFEF5350)  // Hard - red
    )

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val stroke = 20f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            var startAngle = -90f

            drawArc(
                color = ringColors[0],
                startAngle = startAngle,
                sweepAngle = easyAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
            startAngle += easyAngle

            drawArc(
                color = ringColors[1],
                startAngle = startAngle,
                sweepAngle = mediumAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
            startAngle += mediumAngle

            drawArc(
                color = ringColors[2],
                startAngle = startAngle,
                sweepAngle = hardAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$solved/${problems.size}", style = MaterialTheme.typography.titleLarge)
            Text("Solved", style = MaterialTheme.typography.labelMedium)
            Text("$attempting Attempting", style = MaterialTheme.typography.bodySmall)
        }
    }
}