package com.gbros.tabslite.data.servertypes

import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.Finger
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringNumber
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.tab.TabDataType
import com.gbros.tabslite.data.tab.TabTuning
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
class TabRequestType(
    var id: String = "",
    var song_id: String = "",
    var song_name: String = "",
    var artist_id: String = "",
    var artist_name: String = "",
    var type: String = "Chords",
    var part: String = "",
    var version: Int = 0,
    var votes: Int = 0,
    var rating: Double = 0.0,
    var date: Long = 0,
    var status: String = "pending",
    var tab_access_type: String = "public",
    var tonality_name: String = "",
    val version_description: String? = "",
    var verified: Int = 0,
    var unique_chords: String = "",
    var difficulty: String = "",
    var tuning: String = TabTuning.Standard.toString(),
    var capo: Int = 0,

    // firestore uses Java Bean convention for converting between class and data types; this prevents firestore from removing the *is* prefix
    // @set sets this property when read from firestore, @get reads this property when writing to firestore
    // https://stackoverflow.com/a/63980376/3437608, https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/PropertyName
    @get:PropertyName("is_tab_ml") @set:PropertyName("is_tab_ml") var is_tab_ml: Boolean = false,

    var song_genre: String = "",
    var ug_difficulty: String = "",
    var versions_count: Int = 0,
    var contributor: ContributorInfo = ContributorInfo(),
    val content: String = ""
) {
    class RecordingInfo(var is_acoustic: Int = 0, var tonality_name: String = "", var performance: PerformanceInfo? = null, var recording_artists: List<RecordingArtistsInfo> = emptyList()) {
        class RecordingArtistsInfo(var join_field: String = "", var artist: ContributorInfo = ContributorInfo()) {
            override fun toString(): String {
                return artist.username
            }
        }

        class PerformanceInfo(var name: String = "", var serie: SerieInfo? = null, var venue: VenueInfo? = null, var date_start: Long = 0, var cancelled: Int = 0, var type: String = "", var comment: String = "", var video_urls: List<String> = emptyList()) {
            class VenueInfo(var name: String = "", var area: AreaInfo = AreaInfo()) {
                class AreaInfo(var name: String = "", var country: CountryInfo = CountryInfo()) {
                    class CountryInfo(var name_english: String = "")
                }
            }

            class SerieInfo(var name: String = "", var type: String = "")

            override fun toString(): String {
                return "$name; $comment"
            }
        }

        fun getArtists(): ArrayList<String> {
            val result = ArrayList<String>()
            for (artist in recording_artists) {
                result.add(artist.toString())
            }
            return result
        }
    }

    class VersionInfo(
        var id: Int = 0, var song_id: Int = 0, var song_name: String = "", var artist_name: String = "", var artist_id: String = "", var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0, var rating: Double = 0.0, var date: String = "", var status: String = "", var preset_id: Int = 0,
        var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "", var version_description: String? = "", var verified: Int = 0, var recording: RecordingInfo = RecordingInfo())

    class ContributorInfo(var user_id: String = "0", var username: String = "Unregistered")
    class ChordInfo(var chord: String = "", var variations: List<VarInfo> = emptyList()) {
        @IgnoreExtraProperties
        class VarInfo(
            var id: String = "", var listCapos: List<CapoInfo> = emptyList(), var noteIndex: Int = 0, var notes: List<Int> = emptyList(), var frets: List<Int> = emptyList(), var fingers: List<Int> = emptyList(), var fret: Int = 0) {
            class CapoInfo(var fret: Int = 0, var startString: Int = 0, var lastString: Int = 0, var finger: Int = 0)

            private fun Int.toFinger(): Finger {
                return when (this) {
                    1 -> Finger.INDEX
                    2 -> Finger.MIDDLE
                    3 -> Finger.RING
                    4 -> Finger.PINKY
                    5 -> Finger.THUMB
                    else -> Finger.UNKNOWN
                }
            }

            fun toChordVariation(chordName: String, instrument: Instrument): ChordVariation {
                val noteMarkerSet = ArrayList<ChordMarker.Note>()
                val openMarkerSet = ArrayList<ChordMarker.Open>()
                val mutedMarkerSet = ArrayList<ChordMarker.Muted>()
                val barMarkerSet = ArrayList<ChordMarker.Bar>()

                for ((string, fretNumber) in frets.withIndex()) {
                    when {
                        fretNumber > 0 -> {
                            val finger = fingers[string]
                            if (finger.toFinger() != Finger.UNKNOWN) {
                                noteMarkerSet.add(
                                    ChordMarker.Note(
                                        fret = FretNumber(fretNumber),
                                        string = StringNumber(string + 1),
                                        finger = finger.toFinger()
                                    )
                                )
                            } else {
                                //Log.e(javaClass.simpleName, "Chord variation with fret number > 0 (fret= $fretNumber), but no finger (finger= $finger).  This shouldn't happen. String= $string, chordName= $chordName")
                                // this is all the barred notes.  We can ignore it since we take care of bars below.
                            }
                        }

                        fretNumber == 0 -> {
                            openMarkerSet.add(ChordMarker.Open(StringNumber(string + 1)))
                        }  // open string
                        else -> {
                            mutedMarkerSet.add(ChordMarker.Muted(StringNumber(string + 1)))
                        }            // muted string
                    }
                }

                for (bar in listCapos) {
                    val myMarker = ChordMarker.Bar(
                        fret = FretNumber(bar.fret),
                        startString = StringNumber(bar.startString + 1),
                        endString = StringNumber(bar.lastString + 1),
                        finger = bar.finger.toFinger()
                    )
                    barMarkerSet.add(myMarker)
                }

                return ChordVariation(
                    varId = id.lowercase(), chordId = chordName,
                    noteChordMarkers = noteMarkerSet, openChordMarkers = openMarkerSet,
                    mutedChordMarkers = mutedMarkerSet, barChordMarkers = barMarkerSet,
                    instrument = instrument
                )
            }
        }

        fun getChordVariations(instrument: Instrument): List<ChordVariation> {
            val result = ArrayList<ChordVariation>()
            for (variation in variations) {
                result.add(variation.toChordVariation(chord, instrument))
            }

            return result
        }
    }

    @Exclude // don't create a property `tabFull` when writing to firestore
    fun getTabFull(): TabDataType {
        val tab = TabDataType(
            tabId = id,
            songId = song_id,
            songName = song_name,
            songGenre = song_genre,
            artistName = artist_name,
            artistId = artist_id,
            type = type,
            part = part,
            version = version,
            votes = votes,
            isVerified = verified == 1,
            rating = rating,
            date = date,
            status = status,
            tabAccessType = tab_access_type,
            tonalityName = tonality_name,
            capo = capo,
            contributorUserId = contributor.user_id,
            contributorUserName = contributor.username,
            versionsCount = versions_count,
            versionDescription = version_description ?: "",
            content = content
        )

        return tab
    }
}
