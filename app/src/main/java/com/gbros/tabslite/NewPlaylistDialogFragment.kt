package com.gbros.tabslite

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipDescription
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistDao
import com.gbros.tabslite.utilities.PlaylistHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class NewPlaylistDialogFragment(private val tabId: Int?, private val transpose: Int?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val getDataJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistDao().getCurrentPlaylists() }
//        getDataJob.invokeOnCompletion {
//            val playlists = getDataJob.getCompleted()
//        }


        return activity?.let {
            //todo: get current playlists
            val content = layoutInflater.inflate(R.layout.dialog_create_playlist, null)

            val builder = AlertDialog.Builder(it)
            builder.setTitle("Create Playlist")
            builder.setView(content)
            builder.setPositiveButton("Save") { dialog, which ->
                val etTitle = content.findViewById<EditText>(R.id.etTitle)
                val etDesc = content.findViewById<EditText>(R.id.etDescription)
                createPlaylist(etTitle.text.toString(), etDesc.text.toString())  // create a new playlist with the entered info
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun createPlaylist(title: String, description: String) {
        // setting id to 0 will activate the automatic id generation of the database
        val newPlaylist = Playlist(0, true, title, System.currentTimeMillis(), System.currentTimeMillis(), description)
        val insertJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistDao().savePlaylist(newPlaylist) }
        insertJob.invokeOnCompletion {
            val newPlaylistId = insertJob.getCompleted()

            // only add a playlist entry if there's one to be added
            if (tabId != null && transpose != null) {
                PlaylistHelper.addToPlaylist(newPlaylistId.toInt(), tabId, transpose, requireContext())
            }
        }
    }
}