package com.gbros.tabslite

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipDescription
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistDao
import com.gbros.tabslite.utilities.PlaylistHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.NewPlaylistDia"

class NewPlaylistDialogFragment(private val tabId: Int? = null,
                                private val transpose: Int? = null,
                                private val dialogTitle: String = "Create Playlist",
                                private val presetTitle: String = "",
                                private val presetDescription: String = "",
                                private val playlistId: Int = 0,
                                private val dateCreated: Long = System.currentTimeMillis()
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val getDataJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistDao().getCurrentPlaylists() }
//        getDataJob.invokeOnCompletion {
//            val playlists = getDataJob.getCompleted()
//        }


        return activity?.let { mActivity ->
            //todo: get current playlists
            val content = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
            content.findViewById<EditText>(R.id.etTitle).editableText.append(presetTitle)
            content.findViewById<EditText>(R.id.etDescription).editableText.append(presetDescription)


            val builder = AlertDialog.Builder(mActivity)
            builder.setTitle(dialogTitle)
            builder.setView(content)
            builder.setPositiveButton("Save") { dialog, which ->
                val etTitle = content.findViewById<EditText>(R.id.etTitle).text.toString()
                val etDesc = content.findViewById<EditText>(R.id.etDescription).text.toString()

                if (etTitle.isNotEmpty()) {
                    createPlaylist(etTitle, etDesc, playlistId, dateCreated)  // create a new playlist with the entered info
                } else {
                    Snackbar.make(mActivity.findViewById(android.R.id.content), "Playlist title may not be empty.", Snackbar.LENGTH_SHORT).show()
                    Log.i(LOG_NAME, "User attempted to create a playlist with no title.")
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun createPlaylist(title: String, description: String, playlistId: Int = 0, dateCreated: Long) {
        // setting id to 0 will activate the automatic id generation of the database
        val newPlaylist = Playlist(playlistId, true, title, dateCreated, System.currentTimeMillis(), description)
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