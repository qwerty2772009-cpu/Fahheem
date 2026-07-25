package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.audio.AudioFeedbackManager
import com.example.data.model.Mascot
import com.example.data.model.MascotEmotion
import com.example.data.model.MascotId
import com.example.data.model.MascotPosition
import kotlinx.coroutines.delay

@Composable
fun MascotView(
    mascot: Mascot,
    speechText: String? = null,
    emotion: MascotEmotion = MascotEmotion.IDLE,
    position: MascotPosition = MascotPosition.RIGHT,
    size: Dp = 130.dp,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Welcome Wave State (triggers upon launch/mascot change)
    var isWaving by remember(mascot.id) { mutableStateOf(true) }
    var entryAnimated by remember(mascot.id) { mutableStateOf(false) }

    LaunchedEffect(mascot.id) {
        entryAnimated = true
        AudioFeedbackManager.playMascotSparkle()
        delay(3200) // Wave duration 3.2 seconds then return to idle
        isWaving = false
    }

    // Entry Fade + Slide
    val entryAlpha by animateFloatAsState(
        targetValue = if (entryAnimated) 1f else 0f,
        animationSpec = tween(600, easing = LinearOutSlowInEasing),
        label = "EntryAlpha"
    )

    val entryOffsetY by animateFloatAsState(
        targetValue = if (entryAnimated) 0f else 28f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "EntryOffsetY"
    )

    // Personality Waving Duration & Angles
    val waveDuration = when (mascot.id) {
        MascotId.LOLY -> 200
        MascotId.ROCKY -> 320
        MascotId.BATTOOT -> 420
        MascotId.BEAR -> 850
        MascotId.FAHHEEM -> 500
    }

    val maxWaveAngle = when (mascot.id) {
        MascotId.LOLY -> 15f
        MascotId.ROCKY -> 11f
        MascotId.BATTOOT -> 7f
        MascotId.BEAR -> 4f
        MascotId.FAHHEEM -> 8f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MascotAnimations")

    // 1. Waving Oscillation Rotation
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = -maxWaveAngle,
        targetValue = maxWaveAngle,
        animationSpec = infiniteRepeatable(
            animation = tween(waveDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveRotation"
    )

    // 2. Idle Organic Body Sway
    val bodySwayAngle by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BodySwayAngle"
    )

    // 3. Idle Breathing Scale Pulse
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (emotion == MascotEmotion.SLEEPING) 1.05f else 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (emotion == MascotEmotion.SLEEPING) 2500 else 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathScale"
    )

    // 4. Vertical Float / Bounce according to emotion
    val floatTarget = when (emotion) {
        MascotEmotion.EXCITED, MascotEmotion.HAPPY -> -14f
        MascotEmotion.THINKING -> -8f
        MascotEmotion.SLEEPING, MascotEmotion.SAD -> 2f
        else -> -6f
    }

    val floatDuration = when (emotion) {
        MascotEmotion.EXCITED -> 350
        MascotEmotion.HAPPY -> 500
        MascotEmotion.SLEEPING -> 2800
        else -> 1800
    }

    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatTarget,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = floatDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MascotOffsetY"
    )

    val currentRotation = if (isWaving) waveRotation else bodySwayAngle
    val currentOffsetY = idleFloatY + entryOffsetY

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(entryAlpha),
        horizontalAlignment = when (position) {
            MascotPosition.LEFT -> Alignment.Start
            MascotPosition.RIGHT -> Alignment.End
            MascotPosition.TOP, MascotPosition.BOTTOM -> Alignment.CenterHorizontally
        }
    ) {
        if (!speechText.isNullOrBlank()) {
            AnimatedVisibility(
                visible = entryAnimated,
                enter = fadeIn(tween(400)) + scaleIn(tween(400))
            ) {
                MascotSpeechBubble(
                    speechText = speechText,
                    mascotName = mascot.nameAr,
                    isWaving = isWaving,
                    mascotId = mascot.id,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .widthIn(max = 280.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(y = currentOffsetY.dp)
                .rotate(currentRotation)
                .scale(breathScale)
                .clickable {
                    isWaving = true
                    when ((0..2).random()) {
                        0 -> AudioFeedbackManager.playMascotPop()
                        1 -> AudioFeedbackManager.playMascotSparkle()
                        else -> AudioFeedbackManager.playMascotBounce()
                    }
                    AudioFeedbackManager.performLightTap(context)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Authentic 2D Character Artwork — Zero background container, original artwork shape preserved 100%
            Image(
                painter = painterResource(id = mascot.imageResId),
                contentDescription = mascot.nameAr,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit
            )

            // Dynamic Emotion / Waving Floating Badges
            val badgeEmoji = when {
                isWaving -> when (mascot.id) {
                    MascotId.FAHHEEM -> "👋"
                    MascotId.LOLY -> "✨"
                    MascotId.ROCKY -> "😏"
                    MascotId.BEAR -> "💤"
                    MascotId.BATTOOT -> "🌸"
                }
                emotion == MascotEmotion.EXCITED -> "🎉"
                emotion == MascotEmotion.HAPPY -> "✨"
                emotion == MascotEmotion.THINKING -> "💡"
                emotion == MascotEmotion.SLEEPING -> "💤"
                emotion == MascotEmotion.SAD -> "🥺"
                else -> null
            }

            badgeEmoji?.let { emoji ->
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color.White),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = emoji,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MascotSpeechBubble(
    speechText: String,
    mascotName: String,
    isWaving: Boolean = false,
    mascotId: MascotId = MascotId.FAHHEEM,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.85f),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.95f)),
            shadowElevation = 8.dp,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"$speechText\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C1E),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Top Pill Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color.White),
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                if (isWaving) {
                    Text(
                        text = when (mascotId) {
                            MascotId.FAHHEEM -> "ترحيب فهيم 👋"
                            MascotId.LOLY -> "ترحيب لولي 🎉"
                            MascotId.ROCKY -> "ترحيب روكي 😏"
                            MascotId.BEAR -> "ترحيب دبدوب 💤"
                            MascotId.BATTOOT -> "ترحيب بطوط 🌸"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0056D2)
                    )
                } else {
                    Text(
                        text = "ذكاء $mascotName 🧠",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDB2777)
                    )
                }
            }
        }
    }
}
