package com.example.ui.screens.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import com.example.audio.AudioFeedbackManager
import com.example.service.FahheemAccessibilityService
import kotlinx.coroutines.delay

enum class PomodoroMode(
    val titleAr: String,
    val defaultSeconds: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val subtitleAr: String,
    val icon: String
) {
    FOCUS(
        titleAr = "تركيز عميق",
        defaultSeconds = 25 * 60,
        primaryColor = Color(0xFF0056D2),
        secondaryColor = Color(0xFF3B82F6),
        subtitleAr = "أغلق المشتتات واستعد للتركيز الكامل 🧠",
        icon = "🎯"
    ),
    SHORT_BREAK(
        titleAr = "استراحة قصيرة",
        defaultSeconds = 5 * 60,
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF10B981),
        subtitleAr = "اشرب مية، مدد جسمك وخذ نفس عميق ☕",
        icon = "☕"
    ),
    LONG_BREAK(
        titleAr = "استراحة طويلة",
        defaultSeconds = 15 * 60,
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFF8B5CF6),
        subtitleAr = "إنجاز رائع! خذ قسطاً أطول من الراحة 🌿",
        icon = "🌴"
    )
}

val SUBJECT_PRESETS = listOf(
    "الكيمياء العضوية",
    "الفيزياء الحديثة",
    "الأحياء والوراثة",
    "الرياضيات التطبيقية",
    "اللغة العربية (نحو وبلاغة)",
    "اللغة الإنجليزية",
    "التاريخ والجغرافيا"
)

