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
import com.gbros.tabslite.databinding.FragmentPlaylistBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


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
                    (binding.favoriteTabsList.adapter as MyPlaylistEntryRecyclerViewAdapter).submitList(entries)
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