package com.gbros.tabslite.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.SearchResultFragment
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.databinding.ListItemSearchResultBinding

class SearchResultViewHolder(
    private val binding: ListItemSearchResultBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun setClickListener(callback: SearchResultFragment.Callback){
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
