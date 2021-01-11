package com.gbros.tabslite

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.gbros.tabslite.adapters.MyPlaylistEntryRecyclerViewAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.databinding.FragmentPlaylistBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class ViewPlaylistFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPlaylistFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        binding.swipeRefresh.isEnabled = false

        arguments?.let { args ->
            val playlist = args.getParcelable<Playlist>("playlist")
            playlist?.let {
                binding.playlist = playlist

                val getDataJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistEntryDao().getPlaylistItems(playlist.playlistId) }
                getDataJob.invokeOnCompletion {
                    val entries = getDataJob.getCompleted()
                    if (entries.isNotEmpty()) {
                        binding.notEmpty = true
                        binding.favoriteTabsList.adapter = MyPlaylistEntryRecyclerViewAdapter(requireContext(), playlist.title)
                        (binding.favoriteTabsList.adapter as MyPlaylistEntryRecyclerViewAdapter).submitList(entries)
                    }
                }
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
        super.onCreateOptionsMenu(menu, inflater)
        //menu.clear()
        inflater.inflate(R.menu.menu_playlist, menu)
    }
}