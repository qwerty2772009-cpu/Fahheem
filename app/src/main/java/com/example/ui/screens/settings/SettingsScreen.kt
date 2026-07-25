package com.example.ui.screens.settings

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.audio.AudioFeedbackManager
import com.example.data.model.*

@Composable
fun SettingsScreen(
    user: UserEntity?,
    onUpdateMascot: (MascotId) -> Unit,
    onUpdateMascotPosition: (MascotPosition) -> Unit,
    onUpdateLanguage: (AppLanguage) -> Unit,
    onToggleRamadanMode: (Boolean) -> Unit,
    onToggleExamMode: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val currentMascotId = MascotId.valueOf(user?.selectedMascotId ?: "FAHHEEM")
    val currentMascotPosition = MascotPosition.valueOf(user?.mascotPosition ?: "RIGHT")
    val currentLanguage = AppLanguage.fromCode(user?.language ?: "arz")

    var isRamadan by remember { mutableStateOf(user?.isRamadanMode ?: false) }
    var isExam by remember { mutableStateOf(user?.isExamMode ?: false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Character Studio Header & Selector
        item {
            Text(
                text = "استوديو الشخصيات والرفقاء 🎭",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "اختر رفيقك وتغيره في أي وقت دون فقدان أي بيانات",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(Mascots.ALL) { mascot ->
                    val isSelected = mascot.id == currentMascotId
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .width(140.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onUpdateMascot(mascot.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = mascot.imageResId),
                                contentDescription = mascot.nameAr,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mascot.nameAr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = mascot.titleAr,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Mascot Screen Position Preference
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("موقع الشريك على الشاشة 📍", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MascotPosition.entries.forEach { pos ->
                            FilterChip(
                                selected = pos == currentMascotPosition,
                                onClick = { onUpdateMascotPosition(pos) },
                                label = { Text(pos.name) }
                            )
                        }
                    }
                }
            }
        }

        // Language Switcher
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("لغة التطبيق 🌐", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = lang == currentLanguage,
                                onClick = { onUpdateLanguage(lang) },
                                label = { Text(lang.displayName) }
                            )
                        }
                    }
                }
            }
        }

        // Seasonal Modes Toggles (Ramadan & Exam)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الأوضاع الموسمية 🌙📝", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("وضع شهر رمضان المبارك 🌙", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("يتيح تفريغ ساعتين حول الإفطار وتعديل مواعيد المذاكرة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isRamadan,
                            onCheckedChange = {
                                isRamadan = it
                                onToggleRamadanMode(it)
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("وضع الامتحانات المكثف 📝", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("زيادة ساعات المراجعة وتقليل المهام الجانبية قبل الامتحانات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isExam,
                            onCheckedChange = {
                                isExam = it
                                onToggleExamMode(it)
                            }
                        )
                    }
                }
            }
        }

        // Dedicated Sounds & Haptic Feedback Section
        item {
            val context = LocalContext.current
            var uiSounds by AudioFeedbackManager.uiSoundsEnabled
            var notifSounds by AudioFeedbackManager.notificationSoundsEnabled
            var mascotSounds by AudioFeedbackManager.mascotSoundsEnabled
            var haptics by AudioFeedbackManager.hapticsEnabled
            var volume by AudioFeedbackManager.soundVolume

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الأصوات والاهتزازات (Sounds & Haptics) 🔊",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "مؤثرات صوتية هادئة وسريعة بدون موسيقى لزيادة التركيز",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound Volume Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مستوى صوت المؤثرات 🎧", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${(volume * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = volume,
                            onValueChange = {
                                volume = it
                                AudioFeedbackManager.playSlider()
                            },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("أصوات واجهة المستخدم 👆", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("نقرات الأزرار، التنقل والقوائم", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiSounds,
                            onCheckedChange = {
                                uiSounds = it
                                AudioFeedbackManager.playToggle()
                                AudioFeedbackManager.performLightTap(context)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("أصوات التنبيهات 🔔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("جرس هادئ ومميز للتذكيرات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifSounds,
                            onCheckedChange = {
                                notifSounds = it
                                AudioFeedbackManager.playToggle()
                                AudioFeedbackManager.performLightTap(context)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("أصوات تعبيرات المساعد (فهيم/فطين) 💬", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("مؤثرات صوتیة لطيفة ونادرة لشخصيتك المفضلة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = mascotSounds,
                            onCheckedChange = {
                                mascotSounds = it
                                AudioFeedbackManager.playToggle()
                                AudioFeedbackManager.performLightTap(context)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الاستجابة اللمسية والاهتزاز (Haptics) 📳", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("اهتزاز خفيف ومريح عند الضغط وإنجاز المهام", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = haptics,
                            onCheckedChange = {
                                haptics = it
                                AudioFeedbackManager.performConfirmationHaptic(context)
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Test Sound Effects Grid
                    Text("تجربة المؤثرات الصوتية والاهتزاز 🧪:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                AudioFeedbackManager.playTap()
                                AudioFeedbackManager.performLightTap(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("نقرة 👆", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                AudioFeedbackManager.playTaskComplete()
                                AudioFeedbackManager.performTaskCompleteHaptic(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إنجاز 🎉", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                AudioFeedbackManager.playNotificationBell()
                                AudioFeedbackManager.performConfirmationHaptic(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تنبيه 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                AudioFeedbackManager.playMascotSparkle()
                                AudioFeedbackManager.performConfirmationHaptic(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("المساعد 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                AudioFeedbackManager.playAchievementUnlocked()
                                AudioFeedbackManager.performAchievementHaptic(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("وسام 🏆", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Account & Logout
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج مع حفظ البيانات بالسحابة ☁️", fontWeight = FontWeight.Bold)
            }
        }
    }
}
