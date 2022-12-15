package com.gbros.tabslite.adapters

import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.HomeViewPagerFragmentDirections
import com.gbros.tabslite.R
import com.gbros.tabslite.data.TabFullWithPlaylistEntry
import com.gbros.tabslite.databinding.ListItemBrowseTabsBinding
import com.gbros.tabslite.utilities.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.utilities.FAVORITE_TABS_SORTING_PREF_NAME
import com.gbros.tabslite.utilities.TOP_TABS_PLAYLIST_ID

private const val LOG_NAME = "tabslite.BrowseTabsAdap"

class BrowseTabsAdapter :
        ListAdapter<TabFullWithPlaylistEntry, BrowseTabsAdapter.TabViewHolder>(
                TabDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_browse_tabs, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TabViewHolder(private val binding: ListItemBrowseTabsBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.tab?.let { tab ->
                    navigateToTab(tab)
                }
            }
        }

        private fun navigateToTab(tab: TabFullWithPlaylistEntry) {
            // navigate to the tab detail page on item click
            Log.d(LOG_NAME, "Navigating from Favorites to Tab ${tab.tabId}")
            var playlistName = ""
            if (tab.playlistId == FAVORITES_PLAYLIST_ID) {
                playlistName = "Favorites"
            } else if (tab.playlistId == TOP_TABS_PLAYLIST_ID) {
                playlistName = "Top Tabs"
            }
            val direction = HomeViewPagerFragmentDirections.actionViewPagerFragmentToTabDetailFragment2(tabId = tab.tabId, playlistEntry = tab, isPlaylist = false, playlistName = playlistName)
            binding.root.findNavController().navigate(direction)
        }

        fun bind(item: TabFullWithPlaylistEntry) {
            binding.apply {
                tab = item

                whichVersion.text = "ver. ${item.version}"
                executePendingBindings()
            }
        }
    }

    fun updateSorting(position: Int, list: List<TabFullWithPlaylistEntry>) {
        val sortedResult = when(position){
            0 -> list.sortedByDescending { t -> t.dateAdded }  //todo: replace with date of adding to favorites
            1 -> list.sortedBy { t -> t.songName }
            2 -> list.sortedBy { t -> t.artistName }
            3 -> list.sortedByDescending { t -> t.votes }
            else -> list
        }
        submitList(sortedResult)
    }

    fun getItemSelectedListener(sharedPreferences: SharedPreferences?): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            /**
             *
             * Callback method to be invoked when an item in this view has been
             * selected. This callback is invoked only when the newly selected
             * position is different from the previously selected position or if
             * there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the
             * data associated with the selected item.
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateSorting(position, currentList)
                // save our spot for next run
                sharedPreferences?.edit()?.putInt(FAVORITE_TABS_SORTING_PREF_NAME, position)?.apply()
            }
        }
    }

}

private class TabDiffCallback : DiffUtil.ItemCallback<TabFullWithPlaylistEntry>() {

    override fun areItemsTheSame(
            oldItem: TabFullWithPlaylistEntry,
            newItem: TabFullWithPlaylistEntry
    ): Boolean {
        return oldItem.tabId == newItem.tabId && oldItem.entryId == newItem.entryId
    }

    override fun areContentsTheSame(
            oldItem: TabFullWithPlaylistEntry,
            newItem: TabFullWithPlaylistEntry
    ): Boolean {
        return oldItem.tabId == newItem.tabId && oldItem.entryId == newItem.entryId
    }
}