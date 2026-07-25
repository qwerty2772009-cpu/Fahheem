package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FahheemAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceActive.value = true
        Toast.makeText(
            applicationContext,
            "تم تفعيل حارس المشتتات والتركيز من فهيم 🛡️",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (!isGuardEnabled.value) return

        if (SOCIAL_MEDIA_PACKAGES.containsKey(packageName)) {
            val appName = SOCIAL_MEDIA_PACKAGES[packageName] ?: "وسائل التواصل"
            
            val currentTime = System.currentTimeMillis()
            val lastOpen = appOpenTimestamps[packageName] ?: 0L
            val durationOpen = (currentTime - lastOpen) / 1000

            if (durationOpen > BLOCK_THRESHOLD_SECONDS) {
                // Extended usage reached: close app immediately to home screen
                _blockedCountToday.value = _blockedCountToday.value + 1
                
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "🛑 تنبيه من فهيم: تم تجاوز الوقت المسموح لـ $appName! تم إغلاق التطبيق لحمايتك وللتركيز على المذاكرة.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                performGlobalAction(GLOBAL_ACTION_HOME)
            } else {
                // First warning message
                if (lastOpen == 0L || (currentTime - lastOpen) > 60000L) {
                    appOpenTimestamps[packageName] = currentTime
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            applicationContext,
                            "⚠️ تنبيه من فهيم: فتحت $appName! سيتم إغلاقه تلقائياً خلال دقيقة لتستكمل مذاكرتك 🧠",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive

        private val _isGuardEnabled = MutableStateFlow(true)
        val isGuardEnabled: StateFlow<Boolean> = _isGuardEnabled

        private val _blockedCountToday = MutableStateFlow(3)
        val blockedCountToday: StateFlow<Int> = _blockedCountToday

        const val BLOCK_THRESHOLD_SECONDS = 30L // 30 seconds limit for demonstration/focus

        val appOpenTimestamps = mutableMapOf<String, Long>()

        val SOCIAL_MEDIA_PACKAGES = mapOf(
            "com.zhiliaoapp.musically" to "تيك توك (TikTok)",
            "com.ss.android.ugc.trill" to "تيك توك (TikTok)",
            "com.instagram.android" to "إنستجرام (Instagram)",
            "com.facebook.katana" to "فيسبوك (Facebook)",
            "com.twitter.android" to "تويتر / X",
            "com.snapchat.android" to "سناب شات (Snapchat)",
            "com.google.android.youtube" to "يوتيوب (YouTube)"
        )

        fun setGuardEnabled(enabled: Boolean) {
            _isGuardEnabled.value = enabled
        }
    }
}
