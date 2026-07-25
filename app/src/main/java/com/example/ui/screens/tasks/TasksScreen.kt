package com.example.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onAddNewTask: (title: String, subject: String, startTime: String, endTime: String, isHomework: Boolean) -> Unit,
    onPostLessonCheck: (attended: Boolean, teacherAssignedHomework: Boolean, subjectName: String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, HOMEWORK, COMPLETED
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showLessonCheckDialog by remember { mutableStateOf(false) }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("كيمياء") }
    var newTaskStartTime by remember { mutableStateOf("15:00") }
    var newTaskEndTime by remember { mutableStateOf("16:00") }
    var isHomeworkTask by remember { mutableStateOf(false) }

    val filteredTasks = when (selectedFilter) {
        "HOMEWORK" -> tasks.filter { it.isHomework }
        "COMPLETED" -> tasks.filter { it.isCompleted }
        else -> tasks
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showLessonCheckDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.School, contentDescription = "Lesson Check")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إدارة المهام والواجبات 📝",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("الكل (${tasks.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "HOMEWORK",
                        onClick = { selectedFilter = "HOMEWORK" },
                        label = { Text("الواجبات 📚 (${tasks.count { it.isHomework }})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "COMPLETED",
                        onClick = { selectedFilter = "COMPLETED" },
                        label = { Text("المكتمل ✅ (${tasks.count { it.isCompleted }})") }
                    )
                }
            }

            items(filteredTasks) { task ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleTask(task) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${task.subject} • ${task.startTime} - ${task.endTime} • الأولوية: ${task.priority}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (task.isHomework) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "واجب مبرمج 🤖",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("إضافة مهمة جديدة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("عنوان المهمة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskSubject,
                        onValueChange = { newTaskSubject = it },
                        label = { Text("المادة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTaskStartTime,
                            onValueChange = { newTaskStartTime = it },
                            label = { Text("البداية") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newTaskEndTime,
                            onValueChange = { newTaskEndTime = it },
                            label = { Text("النهاية") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isHomeworkTask,
                            onCheckedChange = { isHomeworkTask = it }
                        )
                        Text("تعيين كـ واجب مدرسي/درس")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        onAddNewTask(newTaskTitle, newTaskSubject, newTaskStartTime, newTaskEndTime, isHomeworkTask)
                        newTaskTitle = ""
                        showAddTaskDialog = false
                    }
                }) {
                    Text("حفظ المهمة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Post-Lesson Homework Auto-Scheduler Check
    if (showLessonCheckDialog) {
        var attended by remember { mutableStateOf(true) }
        var hasHomework by remember { mutableStateOf(true) }
        var subjectName by remember { mutableStateOf("الكيمياء") }

        AlertDialog(
            onDismissRequest = { showLessonCheckDialog = false },
            title = { Text("متابعة الدرس الذكية 🏫") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("هل حضرت درس الكيمياء اليوم؟")
                    Row {
                        FilterChip(selected = attended, onClick = { attended = true }, label = { Text("نعم حضرت ✅") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = !attended, onClick = { attended = false }, label = { Text("لم أحضر ❌") })
                    }

                    if (attended) {
                        Text("هل طلب الأستاذ واجب جديد؟")
                        Row {
                            FilterChip(selected = hasHomework, onClick = { hasHomework = true }, label = { Text("أعطانا واجب 📚") })
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(selected = !hasHomework, onClick = { hasHomework = false }, label = { Text("بدون واجب 🌸") })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onPostLessonCheck(attended, hasHomework, subjectName)
                    showLessonCheckDialog = false
                }) {
                    Text("جدولة الواجب آلياً 🤖")
                }
            }
        )
    }
}
