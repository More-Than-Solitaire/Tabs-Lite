package com.gbros.tabslite

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gbros.tabslite.adapters.PlaylistAdapter
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.databinding.FragmentPlaylistsBinding
import com.gbros.tabslite.utilities.InjectorUtils
import com.gbros.tabslite.utilities.PLAYLIST_SORTING_PREF_NAME
import com.gbros.tabslite.utilities.PREFS_NAME
import com.gbros.tabslite.viewmodels.PlaylistsViewModel

private const val LOG_NAME = "tabslite.PlaylistsFragm"

/**
 * The home screen playlists view, showing a list of all playlists that the user has saved
 */
class PlaylistsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistsBinding

    private val viewModel: PlaylistsViewModel by viewModels {
        InjectorUtils.providePlaylistsViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        val adapter = PlaylistAdapter()
        binding.favoriteTabsList.adapter = adapter

        binding.createPlaylist.setOnClickListener {
            // create a new playlist
            NewPlaylistDialogFragment().show(parentFragmentManager, "newPlaylistTag")
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        subscribeUi(binding)
    }

    private fun subscribeUi(binding: FragmentPlaylistsBinding) {
        val adapter = binding.favoriteTabsList.adapter as PlaylistAdapter
        fun updateSorting(position: Int, list: List<Playlist>) {

            val sortedResult = when(position){
                0 -> list.sortedByDescending { t -> t.dateModified }
                1 -> list.sortedBy { t -> t.title }
                2 -> list.sortedBy { t -> t.dateCreated }
                else -> list
            }

            adapter.submitList(sortedResult)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { result ->
            binding.noPlaylists = !result.isNullOrEmpty()

            activity?.application?.apply{
                val storedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PLAYLIST_SORTING_PREF_NAME, 0)
                binding.sortBy.setSelection(storedPref)
                updateSorting(storedPref, result)

                binding.sortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    /**
                     * Callback method to be invoked when the selection disappears from this
                     * view. The selection can disappear for instance when touch is activated
                     * or when the adapter becomes empty.
                     *
                     * @param parent The AdapterView that now contains no selected item.
                     */
                    override fun onNothingSelected(parent: AdapterView<*>?) { }

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
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        updateSorting(position, adapter.currentList)
                        // save our spot for next run
                        activity?.application?.apply{
                            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(PLAYLIST_SORTING_PREF_NAME, position).apply()
                            Log.v(LOG_NAME, "Storing FavoriteTabs SortBy preference ($position)")
                        } ?: Log.w(LOG_NAME, "Could not store FavoriteTabs SortBy preference ($position).")
                    }
                }
            } ?: run {
                // default action if context is somehow null
                adapter.submitList(result.sortedByDescending { t -> t.dateModified })  // needed because 0 is the default selection, so the sort might not be called the first time
                Log.v(LOG_NAME, "Submitted new list of size ${result.size}.  Current adapter list size is now ${adapter.currentList.size}")
            }
        }
    }
}
