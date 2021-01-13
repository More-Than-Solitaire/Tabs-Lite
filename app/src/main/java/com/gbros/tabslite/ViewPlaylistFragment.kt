package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gbros.tabslite.adapters.MyPlaylistEntryRecyclerViewAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.databinding.FragmentPlaylistBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


private const val LOG_NAME = "tabslite.ViewPlaylistFr"

class ViewPlaylistFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPlaylistFragment()
    }

    var playlistId = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        binding.swipeRefresh.isEnabled = false

        arguments?.let { args ->
            val playlist = args.getParcelable<Playlist>("playlist")
            playlist?.let {
                binding.playlist = playlist
                playlistId = playlist.playlistId
                binding.favoriteTabsList.adapter = MyPlaylistEntryRecyclerViewAdapter(requireContext(), playlist.title)
                AppDatabase.getInstance(requireContext()).playlistEntryDao().getLivePlaylistItems(playlistId).observe(viewLifecycleOwner, { entries ->
                    binding.notEmpty = entries.isNotEmpty()
                    (binding.favoriteTabsList.adapter as MyPlaylistEntryRecyclerViewAdapter).submitList(sort(entries))
                })
            }

            // set up toolbar/back button
            // set up toolbar and back button
            setHasOptionsMenu(true)
            (activity as AppCompatActivity).let {
                it.setSupportActionBar(binding.toolbar)
                it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                it.supportActionBar?.setDisplayShowHomeEnabled(true)
                it.supportActionBar?.setDisplayShowTitleEnabled(true)
                it.supportActionBar?.title = playlist?.title
            }
        }

        return binding.root
    }

    private fun sort(playlist: List<PlaylistEntry>): List<PlaylistEntry> {

        val sortedList = LinkedList(listOf(playlist.first()))

        // find any elements that got moved to the front of the list
        while (sortedList.first().prevEntryId != null) {
            val prevId = sortedList.first().prevEntryId
            for (entry in playlist) {
                if (entry.entryId == prevId) {
                    sortedList.push(entry)
                    continue
                }
            }
            // if we fell out of that for loop naturally, we have a list without a beginning
            Log.e(LOG_NAME, "Playlist does not have beginning!  Playlist ID ${playlist.first().playlistId}")
            break
        }

        // find any elements going through the list
        while (sortedList.last.prevEntryId != null) {
            val prevId = sortedList.last.prevEntryId
            for (entry in playlist) {
                if (entry.entryId == prevId) {
                    sortedList.add(entry)
                    continue
                }
            }
            // if we fell out of that for loop naturally, we have a list without a beginning
            Log.e(LOG_NAME, "Playlist does not have end!  Playlist ID ${playlist.first().playlistId}")
            break
        }

        if (sortedList.size != playlist.size) {
            Log.e (LOG_NAME, "Playlist does not connect.  Sorted playlist size: ${sortedList.size}.  Original list size: ${playlist.size}")
        }

        return playlist
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_playlist, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete_playlist) {
            GlobalScope.launch { AppDatabase.getInstance(requireContext()).playlistDao().deletePlaylist(playlistId) }  // delete playlist itself
            GlobalScope.launch { AppDatabase.getInstance(requireContext()).playlistEntryDao().deletePlaylist(playlistId) }  // delete entries in playlist

            Log.i(LOG_NAME, "Playlist $playlistId deleted.")
            activity?.onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}