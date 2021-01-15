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
@Database(entities = [TabFull::class, ChordVariation::class, Playlist::class, PlaylistEntry::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chordVariationDao(): ChordVariationDao
    abstract fun tabFullDao(): TabFullDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistEntryDao(): PlaylistEntryDao

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
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            // add the playlist functionality / data
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS playlist (id INTEGER NOT NULL, user_created INTEGER NOT NULL, title TEXT NOT NULL, date_created LONG NOT NULL, date_modified LONG NOT NULL, description TEXT NOT NULL, PRIMARY KEY(id))")
                database.execSQL("CREATE TABLE IF NOT EXISTS playlist_entry (id INTEGER NOT NULL, playlist_id INTEGER NOT NULL, tab_id INTEGER NOT NULL, next_entry_id INTEGER, prev_entry_id INTEGER, date_added LONG NOT NULL, transpose INT NOT NULL, PRIMARY KEY(id))")
            }
        }


        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                            MIGRATION_5_6, MIGRATION_6_7)
                    .build()
        }
    }
}
