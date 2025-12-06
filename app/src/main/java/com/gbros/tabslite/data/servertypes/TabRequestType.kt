package com.gbros.tabslite.data.servertypes

import android.util.Log
import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.Finger
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringNumber
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.tab.TabDataType

class TabRequestType(var id: Int = -1, var song_id: Int = -1, var song_name: String = "", var artist_id: Int = -1, var artist_name: String = "", var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0, var rating: Double = 0.0, var date: String = "",
                     var status: String = "", var preset_id: Int = 0, var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "", val version_description: String? = null, var verified: Int = 0, val recording: RecordingInfo? = null,
                     var versions: List<VersionInfo> = emptyList(), var user_rating: Int = 0, var difficulty: String = "", var tuning: String = "", var capo: Int = 0, var urlWeb: String = "", var strumming: List<StrummingInfo> = emptyList(), var videosCount: Int = 0,
                     var contributor: ContributorInfo = ContributorInfo(), var pros_brother: String? = null, var recommended: List<VersionInfo> = emptyList(), var applicature: List<ChordInfo> = emptyList(), val content: String? = null) {
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
        var id: Int = 0, var song_id: Int = 0, var song_name: String = "", var artist_name: String = "", var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0, var rating: Double = 0.0, var date: String = "", var status: String = "", var preset_id: Int = 0,
        var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "", var version_description: String = "", var verified: Int = 0, var recording: RecordingInfo = RecordingInfo())

    class ContributorInfo(var user_id: Int = 0, var username: String = "")
    class ChordInfo(var chord: String = "", var variations: List<VarInfo> = emptyList()) {
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

    class StrummingInfo(
        var part: String = "",
        var denuminator: Int = 0,
        var bpm: Int = 0,
        var is_triplet: Int = 0,
        var measures: List<MeasureInfo> = emptyList()
    ) {
        class MeasureInfo(var measure: Int = 0)
    }

    fun getTabFull(): TabDataType {
        val tab = TabDataType(
            tabId = id,
            songId = song_id,
            songName = song_name,
            artistName = artist_name,
            artistId = artist_id,
            type = type,
            part = part,
            version = version,
            votes = votes,
            rating = rating.toDouble(),
            date = date.toIntOrNull() ?: 0,
            status = status,
            presetId = preset_id,
            tabAccessType = tab_access_type,
            tpVersion = tp_version,
            tonalityName = tonality_name,
            isVerified = (verified != 0),
            contributorUserId = contributor.user_id,
            contributorUserName = contributor.username,
            capo = capo
        )

        if (version_description != null) {
            tab.versionDescription = version_description
        } else {
            tab.versionDescription = ""
        }

        if (recording != null) {
            tab.recordingIsAcoustic = (recording.is_acoustic != 0)
            tab.recordingPerformance = recording.performance.toString()
            tab.recordingTonalityName = recording.tonality_name
            tab.recordingArtists = recording.getArtists()
        } else {
            tab.recordingIsAcoustic = false
            tab.recordingPerformance = ""
            tab.recordingTonalityName = ""
            tab.recordingArtists = ArrayList(emptyList<String>())
        }

        if (content != null) {
            tab.content = content
        } else {
            tab.content = "NO TAB CONTENT - Official tab?"
            Log.w(
                javaClass.simpleName,
                "Warning: tab content is empty for id $id.  This is strange.  Could be an official tab."
            )
        }

        return tab
    }
}
