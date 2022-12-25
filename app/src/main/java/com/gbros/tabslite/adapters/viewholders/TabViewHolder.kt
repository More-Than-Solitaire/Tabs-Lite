package com.gbros.tabslite.adapters.viewholders

import android.util.Log
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.HomeViewPagerFragmentDirections
import com.gbros.tabslite.data.TabFullWithPlaylistEntry
import com.gbros.tabslite.databinding.ListItemBrowseTabsBinding
import com.gbros.tabslite.utilities.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.utilities.TOP_TABS_PLAYLIST_ID

private const val LOG_NAME = "tabslite.TabViewHolder "

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
