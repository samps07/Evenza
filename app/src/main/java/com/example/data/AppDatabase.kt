package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [EventEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE countdown_events ADD COLUMN linkUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE countdown_events ADD COLUMN imageUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE countdown_events ADD COLUMN customRemindersJson TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "counter_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with realistic, helpful starter countdown events
                CoroutineScope(Dispatchers.IO).launch {
                    val now = System.currentTimeMillis()
                    val hour = 3600 * 1000L
                    val day = 24 * hour

                    val initialEvents = listOf(
                        EventEntity(
                            title = "Food Coupon",
                            description = "$15 off next dinner order at Bistro",
                            startTimeMillis = now - 3 * day,
                            targetTimeMillis = now + 4 * day,
                            icon = "🍔",
                            colorHex = "#FB923C",
                            category = "Food",
                            priority = 1,
                            customOrder = 0
                        ),
                        EventEntity(
                            title = "Movie Night",
                            description = "Sci-Fi IMAX screening starts at 8 PM",
                            startTimeMillis = now - 1 * day,
                            targetTimeMillis = now + 1 * day + 8 * hour,
                            icon = "🎬",
                            colorHex = "#EC4899",
                            category = "Entertainment",
                            priority = 2,
                            customOrder = 1
                        ),
                        EventEntity(
                            title = "Library Book Due",
                            description = "Return 'Thinking, Fast and Slow'",
                            startTimeMillis = now - 11 * day,
                            targetTimeMillis = now + 3 * day,
                            icon = "📚",
                            colorHex = "#38BDF8",
                            category = "Personal",
                            priority = 1,
                            customOrder = 2
                        ),
                        EventEntity(
                            title = "Amazon Gift Card",
                            description = "Birthday gift card expires end of month",
                            startTimeMillis = now - 12 * day,
                            targetTimeMillis = now + 18 * day,
                            icon = "🎁",
                            colorHex = "#A855F7",
                            category = "Shopping",
                            priority = 0,
                            customOrder = 3
                        ),
                        EventEntity(
                            title = "Train Ticket Departure",
                            description = "Platform 4 Express Train",
                            startTimeMillis = now - 4 * hour,
                            targetTimeMillis = now + 2 * hour,
                            icon = "🚆",
                            colorHex = "#EF4444",
                            category = "Travel",
                            priority = 2,
                            customOrder = 4
                        ),
                        EventEntity(
                            title = "Old Coffee Voucher",
                            description = "Free espresso offer",
                            startTimeMillis = now - 10 * day,
                            targetTimeMillis = now - 1 * day,
                            icon = "☕",
                            colorHex = "#94A3B8",
                            category = "Food",
                            priority = 0,
                            customOrder = 5
                        )
                    )
                    getDatabase(context).eventDao().insertEvents(initialEvents)
                }
            }
        }
    }
}
