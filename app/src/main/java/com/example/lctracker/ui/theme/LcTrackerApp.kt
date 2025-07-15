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
import kotlinx.coroutines.launch

// Hardcoded for now; could move to a separate file later
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
    val ctx = LocalContext.current
    val ds = remember { DataStoreManager(ctx) }
    val scope = rememberCoroutineScope()

    var problemList by remember { mutableStateOf(listOf<Problem>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var diffLevel by remember { mutableStateOf(Difficulty.EASY) }
    var filterLevel by remember { mutableStateOf<Difficulty?>(null) }

    // Load data initially
    LaunchedEffect(Unit) {
        try {
            ds.problemsFlow.collect {
                problemList = it
            }
        } catch (e: Exception) {
            e.printStackTrace() // TODO: Better error handling
        }
    }

    val displayList = filterLevel?.let {
        problemList.filter { prob -> prob.difficulty == it }
    } ?: problemList

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPad ->
        Column(modifier = Modifier.padding(innerPad).padding(16.dp)) {
            Text("LeetCode Tracker", fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))

            if (problemList.isNotEmpty()) {
                ProblemStatsPie(problemList)
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Difficulty.values().forEach { d ->
                        FilterChip(
                            selected = filterLevel?.name == d.name,
                            onClick = {
                                filterLevel = if (filterLevel == d) null else d
                            },
                            label = { Text(d.name) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            LazyColumn {
                items(displayList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                problemList = problemList.map {
                                    if (it.title == item.title) it.copy(isSolved = !it.isSolved) else it
                                }
                                scope.launch {
                                    ds.saveProblems(problemList)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isSolved) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title, modifier = Modifier.weight(1f))
                            Text(
                                if (item.isSolved) "Done" else "Pending",
                                color = if (item.isSolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showAddDialog = false
                        inputTitle = ""
                        diffLevel = Difficulty.EASY
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (inputTitle.isNotBlank()) {
                                val upd = problemList + Problem(inputTitle.trim(), false, diffLevel)
                                problemList = upd
                                scope.launch {
                                    ds.saveProblems(upd)
                                }
                                inputTitle = ""
                                diffLevel = Difficulty.EASY
                                showAddDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddDialog = false
                            inputTitle = ""
                            diffLevel = Difficulty.EASY
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("New Problem") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = inputTitle,
                                onValueChange = { inputTitle = it },
                                label = { Text("Title") },
                                singleLine = true
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Difficulty:")
                            Difficulty.values().forEach { d ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { diffLevel = d }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = diffLevel == d,
                                        onClick = { diffLevel = d }
                                    )
                                    Text(d.name)
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
fun ProblemStatsPie(probs: List<Problem>) {
    val total = probs.size.coerceAtLeast(1) // avoid div by zero
    val easyCt = probs.count { it.difficulty == Difficulty.EASY && it.isSolved }
    val medCt = probs.count { it.difficulty == Difficulty.MEDIUM && it.isSolved }
    val hardCt = probs.count { it.difficulty == Difficulty.HARD && it.isSolved }
    val solved = probs.count { it.isSolved }
    val remain = probs.count { !it.isSolved }

    val angleEasy by animateFloatAsState((easyCt / total.toFloat()) * 360f, label = "easy")
    val angleMed by animateFloatAsState((medCt / total.toFloat()) * 360f, label = "med")
    val angleHard by animateFloatAsState((hardCt / total.toFloat()) * 360f, label = "hard")

    val colors = listOf(Color(0xFF4DB6AC), Color(0xFFFFCA28), Color(0xFFE57373))

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val stroke = 20f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            var start = -90f

            drawArc(
                color = colors[0],
                startAngle = start,
                sweepAngle = angleEasy,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
            start += angleEasy

            drawArc(
                color = colors[1],
                startAngle = start,
                sweepAngle = angleMed,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
            start += angleMed

            drawArc(
                color = colors[2],
                startAngle = start,
                sweepAngle = angleHard,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$solved/${probs.size}", style = MaterialTheme.typography.titleLarge)
            Text("Solved", style = MaterialTheme.typography.labelMedium)
            Text("$remain To Go", style = MaterialTheme.typography.bodySmall)
        }
    }
}