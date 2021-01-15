package com.gbros.tabslite

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.utilities.PlaylistHelper

class AddToPlaylistDialogFragment(private val tabId: Int, currentPlaylists: List<Playlist>, private val transpose: Int = 0) : DialogFragment() {
    private val playlists = currentPlaylists.sortedBy { it.title }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlistNames  = mutableListOf<CharSequence>()
        for (playlist in playlists) {
            playlistNames.add(playlist.title)
        }
        playlistNames.add("New Playlist...")
        val arrPlaylistNames = playlistNames.toTypedArray()

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Add to playlist")
                    .setItems(arrPlaylistNames) { _, which ->  // The 'which' argument contains the index position of the selected item

                        if (which < arrPlaylistNames.size -1) { // the last entry is our New Playlist option, so if they click that option, create a new playlist instead
                            val pId = playlists[which].playlistId
                            PlaylistHelper.addToPlaylist(pId, tabId, transpose, requireContext())
                        } else {
                            // create a new playlist
                            NewPlaylistDialogFragment(tabId, transpose).show(parentFragmentManager, "newPlaylistTag")
                        }
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}