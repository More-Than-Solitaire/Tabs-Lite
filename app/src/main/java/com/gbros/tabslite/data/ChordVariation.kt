package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.*
import com.chrynan.chords.model.ChordMarker
import kotlinx.parcelize.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.collections.ArrayList

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
        @ColumnInfo(name = "note_chord_markers") val noteChordMarkers: @RawValue ArrayList<ChordMarker.Note>,
        @ColumnInfo(name = "open_chord_markers") val openChordMarkers: @RawValue ArrayList<ChordMarker.Open>,
        @ColumnInfo(name = "muted_chord_markers") val mutedChordMarkers: @RawValue ArrayList<ChordMarker.Muted>,
        @ColumnInfo(name = "bar_chord_markers") val barChordMarkers: @RawValue ArrayList<ChordMarker.Bar>
) : Parcelable {
    @Parcelize
    class CapoInfo(var fret: Int, var startString: Int, var lastString: Int, var finger: Int) : Parcelable

    override fun toString() = varId
}