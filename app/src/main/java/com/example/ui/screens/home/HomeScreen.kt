package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.audio.AudioFeedbackManager
import com.example.data.model.*
import com.example.ui.components.MascotView
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    user: UserEntity?,
    speechBubbleText: String,
    tasks: List<TaskEntity>,
    lessons: List<LessonScheduleEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onNavigateToFadfada: () -> Unit,
    onNavigateToFocusTimer: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    val mascot = Mascots.getById(MascotId.valueOf(user?.selectedMascotId ?: "FAHHEEM"))
    val mascotPosition = MascotPosition.valueOf(user?.mascotPosition ?: "RIGHT")
    val commitmentScore = user?.commitmentPercentage ?: 85

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))
    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
    val currentDayName = dayFormat.format(calendar.time)
    val currentDateStr = dateFormat.format(calendar.time)

    val greetingHeader = when {
        hour in 5..11 -> "صباح الفل يا ${user?.name ?: "بطل"} 👋"
        hour in 12..16 -> "أهلاً ومساء الخير يا ${user?.name ?: "بطل"} ☀️"
        hour in 17..21 -> "مساء الجمال يا ${user?.name ?: "بطل"} ✨"
        else -> "يا سهران يا مجتهد يا ${user?.name ?: "بطل"} 🌙"
    }

    val mascotEmotion = when {
        hour in 22..23 || hour in 0..4 -> MascotEmotion.SLEEPING
        commitmentScore >= 85 -> MascotEmotion.EXCITED
        commitmentScore >= 70 -> MascotEmotion.HAPPY
        commitmentScore >= 50 -> MascotEmotion.THINKING
        else -> MascotEmotion.SAD
    }

    val incompleteTasks = tasks.filter { !it.isCompleted }
    val currentTask = incompleteTasks.firstOrNull()
    val nextLesson = lessons.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date & Commitment Header Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$currentDayName، $currentDateStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0056D2)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = greetingHeader,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    // Commitment badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Commitment",
                                tint = Color(0xFF0056D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التزامك: $commitmentScore%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0056D2)
                            )
                        }
                    }
                }
            }
        }

        // Animated Mascot & Dynamic Speech Bubble
        item {
            MascotView(
                mascot = mascot,
                speechText = speechBubbleText,
                emotion = mascotEmotion,
                position = mascotPosition,
                size = 140.dp,
                onClick = onNavigateToFadfada
            )
        }

        // Current Task / Action Hero Banner (Answers "What should I do right now?")
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF0056D2),
                shadowElevation = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "المهمة الحالية ⚡",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Button(
                            onClick = onNavigateToFocusTimer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0056D2)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("ابدأ الجلسة ⏱️", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentTask?.title ?: "جميع مهامك مكتملة! خذ قسطاً من الراحة 🌸",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (currentTask != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${currentTask.estimatedDurationMinutes} دقيقة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المادة: ${currentTask.subject}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Next Event & Homework Warning Card
        if (nextLesson != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE0EDFF),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Lesson",
                                    tint = Color(0xFF0056D2),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الدرس القادم: ${nextLesson.subjectName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Text(
                                text = "الساعة ${nextLesson.startTime} • ${nextLesson.location}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0EDFF)
                        ) {
                            Text(
                                text = "جاهز الشنطة 🎒",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0056D2),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Today's Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مهام اليوم 📋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tasks.count { it.isCompleted }}/${tasks.size} مكتمل",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(tasks) { task ->
            val context = LocalContext.current
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!task.isCompleted) {
                            if (task.isHomework) AudioFeedbackManager.playHomeworkComplete()
                            else AudioFeedbackManager.playTaskComplete()
                            AudioFeedbackManager.performTaskCompleteHaptic(context)
                        } else {
                            AudioFeedbackManager.playCheckbox()
                            AudioFeedbackManager.performLightTap(context)
                        }
                        onToggleTask(task)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = {
                            if (!task.isCompleted) {
                                if (task.isHomework) AudioFeedbackManager.playHomeworkComplete()
                                else AudioFeedbackManager.playTaskComplete()
                                AudioFeedbackManager.performTaskCompleteHaptic(context)
                            } else {
                                AudioFeedbackManager.playCheckbox()
                                AudioFeedbackManager.performLightTap(context)
                            }
                            onToggleTask(task)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0056D2),
                            uncheckedColor = Color(0xFF94A3B8)
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (task.isCompleted) Color(0xFF94A3B8) else Color(0xFF1A1C1E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${task.subject} • ${task.startTime} - ${task.endTime}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    if (task.isHomework) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF0F3)
                        ) {
                            Text(
                                text = "واجب 📚",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color(0xFFDB2777)
                            )
                        }
                    }
                }
            }
        }

        // Quick Fadfada Emotional Support Button
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFadfada() }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0EDFF),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Fadfada",
                                    tint = Color(0xFF0056D2),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "مضغوط أو زهقان؟ فضفض لـ ${mascot.nameAr} 💬",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mascot.introQuoteEg,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = Color(0xFF0056D2)
                    )
                }
            }
        }
    }
}
