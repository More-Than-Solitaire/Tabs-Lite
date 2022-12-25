package com.gbros.tabslite.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.SearchResultFragment.Callback
import com.gbros.tabslite.adapters.viewholders.PlaylistEntryViewHolder
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.databinding.ListItemPlaylistTabBinding

private const val LOG_NAME = "tabslite.MyPlaylistEntr"

/**
 * [RecyclerView.Adapter] that can display a [PlaylistEntry] and makes a call to the
 * specified [Callback].
 */
class PlaylistEntryRecyclerViewAdapter(private val context: Context, private val playlistName: String,
                                       private val dragCallback: (RecyclerView.ViewHolder) -> Unit)
    : ListAdapter<PlaylistEntry, PlaylistEntryViewHolder>(PlaylistEntryCallback()) {
    private var selectedState = false // if a row is long-pressed, we'll switch to "selected" state and show checkboxes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistEntryViewHolder {
        val view = ListItemPlaylistTabBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return PlaylistEntryViewHolder(view, context, playlistName, dragCallback)
    }

    override fun onBindViewHolder(holder: PlaylistEntryViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnLongClickListener {
            selectedState = true

            false
        }

        holder.bind(item)
    }
}

private class PlaylistEntryCallback : DiffUtil.ItemCallback<PlaylistEntry>() {

    override fun areItemsTheSame(oldItem: PlaylistEntry, newItem: PlaylistEntry): Boolean {
        return oldItem.entryId == newItem.entryId
    }

    override fun areContentsTheSame(oldItem: PlaylistEntry, newItem: PlaylistEntry): Boolean {
        return oldItem.entryId == newItem.entryId
    }
}
