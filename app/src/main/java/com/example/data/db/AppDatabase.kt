package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        LessonScheduleEntity::class,
        AIMemoryEntity::class,
        ChatConversationEntity::class,
        StudySessionEntity::class,
        AchievementEntity::class,
        PrayerTimeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun lessonDao(): LessonDao
    abstract fun memoryDao(): AIMemoryDao
    abstract fun chatDao(): ChatDao
    abstract fun sessionDao(): StudySessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fahheem_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
