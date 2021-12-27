package com.gbros.tabslite

import android.app.Activity
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.gbros.tabslite.adapters.MyTabBasicRecyclerViewAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.TabBasic
import com.gbros.tabslite.utilities.TabHelper
import com.gbros.tabslite.workers.SearchHelper
import com.google.android.gms.instantapps.InstantApps
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

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
            if (!rawVersionsList.isNullOrEmpty()) {  // if there are no versions of this song at all, don't try to cast to Array<TabBasic>
                // possible tab.type's: "Tab" (not 100% sure on this one), "Chords", "Official"
                // filter out "official" tabs -- the ones without nice chords and a "content" field.
                // also filter out tabs vs chords.  // todo: maybe implement tabs
                songVersions =
                    (rawVersionsList as Array<TabBasic>).filter { tab -> tab.type == "Chords" }
                songVersions =
                    songVersions.sortedWith(compareByDescending { it.votes })  // thanks https://www.programiz.com/kotlin-programming/examples/sort-custom-objects-property
            }
        }

        setHasOptionsMenu(true)
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
        (activity as AppCompatActivity).let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.setDisplayShowHomeEnabled(true)
            it.supportActionBar?.setDisplayShowTitleEnabled(true)

            // since we're filtering out tabs and official tabs, we could end up with no song versions.
            val tvNoSupportedResults = view.findViewById<TextView>(R.id.tv_no_supported_results)
            if (songVersions.isNotEmpty()) {
                rView.isGone = false
                tvNoSupportedResults.isGone = true
                it.supportActionBar?.title = songVersions[0].toString()
            } else {
                rView.isGone = true
                tvNoSupportedResults.isGone = false
            }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        requireActivity().menuInflater.inflate(R.menu.menu_main, menu)

        if(com.google.android.gms.common.wrappers.InstantApps.isInstantApp(context)){
            menu.findItem(R.id.get_app).isVisible = true
        }

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        SearchHelper.initializeSearchBar("", searchView, requireContext(), viewLifecycleOwner, {q ->
            Log.i(LOG_NAME, "Starting search from SongVersionFragment for query '$q'")
            val direction = SongVersionFragmentDirections.actionSongVersionFragmentToSearchResultFragment(q)
            view?.findNavController()?.navigate(direction)
        })
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.dark_mode_toggle -> {
                context?.let { (activity?.application as DefaultApplication).darkModeDialog(it) }  // show dialog asking user which mode they want
                true
            }
            R.id.get_app -> {
                val postInstall = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .setPackage("com.gbros.tabslite")
                InstantApps.showInstallPrompt((activity as Activity), postInstall, 0, null)

                true
            }
            else -> {
                false // let someone else take care of this click
            }
        }
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
