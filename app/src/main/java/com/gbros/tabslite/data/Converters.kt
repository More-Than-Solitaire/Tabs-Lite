package com.gbros.tabslite.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.Calendar

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

    companion object {
        private val gson = Gson()
    }
}