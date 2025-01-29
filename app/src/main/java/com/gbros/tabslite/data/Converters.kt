package com.gbros.tabslite.data

import androidx.room.TypeConverter
import com.chrynan.chords.model.ChordMarker
import com.google.gson.Gson
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {
    @TypeConverter fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter fun datestampToCalendar(value: Long): Calendar =
            Calendar.getInstance().apply { timeInMillis = value }

    @TypeConverter
    fun arrayListToJson(value: ArrayList<String>?): String = gson.toJson(value)

    @TypeConverter
    fun jsonToArrayList(value: String) = ArrayList(gson.fromJson(value, Array<String>::class.java).toList())
    
    // thanks https://stackoverflow.com/a/44634283/3437608
    @TypeConverter
    fun fromNoteMarkerSet(markers: ArrayList<ChordMarker.Note>): String = gson.toJson(markers)

    @TypeConverter
    fun fromOpenMarkerSet(markers: ArrayList<ChordMarker.Open>): String = gson.toJson(markers)

    @TypeConverter
    fun fromMutedMarkerSet(markers: ArrayList<ChordMarker.Muted>): String = gson.toJson(markers)

    @TypeConverter
    fun fromBarMarkerSet(markers: ArrayList<ChordMarker.Bar>): String = gson.toJson(markers)

    @TypeConverter
    fun toNoteMarkerList(value: String) = ArrayList(gson.fromJson(value, Array<ChordMarker.Note>::class.java).toList())

    @TypeConverter
    fun toOpenMarkerList(value: String) = ArrayList(gson.fromJson(value, Array<ChordMarker.Open>::class.java).toList())

    @TypeConverter
    fun toMutedMarkerList(value: String) = ArrayList(gson.fromJson(value, Array<ChordMarker.Muted>::class.java).toList())

    @TypeConverter
    fun toBarMarkerList(value: String) = ArrayList(gson.fromJson(value, Array<ChordMarker.Bar>::class.java).toList())

    @TypeConverter
    fun fromList(value : List<String>?) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<List<String>>(value)

    companion object {
        private val gson = Gson()
    }
}