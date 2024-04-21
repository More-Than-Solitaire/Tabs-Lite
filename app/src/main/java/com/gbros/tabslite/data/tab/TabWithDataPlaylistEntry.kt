package com.gbros.tabslite.data.tab

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize  // used for playlist reordering
data class TabWithDataPlaylistEntry(
    @ColumnInfo(name = "entry_id") override val entryId: Int,
    @ColumnInfo(name = "playlist_id") override val playlistId: Int,
    @ColumnInfo(name = "id") override val tabId: Int,
    @ColumnInfo(name = "next_entry_id") override val nextEntryId: Int?,
    @ColumnInfo(name = "prev_entry_id") override val prevEntryId: Int?,
    @ColumnInfo(name = "date_added") override val dateAdded: Long,
    @ColumnInfo(name = "song_id") override val songId: Int,
    @ColumnInfo(name = "song_name") override val songName: String,
    @ColumnInfo(name = "artist_name") override val artistName: String,
    @ColumnInfo(name = "verified") override val isVerified: Boolean,
    @ColumnInfo(name = "num_versions") override val numVersions: Int,
    @ColumnInfo(name = "type") override val type: String,
    @ColumnInfo(name = "part") override val part: String,
    @ColumnInfo(name = "version") override val version: Int,
    @ColumnInfo(name = "votes") override val votes: Int,
    @ColumnInfo(name = "rating") override val rating: Double,
    @ColumnInfo(name = "date") override val date: Int,
    @ColumnInfo(name = "status") override val status: String,
    @ColumnInfo(name = "preset_id") override val presetId: Int,
    @ColumnInfo(name = "tab_access_type") override val tabAccessType: String,
    @ColumnInfo(name = "tp_version") override val tpVersion: Int,
    @ColumnInfo(name = "tonality_name") override var tonalityName: String,
    @ColumnInfo(name = "version_description") override val versionDescription: String,
    @ColumnInfo(name = "recording_is_acoustic") override val recordingIsAcoustic: Boolean,
    @ColumnInfo(name = "recording_tonality_name") override val recordingTonalityName: String,
    @ColumnInfo(name = "recording_performance") override val recordingPerformance: String,
    @ColumnInfo(name = "recording_artists") override val recordingArtists: ArrayList<String>,

    @ColumnInfo(name = "recommended") override var recommended: ArrayList<String> = ArrayList(0),
    @ColumnInfo(name = "user_rating") override var userRating: Int = 0,
    @ColumnInfo(name = "difficulty") override var difficulty: String = "novice",
    @ColumnInfo(name = "tuning") override var tuning: String = "E A D G B E",
    @ColumnInfo(name = "capo") override var capo: Int = 0,
    @ColumnInfo(name = "url_web") override var urlWeb: String = "",
    @ColumnInfo(name = "strumming") override var strumming: ArrayList<String> = ArrayList(),
    @ColumnInfo(name = "videos_count") override var videosCount: Int = 0,
    @ColumnInfo(name = "pro_brother") override var proBrother: Int = 0,
    @ColumnInfo(name = "contributor_user_id") override var contributorUserId: Int = -1,
    @ColumnInfo(name = "contributor_user_name") override var contributorUserName: String = "",
    @ColumnInfo(name = "content") override var content: String = "",

    // columns from Playlist
    @ColumnInfo(name = "user_created") val playlistUserCreated: Boolean?,
    @ColumnInfo(name = "title") val playlistTitle: String?,
    @ColumnInfo(name = "date_created") val playlistDateCreated: Long?,
    @ColumnInfo(name = "date_modified") val playlistDateModified: Long?,
    @ColumnInfo(name = "description") val playlistDescription: String?
) : ITab, IDataPlaylistEntry(tabId = tabId, transpose = 0, entryId = entryId, playlistId = playlistId, nextEntryId = nextEntryId, prevEntryId = prevEntryId, dateAdded = dateAdded), Parcelable {
    @IgnoredOnParcel  // we only need parallelization for playlist reordering
    @ColumnInfo(name = "transpose") override var transpose: Int = 0
        private set
    override fun transpose(halfSteps: Int) {
        super<ITab>.transpose(halfSteps)
        transpose += halfSteps
    }
}