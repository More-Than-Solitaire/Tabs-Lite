package com.gbros.tabslite.adapters.viewholders

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.HomeViewPagerFragmentDirections
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.databinding.ListItemPlaylistBinding

class PlaylistViewHolder(private val binding: ListItemPlaylistBinding): RecyclerView.ViewHolder(binding.root) {
    init {
        binding.setClickListener {
            binding.list?.let { playlist ->
                playlistClicked(playlist, it)
            }
        }
    }

    /**
     * On Playlist click, use navigation to go to the playlist fragment to display a playlist
     */
    private fun playlistClicked(playlist: Playlist, view: View) {
        val direction = HomeViewPagerFragmentDirections.actionViewPagerFragmentToPlaylistFragment(playlist)
        view.findNavController().navigate(direction)
    }

    fun bind(playlist: Playlist) {
        binding.apply {
            list = playlist
            executePendingBindings()
        }
    }
}