@Composable
fun FocusScreen(
    onSessionCompleted: (subject: String, durationMinutes: Int, understandingLevel: String) -> Unit
) {
    var currentMode by remember { mutableStateOf(PomodoroMode.FOCUS) }
    var totalModeSeconds by remember { mutableIntStateOf(currentMode.defaultSeconds) }
    var secondsLeft by remember { mutableIntStateOf(currentMode.defaultSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    var completedPomodorosCount by remember { mutableIntStateOf(0) }
    var selectedSubject by remember { mutableStateOf(SUBJECT_PRESETS[0]) }
    var showSubjectDropdown by remember { mutableStateOf(false) }

    var showDistractionWarning by remember { mutableStateOf(false) }
    var showPostSessionDialog by remember { mutableStateOf(false) }
    var selectedUnderstanding by remember { mutableStateOf("VERY_WELL") }

    var isSoundEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val isAccessibilityActive by FahheemAccessibilityService.isServiceActive.collectAsState()
    val isGuardEnabled by FahheemAccessibilityService.isGuardEnabled.collectAsState()
    val blockedCountToday by FahheemAccessibilityService.blockedCountToday.collectAsState()

    var showSimulationStep1 by remember { mutableStateOf(false) }
    var showSimulationStep2 by remember { mutableStateOf(false) }
    var blockDurationMinutes by remember { mutableIntStateOf(120) }

    // Pulse animation when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Animated primary color shift based on mode
    val modeColor by animateColorAsState(
        targetValue = currentMode.primaryColor,
        animationSpec = tween(500),
        label = "modeColor"
    )

    // Timer countdown effect
    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        } else if (isRunning && secondsLeft == 0) {
            isRunning = false
            if (currentMode == PomodoroMode.FOCUS) {
                completedPomodorosCount += 1
                AudioFeedbackManager.playSessionComplete()
                AudioFeedbackManager.performAchievementHaptic(context)
                showPostSessionDialog = true
            } else {
                AudioFeedbackManager.playTimerFinish()
                AudioFeedbackManager.performConfirmationHaptic(context)
                // Automatically return to focus after break
                currentMode = PomodoroMode.FOCUS
                totalModeSeconds = currentMode.defaultSeconds
                secondsLeft = currentMode.defaultSeconds
            }
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val progress = if (totalModeSeconds > 0) (secondsLeft.toFloat() / totalModeSeconds.toFloat()) else 0f

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Title & Sound Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مؤقت البومودورو ⏱️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = currentMode.subtitleAr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable { isSoundEnabled = !isSoundEnabled }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = modeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Mode Selector Tabs (Focus, Short Break, Long Break)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PomodoroMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) mode.primaryColor else Color.Transparent,
                        animationSpec = tween(300),
                        label = "tabBg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tabBgColor)
                            .clickable {
                                isRunning = false
                                currentMode = mode
                                totalModeSeconds = mode.defaultSeconds
                                secondsLeft = mode.defaultSeconds
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(mode.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.titleAr,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Selector (When in Focus Mode)
        AnimatedVisibility(visible = currentMode == PomodoroMode.FOCUS) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { showSubjectDropdown = !showSubjectDropdown }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Subject",
                            tint = modeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المادة الحالية:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = selectedSubject,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = modeColor
                    )
                }
            }
        }

        // Dropdown Menu for Subject
        DropdownMenu(
            expanded = showSubjectDropdown,
            onDismissRequest = { showSubjectDropdown = false },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White)
        ) {
            SUBJECT_PRESETS.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        selectedSubject = subject
                        showSubjectDropdown = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Book, contentDescription = null, tint = modeColor)
                    }
                )
            }
        }

        // Pomodoro Cycle Progress Dots Indicator (e.g. 🍅 Session 2 of 4)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = modeColor.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, modeColor.copy(alpha = 0.2f)),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الدورة الحالية: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val cycleStep = (completedPomodorosCount % 4) + 1
                    for (i in 1..4) {
                        Surface(
                            shape = CircleShape,
                            color = if (i <= cycleStep) modeColor else Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(10.dp)
                        ) {}
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "($completedPomodorosCount مكتملة 🍅)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BIG CIRCULAR TIMER CANVAS DISPLAY
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Background Ring & Progress Arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val radius = diameter / 2f

                // Track
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Active Progress Arc
                val sweepAngle = progress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(currentMode.secondaryColor, currentMode.primaryColor, currentMode.secondaryColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Glass Surface Container Inside Ring
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.82f),
                border = BorderStroke(1.5.dp, Color.White),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isRunning) "المؤقت يعمل 🧠" else "جاهز للبدء ⚡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = modeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Increment / Decrement Buttons (+5 min, -5 min)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (secondsLeft > 5 * 60) {
                        secondsLeft -= 5 * 60
                        totalModeSeconds = secondsLeft
                    }
                },
                enabled = !isRunning,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text("-٥ دقائق", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            }

            OutlinedButton(
                onClick = {
                    secondsLeft += 5 * 60
                    totalModeSeconds = secondsLeft
                },
                enabled = !isRunning,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text("+٥ دقائق", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Duration Chips for Current Mode
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val presets = when (currentMode) {
                PomodoroMode.FOCUS -> listOf(15, 25, 30, 45, 50, 60)
                PomodoroMode.SHORT_BREAK -> listOf(3, 5, 10, 15)
                PomodoroMode.LONG_BREAK -> listOf(15, 20, 30)
            }

            presets.forEach { durationMin ->
                val isSelectedPreset = (totalModeSeconds == durationMin * 60)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelectedPreset) modeColor else Color.White.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, if (isSelectedPreset) modeColor else Color.White),
                    modifier = Modifier
                        .clickable {
                            isRunning = false
                            totalModeSeconds = durationMin * 60
                            secondsLeft = durationMin * 60
                        }
                ) {
                    Text(
                        text = "$durationMin د",
                        fontSize = 11.sp,
                        fontWeight = if (isSelectedPreset) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelectedPreset) Color.White else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MAIN CONTROL BUTTONS (Play/Pause, Reset, Skip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clickable {
                        AudioFeedbackManager.playTap()
                        AudioFeedbackManager.performLightTap(context)
                        isRunning = false
                        secondsLeft = totalModeSeconds
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Play/Pause Button
            Button(
                onClick = {
                    AudioFeedbackManager.playToggle()
                    AudioFeedbackManager.performLightTap(context)
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(170.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "إيقاف مؤقت" else "ابدأ الآن 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Skip Button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clickable {
                        isRunning = false
                        if (currentMode == PomodoroMode.FOCUS) {
                            currentMode = PomodoroMode.SHORT_BREAK
                        } else {
                            currentMode = PomodoroMode.FOCUS
                        }
                        totalModeSeconds = currentMode.defaultSeconds
                        secondsLeft = currentMode.defaultSeconds
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACCESSIBILITY SERVICE DISTRACTION GUARD CARD
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.85f),
            border = BorderStroke(1.5.dp, Color.White),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF0F3),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Accessibility Guard",
                                    tint = Color(0xFFDB2777),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "حارس إمكانية الوصول للمشتتات 🛡️",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E)
                            )
                            Text(
                                text = "إغلاق تطبيقات السوشيال ميديا تلقائياً",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAccessibilityActive) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isAccessibilityActive) "مفعل 🟢" else "إعدادات النظام ⚙️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAccessibilityActive) Color(0xFF15803D) else Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Feature Highlights
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ينبهك المساعد الشخصي (فهيم) فور فتح أي تطبيق مشتت.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عند الاستخدام لـ ٣٠ ثانية: يُغلق التطبيق وتعود للشاشة الرئيسية.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "يُمنع إعادة الفتح لمدة ساعتين حسب جدولك وروتين مذاكرتك.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // Guarded Apps Chips
                Column {
                    Text(
                        text = "التطبيقات المراقبة بحجم النظام:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("تيك توك", "إنستجرام", "فيسبوك", "X", "سناب شات", "يوتيوب").forEach { app ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = app,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button to open system settings for Accessibility
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF0056D2)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعدادات إمكانية الوصول ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0056D2))
                    }

                    // Button to test simulation of assistant warning + auto close
                    Button(
                        onClick = { showSimulationStep1 = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تجربة تحذير المساعد 🤖", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // SIMULATION STEP 1: Personal Assistant Warning Notification Dialog
    if (showSimulationStep1) {
        AlertDialog(
            onDismissRequest = { showSimulationStep1 = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF0F3),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🤖", fontSize = 28.sp)
                    }
                }
            },
            title = {
                Text(
                    text = "⚠️ تنبيه عاجل من مساعدك فهيم",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1A1C1E)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📱 التطبيق المكتشف: إنستجرام (Instagram)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0056D2),
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "\"يا بطل، أنت فتحت السوشيال ميديا أثناء وقت المذاكرة! قدامك ٣٠ ثانية تقفل التطبيق بنفسك أو سيقوم المساعد بإغلاقه تلقائياً لمنع تشتيت عقلك.\"",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSimulationStep1 = false
                        showSimulationStep2 = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("محاكاة تجاوز الوقت واستجابة الإغلاق 🛑", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulationStep1 = false }) {
                    Text("إلغاء", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // SIMULATION STEP 2: App Closed & Locked Status Dialog
    if (showSimulationStep2) {
        AlertDialog(
            onDismissRequest = { showSimulationStep2 = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(30.dp))
                    }
                }
            },
            title = {
                Text(
                    text = "🛑 تم إغلاق التطبيق وقفل إعادة الفتح",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تم تنفيذ أمر العودة للشاشة الرئيسية (GLOBAL_ACTION_HOME) وحظر فتح إنستجرام.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1A1C1E)
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "متبقي على إمكانية إعادة الفتح:",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "01:59:59 ساعتين",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "حسب روتينك اليومي وجلسة التركيز القادمة 🧠",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSimulationStep2 = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("العودة إلى جلسة التركيز والمذاكرة ⚡", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Post Session Reflection Dialog
    if (showPostSessionDialog) {
        AlertDialog(
            onDismissRequest = { showPostSessionDialog = false },
            icon = {
                Text("🎉", fontSize = 40.sp)
            },
            title = {
                Text(
                    text = "عاش جداً يا بطل! أتممت الجلسة 🎯",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "إلى أي مدى استوعبت مادة $selectedSubject خلال الـ ${totalModeSeconds / 60} دقيقة؟",
                        fontSize = 13.sp,
                        color = Color(0xFF1A1C1E)
                    )

                    FilterChip(
                        selected = selectedUnderstanding == "VERY_WELL",
                        onClick = { selectedUnderstanding = "VERY_WELL" },
                        label = { Text("فهمت ممتاز جداً وبسهولة! 🌟", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = selectedUnderstanding == "SOMEWHAT",
                        onClick = { selectedUnderstanding = "SOMEWHAT" },
                        label = { Text("فهمت نص نص محتاج مراجعة بسيطة 😐", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = selectedUnderstanding == "NOT_AT_ALL",
                        onClick = { selectedUnderstanding = "NOT_AT_ALL" },
                        label = { Text("لم أفهم جيداً وأحتاج شرحاً أسهل ❌", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSessionCompleted(selectedSubject, totalModeSeconds / 60, selectedUnderstanding)
                        showPostSessionDialog = false
                        // Automatically switch to Break
                        currentMode = if (completedPomodorosCount % 4 == 0) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
                        totalModeSeconds = currentMode.defaultSeconds
                        secondsLeft = currentMode.defaultSeconds
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (completedPomodorosCount % 4 == 0) "حفظ وابدأ استراحة طويلة (15 دقيقة) ☕" else "حفظ وابدأ استراحة قصيرة (5 دقائق) ☕",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
