package com.gbros.tabslite.adapters

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil


import com.gbros.tabslite.SearchResultFragment.Callback
import com.gbros.tabslite.data.IntSong
import com.gbros.tabslite.databinding.ListItemSearchResultBinding

/**
 * [RecyclerView.Adapter] that can display a [IntSong] and makes a call to the
 * specified [Callback].
 */
class MySearchResultRecyclerViewAdapter(val callback: Callback) : ListAdapter<IntSong, RecyclerView.ViewHolder>(SongDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ViewHolder).bind(item)
        holder.setClickListener(callback)
    }

    class ViewHolder(
            private val binding: ListItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setClickListener(callback: Callback){
            binding.setClickListener {
                binding.song?.let { song ->
                    callback.viewSongVersions(song.songId)
                }
            }
        }

        fun bind(item: IntSong) {
            binding.apply {
                song = item
                executePendingBindings()
            }
        }
    }
}

private class SongDiffCallback : DiffUtil.ItemCallback<IntSong>() {

    override fun areItemsTheSame(oldItem: IntSong, newItem: IntSong): Boolean {
        return oldItem.songId == newItem.songId
    }

    override fun areContentsTheSame(oldItem: IntSong, newItem: IntSong): Boolean {
        return oldItem.songId == newItem.songId
    }
}
