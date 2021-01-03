package com.gbros.tabslite.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.HomeViewPagerFragmentDirections
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabBasic
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.databinding.ListItemBrowseTabsBinding
import com.gbros.tabslite.databinding.ListItemPlaylistBinding
import com.gbros.tabslite.ui.main.ViewPlaylistFragmentDirections

class PlaylistAdapter :
        ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(
                PlaylistDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_playlist, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

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
}

private class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {

    override fun areItemsTheSame(
            oldItem: Playlist,
            newItem: Playlist
    ): Boolean {
        return oldItem.playlistId == newItem.playlistId
    }

    override fun areContentsTheSame(
            oldItem: Playlist,
            newItem: Playlist
    ): Boolean {

        return (oldItem.playlistId == newItem.playlistId
                && oldItem.userCreated == newItem.userCreated
                && oldItem.dateCreated == newItem.dateCreated
                && oldItem.dateModified == newItem.dateModified
                && oldItem.description == newItem.description
                && oldItem.title == newItem.title)
    }
}