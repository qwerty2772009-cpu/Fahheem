package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUserFlow(id: String = "user_default"): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUser(id: String = "user_default"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE user_profile SET selectedMascotId = :mascotId WHERE id = 'user_default'")
    suspend fun updateSelectedMascot(mascotId: String)

    @Query("UPDATE user_profile SET mascotPosition = :position WHERE id = 'user_default'")
    suspend fun updateMascotPosition(position: String)

    @Query("UPDATE user_profile SET language = :lang WHERE id = 'user_default'")
    suspend fun updateLanguage(lang: String)

    @Query("UPDATE user_profile SET commitmentPercentage = :percentage WHERE id = 'user_default'")
    suspend fun updateCommitmentPercentage(percentage: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dateString = :date ORDER BY startTime ASC")
    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, completed: Boolean)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lesson_schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllLessonsFlow(): Flow<List<LessonScheduleEntity>>

    @Query("SELECT * FROM lesson_schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getLessonsForDayFlow(dayOfWeek: Int): Flow<List<LessonScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonScheduleEntity): Long

    @Delete
    suspend fun deleteLesson(lesson: LessonScheduleEntity)
}

@Dao
interface AIMemoryDao {
    @Query("SELECT * FROM ai_memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<AIMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AIMemoryEntity)

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_conversations WHERE category = :category ORDER BY timestamp ASC")
    fun getChatFlow(category: String): Flow<List<ChatConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatConversationEntity)

    @Query("DELETE FROM chat_conversations WHERE category = :category")
    suspend fun clearCategoryChats(category: String)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_times")
    fun getPrayerTimesFlow(): Flow<List<PrayerTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayers: List<PrayerTimeEntity>)
}
