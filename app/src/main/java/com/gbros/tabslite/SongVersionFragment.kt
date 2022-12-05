package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.adapters.MyTabBasicRecyclerViewAdapter
import com.gbros.tabslite.data.TabBasic
import com.gbros.tabslite.workers.SearchHelper

private const val LOG_NAME = "tabslite.SongVersionFra"

/**
 * A fragment representing a list of Items.  This fragment is the list of different versions of the same song.
 * When the user searches for a song, then selects one of those songs, this fragment pops up to let them choose
 * which song version they'd like to pick.
 *
 * Activities containing this fragment MUST implement the
 * [SongVersionFragment.OnListFragmentInteractionListener] interface.
 */
class SongVersionFragment : Fragment() {

    private var songVersions : List<TabBasic> = emptyList()
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { it ->
            val rawVersionsList = it.getParcelableArray(ARG_SONG_VERSIONS) as Array<*>
            if (rawVersionsList.isNotEmpty()) {  // if there are no versions of this song at all, don't try to cast to Array<TabBasic>
                try {
                    // possible tab.type's: "Tab" (not 100% sure on this one), "Chords", "Official"
                    // filter out "official" tabs -- the ones without nice chords and a "content" field.
                    // also filter out tabs vs chords.  // todo: maybe implement tabs
                    songVersions =
                        (rawVersionsList as Array<TabBasic>).filter { tab -> tab.type == "Chords" }
                    songVersions =
                        songVersions.sortedWith(compareByDescending { it.votes })  // thanks https://www.programiz.com/kotlin-programming/examples/sort-custom-objects-property
                } catch (_: ClassCastException) {
                    Log.w(LOG_NAME, "Cast failure to Array<TabBasic>.  Unsure how this can happen.  rawVersionsList is $rawVersionsList")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song_version, container, false)
        val rView = view.findViewById<RecyclerView>(R.id.song_version_list)
        // Set the adapter
        if (rView is RecyclerView) {
            with(rView) {
                listener = object: OnListFragmentInteractionListener {
                    override fun onListFragmentInteraction(tabId: Int) {
                        Log.v(LOG_NAME, "Navigating to tab detail fragment (tabId: $tabId)")

                        val direction = SongVersionFragmentDirections.actionSongVersionFragmentToTabDetailFragment2( tabId = tabId, playlistEntry = null, isPlaylist =  false, playlistName =  "")
                        view.findNavController().navigate(direction)
                    }
                }

                layoutManager = LinearLayoutManager(context)
                adapter = MyTabBasicRecyclerViewAdapter(songVersions, listener)
            }
        }

        // set up toolbar and back button
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            it.findNavController().navigateUp()
        }

        // set toolbar title
        val tvNoSupportedResults = view.findViewById<TextView>(R.id.tv_no_supported_results)
        if (songVersions.isNotEmpty()) {
            rView.isGone = false
            tvNoSupportedResults.isGone = true
            toolbar.title = songVersions[0].toString()
        } else {
            rView.isGone = true
            tvNoSupportedResults.isGone = false
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.dark_mode_toggle -> {
                        context?.let { (activity?.application as DefaultApplication).darkModeDialog(it) }  // show dialog asking user which mode they want
                        true
                    }
                    R.id.search -> {
                        val searchView = menuItem.actionView as SearchView
                        SearchHelper.initializeSearchBar("", searchView, requireContext(), viewLifecycleOwner) { q ->
                            Log.i(LOG_NAME, "Starting search from Home for '$q'")
                            val direction = SongVersionFragmentDirections.actionSongVersionFragmentToSearchResultFragment(q)
                            view.findNavController().navigate(direction)
                        }

                        true
                    }
                    else -> {
                        false // let someone else take care of this click
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(tabId: Int)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_SONG_VERSIONS = "songVersions"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(songVersions: Array<TabBasic>) =
                SongVersionFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArray(ARG_SONG_VERSIONS, songVersions)
                    }
                }
    }
}
