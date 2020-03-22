package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * [ChordVariation] represents all the chords the user has come across so far.  The chords here are
 * used for offline access.
 */
@Entity(
        tableName = "chord_variation"
)

@Parcelize
data class ChordVariation(
        @PrimaryKey @ColumnInfo(name = "id") val varId: String,
        @ColumnInfo(name = "chord_id") val chordId: String,
        @ColumnInfo(name = "list_capos") val listCapos: ArrayList<CapoInfo>,
        @ColumnInfo(name = "note_index") val noteIndex: Int,
        @ColumnInfo(name = "notes") val notes: ArrayList<Int>,
        @ColumnInfo(name = "frets") val frets: ArrayList<Int>,
        @ColumnInfo(name = "fingers") val fingers: ArrayList<Int>,
        @ColumnInfo(name = "fret") val fret: Int
) : Parcelable {
    @Parcelize
    class CapoInfo(var fret: Int, var startString: Int, var lastString: Int, var finger: Int) : Parcelable

    override fun toString() = varId
}