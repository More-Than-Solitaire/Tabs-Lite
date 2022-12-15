package com.gbros.tabslite.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.SearchResultFragment.Callback
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.databinding.ListItemSearchResultBinding

/**
 * [RecyclerView.Adapter] that can display a [IntTabBasic] and makes a call to the
 * specified [Callback].
 */
class MySearchResultRecyclerViewAdapter(val callback: Callback) : ListAdapter<IntTabFull, RecyclerView.ViewHolder>(SongDiffCallback()) {
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

        fun bind(item: IntTabFull) {
            binding.apply {
                song = item
                executePendingBindings()
            }
        }
    }
}

private class SongDiffCallback : DiffUtil.ItemCallback<IntTabFull>() {

    override fun areItemsTheSame(oldItem: IntTabFull, newItem: IntTabFull): Boolean {
        return oldItem.songId == newItem.songId
    }

    override fun areContentsTheSame(oldItem: IntTabFull, newItem: IntTabFull): Boolean {
        return oldItem.songId == newItem.songId
    }
}
