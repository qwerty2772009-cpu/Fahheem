package com.example.ui.screens.schedule

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
import com.example.data.model.LessonScheduleEntity
import com.example.data.model.PrayerTimeEntity

@Composable
fun ScheduleScreen(
    lessons: List<LessonScheduleEntity>,
    prayers: List<PrayerTimeEntity>,
    isRamadanMode: Boolean,
    isExamMode: Boolean
) {
    var selectedDayOfWeek by remember { mutableIntStateOf(6) } // 6 = Saturday in Egypt

    val daysList = listOf(
        Pair(6, "السبت"),
        Pair(7, "الأحد"),
        Pair(1, "الإثنين"),
        Pair(2, "الثلاثاء"),
        Pair(3, "الأربعاء"),
        Pair(4, "الخميس"),
        Pair(5, "الجمعة")
    )

    val currentDayLessons = lessons.filter { it.dayOfWeek == selectedDayOfWeek }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Banners (Ramadan & Exam Modes)
        if (isRamadanMode || isExamMode) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isRamadanMode) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NightsStay, contentDescription = "Ramadan")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("وضع رمضان 🌙 (تفريغ الإفطار)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isExamMode) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Exam")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("وضع الامتحانات 📝 (مكثف)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Days Bar
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysList) { day ->
                    val isSelected = day.first == selectedDayOfWeek
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDayOfWeek = day.first },
                        label = { Text(day.second, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null
                    )
                }
            }
        }

        // Daily Prayer Times Bar
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مواعيد الصلاة اليومية 🕌",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "محدث آلياً",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        prayers.forEach { prayer ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(prayer.displayNameAr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(prayer.timeString, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Lessons & Schedule Items
        item {
            Text(
                text = "دروس ومواعيد هذا اليوم 📚",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (currentDayLessons.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EventAvailable,
                            contentDescription = "Free Day",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد دروس أو مواعيد في هذا اليوم!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "فرصة ممتازة للمراجعة أو الراحة مع فهيم 🌸",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(currentDayLessons) { lesson ->
            val colorHex = android.graphics.Color.parseColor(lesson.colorHex)
            val lessonBg = Color(colorHex)

            // Check if prayer overlaps lesson
            val overlappingPrayer = prayers.firstOrNull { prayer ->
                prayer.timeString >= lesson.startTime && prayer.timeString <= lesson.endTime
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = lessonBg.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, lessonBg, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lesson.subjectName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = lessonBg
                        )

                        Text(
                            text = "${lesson.startTime} - ${lesson.endTime}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "المدرس: ${lesson.teacherName} • ${lesson.location}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Small red indicator inside lesson if prayer overlaps
                    if (overlappingPrayer != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Prayer overlap",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "يتداخل مع صلاة ${overlappingPrayer.displayNameAr} (${overlappingPrayer.timeString})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
