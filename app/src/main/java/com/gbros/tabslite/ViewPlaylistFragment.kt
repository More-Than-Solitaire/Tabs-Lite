package com.gbros.tabslite

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.adapters.MyPlaylistEntryRecyclerViewAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.databinding.FragmentPlaylistBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

private const val LOG_NAME = "tabslite.ViewPlaylistFr"

class ViewPlaylistFragment : Fragment() {
    companion object {
        fun newInstance() = ViewPlaylistFragment()
    }

    var runonceflag = true
    var playlistId = 0
    var playlist: Playlist? = null

    var menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.delete_playlist) {
                context?.let { context ->  // confirm deletion
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setMessage("Delete Playlist?").setPositiveButton("Yes", deletePlaylistConfirmationDialogListener)
                        .setNegativeButton("No", deletePlaylistConfirmationDialogListener).show()
                }

                true
            } else if (menuItem.itemId == R.id.edit_playlist) {
                // todo: make "Edit Playlist" into a string resource
                playlist?.let { pl -> NewPlaylistDialogFragment(null, null, "Edit Playlist", pl.title, pl.description, pl.playlistId).show(parentFragmentManager, "editPlaylistTag") }
                true
            } else {
                false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        // arguments
        arguments?.let { args ->
            val playlist = args.getParcelable<Playlist>("playlist")
            playlist?.let { updateView(binding, it) }  // set up once with the passed variable

            // now get live data from the database.  This enables editing (and instant feedback)
            AppDatabase.getInstance(requireContext()).playlistDao().getPlaylistLive(playlistId).observe(viewLifecycleOwner) { pl ->
                updateView(binding, pl)
            }

            // set up toolbar and back button
            binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            binding.toolbar.setNavigationOnClickListener {
                it.findNavController().navigateUp()
            }

            binding.toolbar.setOnMenuItemClickListener {
                menuProvider.onMenuItemSelected(it)
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateView(binding: FragmentPlaylistBinding, playlist: Playlist) {
        binding.playlist = playlist
        this.playlistId = playlist.playlistId
        this.playlist = playlist
        setupRecyclerViewAdapter(binding, playlist.title)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerViewAdapter(binding: FragmentPlaylistBinding, playlistTitle: String) {
        AppDatabase.getInstance(requireContext()).playlistEntryDao().getLivePlaylistItems(playlistId).observe(viewLifecycleOwner) { entries ->
            binding.notEmpty = entries.isNotEmpty()

            val orderedEntries = sort(entries)

            // update the database only when touch is lifted
            var currentFromPos: Int? = null
            var currentToPos: Int = 0

            val finishMoveCallback = {
                Log.d(LOG_NAME, "Finish move callback")
                val myFrom = currentFromPos
                if (myFrom != null && currentToPos != myFrom) {
                    Log.d(LOG_NAME, "Moving from $myFrom to $currentToPos")
                    Log.d(
                        LOG_NAME,
                        "Original item at dest: ${orderedEntries[currentToPos].entryId}"
                    )

                    val src = orderedEntries[myFrom]
                    val dest = orderedEntries[currentToPos]

                    val destPrev: Int?
                    val destNext: Int?

                    if (currentToPos < myFrom) {
                        destPrev = dest.prevEntryId
                        destNext = dest.entryId
                    } else {
                        destPrev = dest.entryId
                        destNext = dest.nextEntryId
                    }

                    if (runonceflag) {
                        GlobalScope.launch {
                            AppDatabase.getInstance(requireContext()).playlistEntryDao().moveEntry(
                                src.prevEntryId,
                                src.nextEntryId,
                                src.entryId,
                                destPrev,
                                destNext
                            )
                        }
                    }
                }

                // reset vars so touchHelper knows to update the currentFromPosition when it gets called next
                currentFromPos = null
                currentToPos = 0
            }

            // drag to reorder
            val touchHelper =
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(UP + DOWN, 0) {

                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        // track the latest coordinates
                        if (currentFromPos == null) {
                            currentFromPos = viewHolder.absoluteAdapterPosition
                        }
                        currentToPos = target.absoluteAdapterPosition

                        binding.favoriteTabsList.adapter?.notifyItemMoved(
                            viewHolder.absoluteAdapterPosition,
                            target.absoluteAdapterPosition
                        )
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }

                    override fun isLongPressDragEnabled(): Boolean {
                        return false  // true means a long press will enable reorder anywhere in the list item
                    }
                })

            val dragCallback =
                { viewHolder: RecyclerView.ViewHolder -> touchHelper.startDrag(viewHolder) }
            touchHelper.attachToRecyclerView(binding.favoriteTabsList)

            binding.favoriteTabsList.adapter =
                MyPlaylistEntryRecyclerViewAdapter(requireContext(), playlistTitle, dragCallback)
            (binding.favoriteTabsList.adapter as MyPlaylistEntryRecyclerViewAdapter).submitList(
                orderedEntries
            )

            binding.favoriteTabsList.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    finishMoveCallback()
                }

                return@setOnTouchListener false  // so the touch gets passed down to the drag handler in MyPlaylistEntryRecyclerViewAdapter.kt
            }
        }

    }

    private fun sort(playlist: List<PlaylistEntry>): List<PlaylistEntry> {
        if (playlist.isEmpty()) {
            return playlist
        }

        val sortedList = LinkedList(listOf(playlist.first()))

        // find any elements that got moved to the front of the list
        while (sortedList.first().prevEntryId != null) {
            Log.d(LOG_NAME, "Current first entry: ${playlist.first().entryId}")
            val prevId = sortedList.first().prevEntryId

            var foundMatch = false
            for (entry in playlist) {
                if (entry.entryId == prevId) {
                    sortedList.push(entry)
                    foundMatch = true
                    break
                }
            }

            if (!foundMatch) {
                // if we fell out of that for loop naturally, we have a list without a beginning
                Log.e(LOG_NAME, "Playlist does not have beginning!  Playlist ID ${playlist.first().playlistId}")
                break
            }
        }

        Log.d(LOG_NAME, "Sorted list size: ${sortedList.size}")

        // find any elements going through the list
        while (sortedList.last.nextEntryId != null) {
            Log.d(LOG_NAME, "Current last entry: ${sortedList.last.entryId}. List size: ${sortedList.size}")
            val nextId = sortedList.last.nextEntryId

            var foundMatch = false
            for (entry in playlist) {
                Log.d(LOG_NAME, "entry ${entry.entryId}.  Prev. ${entry.prevEntryId}, next ${entry.nextEntryId}.  Looking for ${entry.entryId} == $nextId")
                if (entry.entryId == nextId) {
                    sortedList.add(entry)
                    Log.d(LOG_NAME, "Entry ${entry.entryId} added to list.  New size: ${sortedList.size}.  New last element: ${sortedList.last}")
                    foundMatch = true
                    break
                }
            }

            if (!foundMatch) {
                // if we fell out of that for loop naturally, we have a list without an end
                Log.e(LOG_NAME, "Playlist does not have end!  Playlist ID ${playlist.first().playlistId}")
                break
            }
        }

        if (sortedList.size != playlist.size) {
            Log.e (LOG_NAME, "Playlist does not connect.  Sorted playlist size: ${sortedList.size}.  Original list size: ${playlist.size}")
        }

        return sortedList
    }

    private var deletePlaylistConfirmationDialogListener =
        DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    // exit playlist view
                    activity?.onBackPressedDispatcher?.onBackPressed()

                    // delete playlist itself
                    GlobalScope.launch { AppDatabase.getInstance(requireContext()).playlistDao().deletePlaylist(playlistId)}

                    // delete entries in playlist
                    GlobalScope.launch {AppDatabase.getInstance(requireContext()).playlistEntryDao().clearPlaylist(playlistId)
                    }

                    Log.i(LOG_NAME, "Playlist $playlistId deleted.")
                }
                DialogInterface.BUTTON_NEGATIVE -> {}
            }
        }
}