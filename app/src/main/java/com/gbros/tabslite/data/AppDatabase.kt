package com.gbros.tabslite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.playlist.DataPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabDataType

const val DATABASE_NAME = "local-tabs-db"

/**
 * The Room database for this app
 */
@Database(entities = [TabDataType::class, ChordVariation::class, Playlist::class, DataPlaylistEntry::class, Preference::class, SearchSuggestions::class], version = 15)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataAccess(): DataAccess

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tabs ADD COLUMN transposed INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE garden_plantings")
                db.execSQL("DROP TABLE plants")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE chord_variation")
                db.execSQL("CREATE TABLE IF NOT EXISTS chord_variation (id TEXT NOT NULL, chord_id TEXT NOT NULL, chord_markers TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE chord_variation")
                db.execSQL("CREATE TABLE IF NOT EXISTS chord_variation (id TEXT NOT NULL, chord_id TEXT NOT NULL, note_chord_markers TEXT NOT NULL, open_chord_markers TEXT NOT NULL, muted_chord_markers TEXT NOT NULL, bar_chord_markers TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tabs ADD COLUMN favorite_time INTEGER DEFAULT NULL")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            // add the playlist functionality / data
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS playlist (id INTEGER NOT NULL, user_created INTEGER NOT NULL, title TEXT NOT NULL, date_created INTEGER NOT NULL, date_modified INTEGER NOT NULL, description TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS playlist_entry (id INTEGER NOT NULL, playlist_id INTEGER NOT NULL, tab_id INTEGER NOT NULL, next_entry_id INTEGER, prev_entry_id INTEGER, date_added INTEGER NOT NULL, transpose INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            // migrate favorites over to the playlist with special ID -1
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT INTO playlist_entry (playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose) SELECT -1, id, NULL, NULL, favorite_time, transposed FROM tabs WHERE favorite IS 1")
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            // rename playlist_entry.id to playlist_entry.entry_id
            // remove unused columns from tabs table
            override fun migrate(db: SupportSQLiteDatabase) {
                // create new temp table
                db.execSQL("CREATE TABLE IF NOT EXISTS playlist_entry_new (entry_id INTEGER NOT NULL, playlist_id INTEGER NOT NULL, tab_id INTEGER NOT NULL, next_entry_id INTEGER, prev_entry_id INTEGER, date_added INTEGER NOT NULL, transpose INTEGER NOT NULL, PRIMARY KEY(entry_id))")

                // copy data from old table to new
                db.execSQL("INSERT INTO playlist_entry_new (entry_id, playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose) SELECT id, playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose FROM playlist_entry")

                // delete old playlist_entry table
                db.execSQL("DROP TABLE playlist_entry")

                // rename new table to playlist_entry
                db.execSQL("ALTER TABLE playlist_entry_new RENAME TO playlist_entry")
            }
        }
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            // rename playlist_entry.id to playlist_entry.entry_id
            // remove unused columns from tabs table
            override fun migrate(db: SupportSQLiteDatabase) {
                // ***** drop favorite, favorite_time, and transposed columns from 'tabs' table *****
                // Create new table with columns removed
                db.execSQL("CREATE TABLE tabs_new (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "song_id INTEGER NOT NULL DEFAULT -1," +
                        "song_name TEXT NOT NULL DEFAULT ''," +
                        "artist_name TEXT NOT NULL DEFAULT ''," +
                        "type TEXT NOT NULL DEFAULT ''," +
                        "part TEXT NOT NULL DEFAULT ''," +
                        "version INTEGER NOT NULL DEFAULT 0," +
                        "votes INTEGER NOT NULL DEFAULT 0," +
                        "rating REAL NOT NULL DEFAULT 0.0," +
                        "date INTEGER NOT NULL DEFAULT 0," +
                        "status TEXT NOT NULL DEFAULT ''," +
                        "preset_id INTEGER NOT NULL DEFAULT 0," +
                        "tab_access_type TEXT NOT NULL DEFAULT 'public'," +
                        "tp_version INTEGER NOT NULL DEFAULT 0," +
                        "tonality_name TEXT NOT NULL DEFAULT ''," +
                        "version_description TEXT NOT NULL DEFAULT ''," +
                        "verified INTEGER NOT NULL DEFAULT 0," +
                        "recording_is_acoustic INTEGER NOT NULL DEFAULT 0," +
                        "recording_tonality_name TEXT NOT NULL DEFAULT ''," +
                        "recording_performance TEXT NOT NULL DEFAULT ''," +
                        "recording_artists TEXT NOT NULL DEFAULT ''," +
                        "num_versions INTEGER NOT NULL DEFAULT 1," +
                        "recommended TEXT NOT NULL DEFAULT ''," +
                        "user_rating INTEGER NOT NULL DEFAULT 0," +
                        "difficulty TEXT NOT NULL DEFAULT 'novice'," +
                        "tuning TEXT NOT NULL DEFAULT 'E A D G B E'," +
                        "capo INTEGER NOT NULL DEFAULT 0," +
                        "url_web TEXT NOT NULL DEFAULT ''," +
                        "strumming TEXT NOT NULL DEFAULT ''," +
                        "videos_count INTEGER NOT NULL DEFAULT 0," +
                        "pro_brother INTEGER NOT NULL DEFAULT 0," +
                        "contributor_user_id INTEGER NOT NULL DEFAULT -1," +
                        "contributor_user_name TEXT NOT NULL DEFAULT ''," +
                        "content TEXT NOT NULL DEFAULT ''" +
                        ")"
                )

                // Copy the data from the old table to the new table
                db.execSQL("INSERT INTO tabs_new SELECT " +
                        "id, song_id, song_name, artist_name, type, part, version, votes, rating, date, status, " +
                        "preset_id, tab_access_type, tp_version, tonality_name, version_description, verified, " +
                        "recording_is_acoustic, recording_tonality_name, recording_performance, recording_artists, " +
                        "num_versions, recommended, user_rating, difficulty, tuning, capo, url_web, strumming, " +
                        "videos_count, pro_brother, contributor_user_id, contributor_user_name, content " +
                        "FROM tabs"
                )

                // Drop the old table
                db.execSQL("DROP TABLE tabs")

                // Rename the new table to the original table name
                db.execSQL("ALTER TABLE tabs_new RENAME TO tabs")
            }
        }
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            // add empty user preferences table
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE preferences (name TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")
            }
        }
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            // add empty user preferences table
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE search_suggestions (query TEXT PRIMARY KEY NOT NULL, suggested_searches TEXT NOT NULL)")
            }
        }
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chord_variation ADD COLUMN instrument TEXT NOT NULL DEFAULT 'Guitar'")
            }
        }
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tabs ADD COLUMN artist_id INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE tabs SET content = ''")
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
                        MIGRATION_14_15)
                    .build()
        }
    }
}