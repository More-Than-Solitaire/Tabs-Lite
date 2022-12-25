package com.gbros.tabslite.adapters.viewholders

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.R
import com.gbros.tabslite.ViewPlaylistFragmentDirections
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.databinding.ListItemPlaylistTabBinding
import com.gbros.tabslite.workers.UgApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.PlaylistEntryV"

class PlaylistEntryViewHolder (
    val binding: ListItemPlaylistTabBinding,
    private val context: Context,
    private val playlistName: String,
    private val dragCallback: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val bindingClickListener: OnClickListener = OnClickListener {
        binding.tab?.let { tab ->
            binding.entry?.let { playlistEntry ->
                // navigate to the tab detail page on item click
                Log.d(LOG_NAME, "Navigating from Playlist to Tab ${tab.tabId}")
                val direction = ViewPlaylistFragmentDirections.actionPlaylistFragmentToTabDetailFragment2(tabId = tab.tabId, playlistEntry = playlistEntry, isPlaylist =  true, playlistName = playlistName)
                binding.root.findNavController().navigate(direction)
            }
        }
    }

    private val optionsMenuOnClickListener = OnClickListener {
        val popup = PopupMenu(context, it)
        popup.inflate(R.menu.popup_playlist_item)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.remove_from_playlist -> {  // delete this entry from the linked list
                    GlobalScope.launch {
                        val db = AppDatabase.getInstance(context).playlistEntryDao()
                        val prevId = binding.entry!!.prevEntryId
                        val nextId = binding.entry!!.nextEntryId
                        val entryId = binding.entry!!.entryId

                        db.setNextEntryId(prevId, nextId)     // update prev item to point to next
                        db.setPrevEntryId(nextId, prevId)     // update next to point to prev
                        db.deleteEntry(entryId)               // delete this
                    }

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popup.show()
    }

    private val dragTouchHandler = OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            dragCallback(this)
            Log.d(LOG_NAME, "ACTION DOWN")
            return@OnTouchListener true
        } else {
            Log.d(LOG_NAME, "Action ${event.action}")
            return@OnTouchListener true

        }
    }

    fun bind(item: PlaylistEntry) {
        binding.apply {
            entry = item

            // get tab from internet
            val getDataJob = GlobalScope.async { UgApi.fetchTabFromInternet(item.tabId, AppDatabase.getInstance(context)) }
            getDataJob.invokeOnCompletion {
                if (getDataJob.getCompleted()) {
                    // get tab from db
                    val getTabJob = GlobalScope.async { AppDatabase.getInstance(context).tabFullDao().getTab(item.tabId) }
                    getTabJob.invokeOnCompletion {
                        val tab = getTabJob.getCompleted()

                        // bind tab
                        binding.tab = tab
                        binding.version = "ver. ${tab.version}"
                    }
                } else {
                    Log.e(LOG_NAME, "Could not fetch tab for playlist listing.  Check internet connection?")
                }
            }

            binding.clickListener = bindingClickListener
            binding.textViewOptions.setOnClickListener(optionsMenuOnClickListener)
            binding.dragHandle.setOnTouchListener(dragTouchHandler)

            executePendingBindings()
        }
    }
}