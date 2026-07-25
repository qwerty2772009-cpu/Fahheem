package com.example.ui.screens.stats

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySessionEntity

@Composable
fun StatsScreen(
    sessions: List<StudySessionEntity>,
    commitmentPercentage: Int
) {
    var selectedReportPeriod by remember { mutableStateOf("WEEKLY") } // DAILY, WEEKLY, MONTHLY

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقارير وفحوصات الأداء 📊",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedReportPeriod == "DAILY",
                        onClick = { selectedReportPeriod = "DAILY" },
                        label = { Text("يومي") }
                    )
                    FilterChip(
                        selected = selectedReportPeriod == "WEEKLY",
                        onClick = { selectedReportPeriod = "WEEKLY" },
                        label = { Text("أسبوعي") }
                    )
                    FilterChip(
                        selected = selectedReportPeriod == "MONTHLY",
                        onClick = { selectedReportPeriod = "MONTHLY" },
                        label = { Text("شهري") }
                    )
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = "Hours", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("مجموع ساعات المذاكرة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("38.5 ساعة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Commitment", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("معدل الالتزام", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("$commitmentPercentage%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // Subject Breakdown Visual Bar Chart
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "توزيع الساعات حسب المواد 📚",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val subjectStats = listOf(
                        Triple("الكيمياء", 12f, "#2563EB"),
                        Triple("الفيزياء", 10f, "#4F46E5"),
                        Triple("الأحياء", 8.5f, "#059669"),
                        Triple("الرياضيات", 5f, "#D97706"),
                        Triple("اللغة العربية", 3f, "#EC4899")
                    )

                    subjectStats.forEach { (subject, hours, hex) ->
                        val barColor = Color(android.graphics.Color.parseColor(hex))
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(subject, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$hours ساعة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (hours / 15f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Key AI Insights & Predictions
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI Prediction", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحليلات وتوقعات فهيم الذكية 🧠",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("• المادة الأكثر إنجازاً: الكيمياء (أداء ممتاز وسريع).", fontSize = 13.sp)
                    Text("• المادة الأكثر تأجيلاً: الرياضيات (يفضل المذاكرة بعد الغداء فوراً).", fontSize = 13.sp)
                    Text("• تاريخ إنهاء المنهج المتوقع: 15 مايو القادم بنسبة نجاح 98%.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
