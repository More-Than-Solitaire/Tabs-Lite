package com.gbros.tabslite.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gbros.tabslite.AddToPlaylistDialogFragment
import com.gbros.tabslite.R
import com.gbros.tabslite.SongVersionFragment
import com.gbros.tabslite.adapters.MyPlaylistEntryRecyclerViewAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.TabBasic
import com.gbros.tabslite.databinding.FragmentPlaylistBinding
import com.gbros.tabslite.databinding.FragmentPlaylistsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class ViewPlaylistFragment : Fragment() {

    companion object {
        fun newInstance() = ViewPlaylistFragment()
    }

    private lateinit var viewModel: MainViewModel

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
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}