package com.gbros.tabslite.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.SearchResultFragment.Callback
import com.gbros.tabslite.adapters.viewholders.SearchResultViewHolder
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.databinding.ListItemSearchResultBinding

/**
 * [RecyclerView.Adapter] that can display a [IntTabFull] and makes a call to the
 * specified [Callback].
 */
class TabFullToSearchResultRecyclerViewAdapter(val callback: Callback) : ListAdapter<IntTabFull, SearchResultViewHolder>(SongDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = ListItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.setClickListener(callback)
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
