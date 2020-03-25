package com.gbros.tabslite.data

import android.util.Log
import com.chrynan.chords.model.*

class TabRequestType(var id: Int, var song_id: Int, var song_name: String, var artist_name: String,
                     var type: String, var part: String, var version: Int, var votes: Int,
                     var rating: Double, var date: String, var status: String, var preset_id: Int,
                     var tab_access_type: String, var tp_version: Int, var tonality_name: String,
                     val version_description: String?, var verified: Int, val recording: RecordingInfo?,
                     var versions: List<VersionInfo>, var recommended: List<VersionInfo>,
                     var user_rating: Int, var difficulty: String, var tuning: String, var capo: Int,
                     var urlWeb: String, var strumming: List<StrummingInfo>, var videosCount: Int,
                     var pros_brother: String?, var contributor: ContributorInfo,
                     var applicature: List<ChordInfo>, val content: String?) {
    class RecordingInfo (var is_acoustic: Int, var tonality_name: String, var performance: PerformanceInfo?,
                         var recording_artists: List<RecordingArtistsInfo>){
        class RecordingArtistsInfo(var join_field: String, var artist: ContributorInfo){
            override fun toString(): String {
                return artist.username
            }
        }
        class PerformanceInfo(var name: String, var serie: SerieInfo?, var venue: VenueInfo?,
                              var date_start: Long, var cancelled: Int, var type: String,
                              var comment: String, var video_urls: List<String>) {
            class VenueInfo(name: String, area: AreaInfo){
                class AreaInfo(name: String, country: CountryInfo){
                    class CountryInfo(name_english: String)
                }
            }
            class SerieInfo(name: String, type: String)
            override fun toString(): String {
                return "$name; $comment"
            }
        }

        fun getArtists(): ArrayList<String> {
            val result = ArrayList<String>()
            for (artist in recording_artists){
                result.add(artist.toString())
            }
            return result
        }
    }
    class VersionInfo(var id: Int, var song_id: Int, var song_name: String, var artist_name: String,
                      var type: String, var part: String, var version: Int, var votes: Int,
                      var rating: Double, var date: String, var status: String, var preset_id: Int,
                      var tab_access_type: String, var tp_version: Int, var tonality_name: String,
                      var version_description: String, var verified: Int, var recording: RecordingInfo)
    class ContributorInfo(var user_id: Int, var username: String)
    class ChordInfo(var chord: String, var variations: List<VarInfo>){
        class VarInfo(var id: String, var listCapos: List<ChordVariation.CapoInfo>, var noteIndex: Int,
                      var notes: List<Int>, var frets: List<Int>, var fingers: List<Int>,
                      var fret: Int) {
            private fun Int.toFinger(): Finger {
                return when(this){
                    1 -> Finger.INDEX
                    2 -> Finger.MIDDLE
                    3 -> Finger.RING
                    4 -> Finger.PINKY
                    5 -> Finger.THUMB
                    else -> Finger.UNKNOWN
                }
            }
            fun toChordVariation(chordName: String) : ChordVariation {
                val noteMarkerSet = ArrayList<ChordMarker.Note>()
                val openMarkerSet = ArrayList<ChordMarker.Open>()
                val mutedMarkerSet = ArrayList<ChordMarker.Muted>()
                val barMarkerSet = ArrayList<ChordMarker.Bar>()

                for ((string, fretNumber) in frets.withIndex()) {
                    if (fingers[string].toFinger() != Finger.UNKNOWN) {
                        when {
                            fretNumber > 0 -> {
                                noteMarkerSet.add(ChordMarker.Note(fret = FretNumber(fretNumber), string = StringNumber(string + 1), finger = fingers[string].toFinger()))
                            }
                            fretNumber == 0 -> {
                                openMarkerSet.add(ChordMarker.Open(StringNumber(string + 1)))
                            }  // open string
                            else -> {
                                mutedMarkerSet.add(ChordMarker.Muted(StringNumber(string + 1)))
                            }            // muted string
                        }
                    }
                }

                for (bar in listCapos) {
                    val myMarker = ChordMarker.Bar(fret = FretNumber(bar.fret), startString = StringNumber(bar.startString + 1),
                            endString = StringNumber(bar.lastString), finger = bar.finger.toFinger())
                    barMarkerSet.add(myMarker)
                }

                return ChordVariation(varId = id, chordId = chordName, noteChordMarkers = noteMarkerSet,
                        openChordMarkers = openMarkerSet, mutedChordMarkers = mutedMarkerSet, barChordMarkers = barMarkerSet)
            }
        }

        fun getChordVariations(): List<ChordVariation> {
            val result = ArrayList<ChordVariation>()
            for(variation in variations){
                result.add(variation.toChordVariation(chord))
            }

            return result
        }
    }
    class StrummingInfo(var part: String, var denuminator: Int, var bpm: Int, var is_triplet: Int, var measures: List<MeasureInfo>){
        class MeasureInfo(var measure: Int)
    }

    fun getTabFull(): TabFull {
        val tab = TabFull(tabId = id, songId = song_id, songName = song_name, artistName = artist_name,
                type = type, part = part, version = version, votes = votes, rating = rating.toDouble(),
                date = date.toInt(), status = status, presetId = preset_id, tabAccessType = tab_access_type,
                tpVersion = tp_version, tonalityName = tonality_name,
                isVerified = (verified!=0), contributorUserId = contributor.user_id,
                contributorUserName = contributor.username)

        if(version_description != null) {
            tab.versionDescription = version_description
        } else {
            tab.versionDescription = ""
        }

        if(recording != null) {
            tab.recordingIsAcoustic = (recording.is_acoustic!=0)
            tab.recordingPerformance = recording.performance.toString()
            tab.recordingTonalityName = recording.tonality_name
            tab.recordingArtists = recording.getArtists()
        } else {
            tab.recordingIsAcoustic = false
            tab.recordingPerformance = ""
            tab.recordingTonalityName = ""
            tab.recordingArtists = ArrayList(emptyList<String>())
        }

        if(content != null) {
            tab.content = content
        } else {
            tab.content = "NO TAB CONTENT - Official tab?"
            Log.w(javaClass.simpleName, "Warning: tab content is empty for id $id.  This is strange.  Could be an official tab.")  //todo: implement official tabs (e.g. tabid=2456778)
        }

        return tab
    }

    fun getChordVariations(): List<ChordVariation> {
        val myChords = ArrayList<ChordVariation>()
        for(chord: ChordInfo in applicature) {
            for (variation: ChordInfo.VarInfo in chord.variations) {
                myChords.add(variation.toChordVariation(chord.chord))
            }
        }

        return myChords
    }
}