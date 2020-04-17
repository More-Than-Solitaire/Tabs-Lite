package com.gbros.tabslite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gbros.tabslite.utilities.DATABASE_NAME

/**
 * The Room database for this app
 */
@Database(entities = [TabFull::class, ChordVariation::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chordVariationDao(): ChordVariationDao
    abstract fun tabFullDao(): TabFullDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tabs ADD COLUMN transposed INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE garden_plantings")
                database.execSQL("DROP TABLE plants")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE chord_variation")
                database.execSQL("CREATE TABLE IF NOT EXISTS chord_variation (id TEXT NOT NULL, chord_id TEXT NOT NULL, chord_markers TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE chord_variation")
                database.execSQL("CREATE TABLE IF NOT EXISTS chord_variation (id TEXT NOT NULL, chord_id TEXT NOT NULL, note_chord_markers TEXT NOT NULL, open_chord_markers TEXT NOT NULL, muted_chord_markers TEXT NOT NULL, bar_chord_markers TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tabs ADD COLUMN favorite_time INTEGER DEFAULT NULL")
            }
        }


        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
        }
    }
}
