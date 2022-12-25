package com.gbros.tabslite.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.gbros.tabslite.R
import com.gbros.tabslite.adapters.viewholders.PlaylistViewHolder
import com.gbros.tabslite.data.Playlist

class PlaylistListAdapter: ListAdapter<Playlist, PlaylistViewHolder>(PlaylistDiffCallback()) {

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