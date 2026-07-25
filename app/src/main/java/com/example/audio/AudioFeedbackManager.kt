package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object AudioFeedbackManager {
    private const val SAMPLE_RATE = 22050

    val uiSoundsEnabled = mutableStateOf(true)
    val notificationSoundsEnabled = mutableStateOf(true)
    val mascotSoundsEnabled = mutableStateOf(true)
    val hapticsEnabled = mutableStateOf(true)
    val soundVolume = mutableStateOf(0.8f)

    private val scope = CoroutineScope(Dispatchers.Default)

    private enum class ToneType { SINE, TRIANGLE, BELL }

    /**
     * Synthesizes short, soft PCM tones with exponential attack/decay envelope
     * to eliminate clicks and ensure a pleasant, modern, non-distracting sound.
     */
    private fun playSynthTone(
        frequencies: DoubleArray,
        durationMs: Int,
        type: ToneType = ToneType.SINE,
        frequencySlideTo: DoubleArray? = null
    ) {
        if (soundVolume.value <= 0.01f) return

        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val vol = soundVolume.value.coerceIn(0f, 1f)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = when {
                        progress < 0.1 -> progress / 0.1 // Attack
                        else -> (1.0 - (progress - 0.1) / 0.9) * (1.0 - (progress - 0.1) / 0.9) // Exponential Decay
                    }

                    var sampleValue = 0.0
                    frequencies.forEachIndexed { index, startFreq ->
                        val endFreq = frequencySlideTo?.getOrNull(index) ?: startFreq
                        val currentFreq = startFreq + (endFreq - startFreq) * progress
                        val phase = 2.0 * PI * currentFreq * i / SAMPLE_RATE

                        val rawWave = when (type) {
                            ToneType.SINE -> sin(phase)
                            ToneType.TRIANGLE -> (2.0 / PI) * Math.asin(sin(phase))
                            ToneType.BELL -> sin(phase) + 0.3 * sin(2.0 * phase) + 0.1 * sin(3.0 * phase)
                        }
                        sampleValue += rawWave
                    }

                    val normalizedSample = (sampleValue / frequencies.size.coerceAtLeast(1)) * envelope * vol * Short.MAX_VALUE
                    buffer[i] = normalizedSample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Stop and release AudioTrack after playback finishes
                scope.launch {
                    kotlinx.coroutines.delay(durationMs.toLong() + 50L)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (e: Exception) {
                        // Ignore cleanup exceptions
                    }
                }
            } catch (e: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    // --- UI SOUND EFFECTS ---
    fun playTap() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(350.0), 50, ToneType.SINE, doubleArrayOf(500.0))
    }

    fun playToggle() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(400.0), 60, ToneType.SINE, doubleArrayOf(650.0))
    }

    fun playCheckbox() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(523.25), 80, ToneType.SINE, doubleArrayOf(659.25))
    }

    fun playSlider() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(440.0), 30, ToneType.SINE)
    }

    fun playBack() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(380.0), 50, ToneType.SINE, doubleArrayOf(220.0))
    }

    // --- STUDY & TASKS SOUND EFFECTS ---
    fun playTaskComplete() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(523.25, 659.25, 783.99), 220, ToneType.BELL)
    }

    fun playHomeworkComplete() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(587.33, 739.99, 880.00), 250, ToneType.BELL)
    }

    fun playSessionComplete() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(659.25, 830.61, 987.77, 1318.51), 300, ToneType.BELL)
    }

    fun playTimerFinish() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(880.0), 280, ToneType.BELL)
    }

    fun playAchievementUnlocked() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 300, ToneType.BELL)
    }

    fun playDailyGoal() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(659.25, 880.00, 1174.66), 260, ToneType.BELL)
    }

    fun playAllTasksDone() {
        if (!uiSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 300, ToneType.BELL)
    }

    // --- NOTIFICATIONS ---
    fun playNotificationBell() {
        if (!notificationSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(783.99), 280, ToneType.BELL)
    }

    // --- AI & MASCOT SOUNDS ---
    fun playMascotPop() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(440.0), 50, ToneType.SINE, doubleArrayOf(880.0))
    }

    fun playMascotSparkle() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(1046.50, 1318.51, 1567.98), 120, ToneType.BELL)
    }

    fun playMascotBounce() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(300.0), 120, ToneType.SINE, doubleArrayOf(600.0))
    }

    fun playMascotChime() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(659.25, 880.00), 150, ToneType.BELL)
    }

    fun playMascotThinking() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(350.0, 450.0), 100, ToneType.SINE)
    }

    fun playMascotYawn() {
        if (!mascotSoundsEnabled.value) return
        playSynthTone(doubleArrayOf(320.0), 220, ToneType.SINE, doubleArrayOf(160.0))
    }

    // --- HAPTIC FEEDBACK ---
    fun performLightTap(context: Context) {
        if (!hapticsEnabled.value) return
        vibrate(context, 12)
    }

    fun performTaskCompleteHaptic(context: Context) {
        if (!hapticsEnabled.value) return
        vibrate(context, 35)
    }

    fun performConfirmationHaptic(context: Context) {
        if (!hapticsEnabled.value) return
        vibrate(context, 25)
    }

    fun performAchievementHaptic(context: Context) {
        if (!hapticsEnabled.value) return
        vibratePattern(context, longArrayOf(0, 30, 50, 60))
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore if hardware unavailable
        }
    }

    private fun vibratePattern(context: Context, pattern: LongArray) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
