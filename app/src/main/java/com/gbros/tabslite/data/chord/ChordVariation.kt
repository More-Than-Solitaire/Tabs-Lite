package com.gbros.tabslite.data.chord

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chrynan.chords.model.Chord
import com.chrynan.chords.model.ChordMarker
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * [ChordVariation] is how to play an instance of this particular chord ([chordId]).
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
    @ColumnInfo(name = "bar_chord_markers") val barChordMarkers: @RawValue ArrayList<ChordMarker.Bar>,
    @ColumnInfo(name = "instrument") val instrument: Instrument
) : Parcelable {

    override fun toString() = varId

    /**
     * Converts this [ChordVariation] to a [com.chrynan.chords.model.Chord]
     */
    fun toChrynanChord(): Chord {
        val markerSet = HashSet<ChordMarker>()
        markerSet.addAll(noteChordMarkers)
        markerSet.addAll(openChordMarkers)
        markerSet.addAll(mutedChordMarkers)
        markerSet.addAll(barChordMarkers)

        return Chord(chordId, markerSet)
    }
}