package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AdminUserEntity::class,
        LlmProviderEntity::class,
        TopicEntity::class,
        ResearchPackageEntity::class,
        SourceEntity::class,
        ArticleEntity::class,
        ArticleVersionEntity::class,
        QualityCheckEntity::class,
        ArticleImageEntity::class,
        BloggerPostEntity::class,
        FacebookPostEntity::class,
        ScheduledJobEntity::class,
        ApiLogEntity::class,
        ErrorLogEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "news_publisher.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
