package com.gbros.tabslite.data

import androidx.room.TypeConverter
import com.chrynan.chords.model.ChordMarker
import com.gbros.tabslite.data.chord.ChordVariation
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {
    @TypeConverter fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter fun datestampToCalendar(value: Long): Calendar =
            Calendar.getInstance().apply { timeInMillis = value }

    @TypeConverter
    fun listToJson(value: ArrayList<String>?) = gson.toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = ArrayList<String>(gson.fromJson(value, Array<String>::class.java).toList())

    @TypeConverter
    fun intListToJson(value: ArrayList<Int>?) = gson.toJson(value)

    @TypeConverter
    fun jsonToIntList(value: String) = ArrayList<Int>(gson.fromJson(value, Array<Int>::class.java).toList())

    @TypeConverter
    fun capoToJson(value: List<ChordVariation.CapoInfo>) = gson.toJson(value)

    @TypeConverter
    fun jsontoCapo(value: String) = ArrayList<ChordVariation.CapoInfo>(gson.fromJson(value, Array<ChordVariation.CapoInfo>::class.java).toList())

    // thanks https://stackoverflow.com/a/44634283/3437608
    @TypeConverter
    fun fromNoteMarkerSet(markers: ArrayList<ChordMarker.Note>) = gson.toJson(markers)

    @TypeConverter
    fun fromOpenMarkerSet(markers: ArrayList<ChordMarker.Open>) = gson.toJson(markers)

    @TypeConverter
    fun fromMutedMarkerSet(markers: ArrayList<ChordMarker.Muted>) = gson.toJson(markers)

    @TypeConverter
    fun fromBarMarkerSet(markers: ArrayList<ChordMarker.Bar>) = gson.toJson(markers)

    @TypeConverter
    fun toNoteMarkerList(value: String) = ArrayList<ChordMarker.Note>(gson.fromJson(value, Array<ChordMarker.Note>::class.java).toList())

    @TypeConverter
    fun toOpenMarkerList(value: String) = ArrayList<ChordMarker.Open>(gson.fromJson(value, Array<ChordMarker.Open>::class.java).toList())

    @TypeConverter
    fun toMutedMarkerList(value: String) = ArrayList<ChordMarker.Muted>(gson.fromJson(value, Array<ChordMarker.Muted>::class.java).toList())

    @TypeConverter
    fun toBarMarkerList(value: String) = ArrayList<ChordMarker.Bar>(gson.fromJson(value, Array<ChordMarker.Bar>::class.java).toList())

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    companion object {
        private val gson = Gson()
    }
}