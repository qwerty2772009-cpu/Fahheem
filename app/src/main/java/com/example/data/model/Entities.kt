package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String = "user_default",
    val name: String = "طالب فهمان",
    val email: String = "student@fahheem.ai",
    val grade: String = "الثانوية العامة (الصف الثالث)",
    val targetGradePercentage: Int = 98,
    val educationalSystem: String = "النظام المصري العام",
    val subjects: String = "كيمياء, فيزياء, أحياء, رياضيات, لغة عربية, لغة إنجليزية",
    val wakeUpTime: String = "06:30",
    val sleepTime: String = "23:00",
    val selectedMascotId: String = "FAHHEEM",
    val mascotPosition: String = "RIGHT",
    val language: String = "arz",
    val commitmentPercentage: Int = 85,
    val isRamadanMode: Boolean = false,
    val isExamMode: Boolean = false,
    val isVacationMode: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isAuthenticated: Boolean = true,
    val country: String = "مصر",
    val city: String = "القاهرة",
    val studyDurationMinutes: Int = 45,
    val breakDurationMinutes: Int = 10
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val startTime: String,
    val endTime: String,
    val estimatedDurationMinutes: Int = 45,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val notes: String = "",
    val colorHex: String = "#3B82F6",
    val isCompleted: Boolean = false,
    val isHomework: Boolean = false,
    val dateString: String, // YYYY-MM-DD
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_schedules")
data class LessonScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val teacherName: String = "",
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val location: String = "السنتر / أونلاين",
    val colorHex: String = "#6366F1",
    val isRecurring: Boolean = true,
    val hasHomeworkAssigned: Boolean = false
)

@Entity(tableName = "ai_memories")
data class AIMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // WEAK_SUBJECT, HABIT, GOAL, EMOTIONAL, PREFERENCE
    val memoryText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mascotId: String,
    val sender: String, // USER, MASCOT
    val messageText: String,
    val category: String = "CHAT", // CHAT, FADFADA, ONBOARDING
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val understandingLevel: String = "VERY_WELL", // VERY_WELL, SOMEWHAT, NOT_AT_ALL
    val focusScore: Int = 90,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val unlockedTimestamp: Long? = null
)

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey val name: String, // Fajr, Dhuhr, Asr, Maghrib, Isha
    val timeString: String, // e.g., "04:30"
    val displayNameAr: String
)
