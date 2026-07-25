package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class FahheemRepository(private val db: AppDatabase) {

    val userDao: UserDao = db.userDao()
    val taskDao: TaskDao = db.taskDao()
    val lessonDao: LessonDao = db.lessonDao()
    val memoryDao: AIMemoryDao = db.memoryDao()
    val chatDao: ChatDao = db.chatDao()
    val sessionDao: StudySessionDao = db.sessionDao()
    val achievementDao: AchievementDao = db.achievementDao()
    val prayerDao: PrayerDao = db.prayerDao()

    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val allTasksFlow: Flow<List<TaskEntity>> = taskDao.getAllTasksFlow()
    val allLessonsFlow: Flow<List<LessonScheduleEntity>> = lessonDao.getAllLessonsFlow()
    val allMemoriesFlow: Flow<List<AIMemoryEntity>> = memoryDao.getAllMemoriesFlow()
    val allSessionsFlow: Flow<List<StudySessionEntity>> = sessionDao.getAllSessionsFlow()
    val allAchievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()
    val prayerTimesFlow: Flow<List<PrayerTimeEntity>> = prayerDao.getPrayerTimesFlow()

    fun getChatFlow(category: String): Flow<List<ChatConversationEntity>> = chatDao.getChatFlow(category)

    suspend fun getUser(): UserEntity {
        return userDao.getUser() ?: UserEntity().also { userDao.insertOrUpdateUser(it) }
    }

    suspend fun saveUser(user: UserEntity) {
        userDao.insertOrUpdateUser(user)
    }

    suspend fun updateSelectedMascot(mascotId: MascotId) {
        userDao.updateSelectedMascot(mascotId.name)
    }

    suspend fun updateMascotPosition(position: MascotPosition) {
        userDao.updateMascotPosition(position.name)
    }

    suspend fun updateLanguage(language: AppLanguage) {
        userDao.updateLanguage(language.code)
    }

    suspend fun updateCommitmentPercentage(percentage: Int) {
        userDao.updateCommitmentPercentage(percentage)
    }

    suspend fun addTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) {
        taskDao.updateTaskCompletion(taskId, completed)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun addLesson(lesson: LessonScheduleEntity): Long {
        return lessonDao.insertLesson(lesson)
    }

    suspend fun deleteLesson(lesson: LessonScheduleEntity) {
        lessonDao.deleteLesson(lesson)
    }

    suspend fun addChat(chat: ChatConversationEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun addAIMemory(memoryText: String, category: String) {
        memoryDao.insertMemory(AIMemoryEntity(memoryText = memoryText, category = category))
    }

    suspend fun logStudySession(subject: String, durationMinutes: Int, understanding: String) {
        sessionDao.insertSession(
            StudySessionEntity(
                subject = subject,
                durationMinutes = durationMinutes,
                understandingLevel = understanding
            )
        )
    }

    suspend fun seedInitialDataIfNeeded() {
        val currentUser = userDao.getUser()
        if (currentUser == null) {
            userDao.insertOrUpdateUser(UserEntity())
        }

        // Seed initial achievements if empty
        val initialAchievements = listOf(
            AchievementEntity(
                id = "first_session",
                titleAr = "أول جلسة تركيز 🎯",
                titleEn = "First Study Session",
                descriptionAr = "أتممت أول جلسة ذاكرة بنجاح مع شريك رحلتك.",
                descriptionEn = "Completed your first study session successfully.",
                iconName = "PlayArrow",
                isUnlocked = true,
                progress = 1.0f
            ),
            AchievementEntity(
                id = "homework_master",
                titleAr = "بطل الواجبات 📚",
                titleEn = "Homework Master",
                descriptionAr = "أنجزت 5 واجبات منسقة آلية في موعدها.",
                descriptionEn = "Completed 5 automatically scheduled homework tasks.",
                iconName = "CheckCircle",
                isUnlocked = false,
                progress = 0.4f
            ),
            AchievementEntity(
                id = "consistency_week",
                titleAr = "أسبوع بلا انقطاع 🔥",
                titleEn = "Consistent Week",
                descriptionAr = "التزمت بجدولك لمدة 7 أيام متتالية دون تأجيل.",
                descriptionEn = "Maintained your schedule for 7 consecutive days.",
                iconName = "Star",
                isUnlocked = false,
                progress = 0.7f
            ),
            AchievementEntity(
                id = "early_bird",
                titleAr = "الطائر المبكر 🌅",
                titleEn = "Early Bird",
                descriptionAr = "بدأت أول جلسة مذاكرة قبل الساعة 7 صباحاً.",
                descriptionEn = "Started your first study session before 7 AM.",
                iconName = "WbSunny",
                isUnlocked = true,
                progress = 1.0f
            ),
            AchievementEntity(
                id = "hundred_hours",
                titleAr = "نادي الـ 100 ساعة ⏳",
                titleEn = "100 Study Hours Club",
                descriptionAr = "جمعت 100 ساعة مذاكرة مركزة مع فهيم وأصدقائه.",
                descriptionEn = "Accumulated 100 total focused study hours.",
                iconName = "MilitaryTech",
                isUnlocked = false,
                progress = 0.25f
            )
        )
        achievementDao.insertAll(initialAchievements)

        // Seed initial prayer times
        val initialPrayers = listOf(
            PrayerTimeEntity("Fajr", "04:32", "الفجر"),
            PrayerTimeEntity("Dhuhr", "12:02", "الظهر"),
            PrayerTimeEntity("Asr", "15:35", "العصر"),
            PrayerTimeEntity("Maghrib", "18:48", "المغرب"),
            PrayerTimeEntity("Isha", "20:12", "العشاء")
        )
        prayerDao.insertPrayerTimes(initialPrayers)

        // Seed sample tasks and lessons for smooth initial layout
        val today = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())
        taskDao.insertTask(
            TaskEntity(
                title = "مراجعة باب الكيمياء العضوية",
                subject = "كيمياء",
                startTime = "14:00",
                endTime = "14:45",
                estimatedDurationMinutes = 45,
                priority = "HIGH",
                notes = "التركيز على تفاعلات الألكينات والتسمية.",
                colorHex = "#2563EB",
                isCompleted = false,
                dateString = today
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "حل واجب الفيزياء (الفصل الثالث)",
                subject = "فيزياء",
                startTime = "16:00",
                endTime = "17:00",
                estimatedDurationMinutes = 60,
                priority = "MEDIUM",
                notes = "حل مسائل قانون فاراداي والحث الكهرومغناطيسي.",
                colorHex = "#4F46E5",
                isCompleted = true,
                isHomework = true,
                dateString = today
            )
        )

        lessonDao.insertLesson(
            LessonScheduleEntity(
                subjectName = "درس الكيمياء (مستر أحمد)",
                teacherName = "أستاذ أحمد فاروق",
                dayOfWeek = 6, // Saturday
                startTime = "11:00",
                endTime = "13:00",
                location = "سنتر التفوق / الدقي",
                colorHex = "#0284C7"
            )
        )
        lessonDao.insertLesson(
            LessonScheduleEntity(
                subjectName = "درس الفيزياء (د. محمود)",
                teacherName = "د. محمود عبد المنعم",
                dayOfWeek = 7, // Sunday
                startTime = "17:00",
                endTime = "19:00",
                location = "أونلاين منصة التفوق",
                colorHex = "#6366F1"
            )
        )
    }
}
