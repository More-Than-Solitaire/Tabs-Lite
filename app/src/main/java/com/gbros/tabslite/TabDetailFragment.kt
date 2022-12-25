package com.gbros.tabslite

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ShareCompat
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.IntPlaylistEntry
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.data.Tab
import com.gbros.tabslite.databinding.FragmentTabDetailBinding
import com.gbros.tabslite.utilities.TabHelper
import com.gbros.tabslite.workers.SearchHelper
import com.gbros.tabslite.workers.UgApi
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


private const val LOG_NAME = "tabslite.TabDetailFragm"

/**
 * A fragment representing a single Tab detail screen.  This is the main purpose of the app, containing
 * tab specific info, and a TabView for displaying the actual tab.
 */
class TabDetailFragment : Fragment() {
    private val timerHandler = Handler(Looper.getMainLooper())
    private var isScrolling: Boolean = false
    private var scrollDelayMs: Long = 34  // default scroll speed (smaller is faster)
    private var currentChordDialog: ChordBottomSheetDialogFragment? = null
    private var playlistEntry: IntPlaylistEntry? = null

    private lateinit var binding: FragmentTabDetailBinding
    private lateinit var optionsMenu: Menu

    //region Fragment overrides

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(LOG_NAME, "Starting TabDetailFragment")

        /* ************************************     BASIC SETUP     ************************************ */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_detail, container, false)

        // hide notification bar in landscape mode
        val notificationBarShouldBeVisible = resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        setNotificationBarVisibility(notificationBarShouldBeVisible)
        binding.coordinatorLayout.fitsSystemWindows = notificationBarShouldBeVisible
        binding.appbar.fitsSystemWindows = notificationBarShouldBeVisible
        binding.toolbarLayout.fitsSystemWindows = notificationBarShouldBeVisible

        binding.lifecycleOwner = viewLifecycleOwner
        setPlaylistButtonsEnabled(false, false)

        // title bar
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }

        // autoscroll speed seek bar
        setScrollListener(binding) // scroll
        binding.autoscrollSpeed.clipToOutline = true  // not really needed since the background is enough bigger
        binding.autoscrollSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.autoscrollSpeed.isGone = true  // start hidden
        setScrollCallback(binding)  // does this break when we leave it scrolling before pausing?

        // chord views
        binding.tabContent.setCallback(this::chordClicked)

        /* ************************************     Load Tab/UI     ************************************ */
        arguments?.let { argBundle ->
            val tabId = argBundle.getInt("tabId")  // should we replace this with a TabBasic since that's normally what the sender already has?

            val isPlaylist = argBundle.getBoolean("isPlaylist", false)
            playlistEntry = argBundle.getParcelable("playlistEntry") //todo: test what happens when this is null  (when this is removed from kotlin, see https://stackoverflow.com/questions/73019160/android-getparcelableextra-deprecated)
            loadTab(tabId, binding, playlistEntry)  // load current tab


            // playlist related buttons
            val playlistName = argBundle.getString("playlistName", "")
            binding.isPlaylist = isPlaylist
            binding.playlistNameStr = playlistName
            binding.nextTabButtonText = "Next"

            // transpose
            binding.transposed = "0"
            binding.transposeUp.setOnClickListener { transpose(true, playlistEntry) }
            binding.transposeDown.setOnClickListener { transpose(false, playlistEntry) }
            binding.cancelTranspose.setOnClickListener { transpose(-(binding.transposed!!.toInt()), playlistEntry) }
        }

        // set screen to always on while the tab is open
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
                menuInflater.inflate(R.menu.menu_tab_detail, menu)
                optionsMenu = menu

                binding.tab?.let {
                    setHeartIconState(it.tabId)  // make sure the favorites heart doesn't get reset when app is paused
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        if(!(activity?.application as DefaultApplication).runningOnFirebaseTest()){  // disable share menu for test lab
                            createShareIntent()
                        }
                        true
                    }
                    R.id.action_favorite -> {
                        Log.d(LOG_NAME, "setting favorite status")
                        if(binding.tab != null) {
                            val isNowFavorite = !menuItem.isChecked

                            // update menu icon
                            menuItem.isChecked = isNowFavorite  // update .isChecked state
                            if (isNowFavorite) {
                                menuItem.setIcon(R.drawable.ic_favorite)
                            } else {
                                menuItem.setIcon(R.drawable.ic_unfavorite)
                            }

                            // update database
                            GlobalScope.launch {
                                val playlistEntryDb = AppDatabase.getInstance(requireContext()).playlistEntryDao()
                                if (isNowFavorite) {
                                    playlistEntryDb.insertToFavorites(
                                        binding.tab!!.tabId,
                                        binding.transposed!!.toInt()
                                    )
                                } else {
                                    playlistEntryDb.deleteTabFromFavorites(binding.tab!!.tabId)
                                }
                            }
                        }
                        true
                    }
                    R.id.action_reload -> {  // reload button clicked (refresh page)
                        if(binding.tab != null) {
                            binding.progressBar2.isGone = false
                            loadTab(binding.tab!!.tabId, binding, playlistEntry, true)
                            true
                        } else {
                            false
                        }
                    }
                    R.id.dark_mode_toggle -> {
                        // show dialog asking user which mode they want
                        context?.let { (activity?.application as DefaultApplication).darkModeDialog(it) }
                        true
                    }
                    R.id.action_add_to_playlist -> {
                        // show add to playlist dialog
                        if (binding.tab != null) {
                            Log.v(LOG_NAME, "Adding tab to playlist")
                            val getPlaylistsFromDbJob = GlobalScope.async {
                                AppDatabase.getInstance(requireContext()).playlistDao()
                                    .getCurrentPlaylists()
                            }
                            getPlaylistsFromDbJob.invokeOnCompletion {
                                val playlists = getPlaylistsFromDbJob.getCompleted()
                                AddToPlaylistDialogFragment(binding.tab!!.tabId, playlists, binding.transposed!!.toInt()).show(
                                    childFragmentManager,
                                    "AddToPlaylistDialogTag"
                                )
                                Log.v(LOG_NAME, "Add to playlist task handed off to dialog.")
                            }

                            true
                        } else {
                            false
                        }
                    }
                    R.id.search -> {
                        val searchView = menuItem.actionView as SearchView
                        if (!SearchHelper.InitilizationComplete) {
                            SearchHelper.initializeSearchBar(
                                "",
                                searchView,
                                requireContext(),
                                viewLifecycleOwner
                            ) { q ->
                                Log.i(LOG_NAME, "Starting search from TabDetailFragment for '$q'")
                                val direction = TabDetailFragmentDirections.actionTabDetailFragment2ToSearchResultFragment(q)
                                view.findNavController().navigate(direction)
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onPause() {
        // consider pausing the autoscroll?
        currentChordDialog?.dismiss()  // this doesn't parcelize well, so get rid of it before we pause
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    //endregion

    private fun setPlaylistButtonsEnabled(prev: Boolean? = null, next: Boolean? = null) {
        // disable and grey out next and previous buttons until they're set to the correct action
        if (prev != null) {
            if (prev) {
                binding.btnTopSkipPrev.isEnabled = true
                binding.btnTopSkipPrev.imageAlpha = 255
                binding.btnPrev.isEnabled = true
            } else {
                binding.btnTopSkipPrev.isEnabled = false
                binding.btnTopSkipPrev.imageAlpha = 75
                binding.btnPrev.isEnabled = false
            }
        }

        if (next != null) {
            if (next) {
                binding.btnTopSkipNext.isEnabled = true
                binding.btnTopSkipNext.imageAlpha = 255
                binding.btnNext.isEnabled = true
            } else {
                binding.btnTopSkipNext.isEnabled = false
                binding.btnTopSkipNext.imageAlpha = 75
                binding.btnNext.isEnabled = false
            }
        }
    }

    private fun setNotificationBarVisibility(visible: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setNotificationBarVisibilityForNewAndroidVersions(visible)
        } else {
            setNotificationBarVisibilityForOldAndroidVersions(visible)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setNotificationBarVisibilityForNewAndroidVersions(visible: Boolean) {
        val windowInsetsController = activity?.window?.decorView?.windowInsetsController

        if (visible) {
            windowInsetsController?.show(
                WindowInsets.Type.statusBars()
                        or WindowInsets.Type.navigationBars()
            )
        } else {
            windowInsetsController?.hide(
                WindowInsets.Type.statusBars()
                        or WindowInsets.Type.navigationBars()
            )
        }
    }

    private fun setNotificationBarVisibilityForOldAndroidVersions(visible: Boolean) {
        if (visible) {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        else {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    /**
     * Loads a tab into the current view
     *
     * This function does an asynchronous tab fetch from the database (or the internet if none cached), then calls
     * [onDataStored] with the completed tab information.
     *
     * @param   tabId           the ID of the tab to load into the view
     * @param   binding         the view binding to reset UI elements; used in calling [onDataStored]
     * @param   playlistEntry   the playlist entry corresponding to the tab to be loaded, or null if none exists
     */
    private fun loadTab(tabId: Int, binding: FragmentTabDetailBinding, playlistEntry: IntPlaylistEntry? = null, forceInternetLoad: Boolean = false) {
        Log.i(LOG_NAME, "Loading tab $tabId")
        val getDataJob = GlobalScope.async { TabHelper.fetchTabFromInternet(tabId, AppDatabase.getInstance(requireContext()), forceInternetLoad) }
        getDataJob.invokeOnCompletion(onDataStored(tabId, binding, playlistEntry))
        setNextAndPreviousTabs(playlistEntry, binding)
    }

    /**
     * Get next and previous tabs if they exist for this entry in a playlist.  Automatically enables and disables
     * the Next and Previous buttons as needed.
     *
     * @param playlistEntry     The entry in the playlist for this tab.  If not null, isPlaylist is assumed to be
     *                          true. If null, this function does nothing.
     * @param binding           The FragmentTabDetailBinding for calling [loadTab]
     */
    private fun setNextAndPreviousTabs(playlistEntry: IntPlaylistEntry?, binding: FragmentTabDetailBinding) {
        // get next and previous tabs if they exist  //todo: move to new function
        if (playlistEntry != null ) {
            // update NEXT buttons
            val nextId = playlistEntry.nextEntryId
            if (nextId != null) {
                val getNextJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistEntryDao().getEntryById(nextId) }
                getNextJob.invokeOnCompletion {
                    val nextEntry = getNextJob.getCompleted()
                    if (nextEntry != null) {
                        // we now have the info for when the user clicks the Next buttons.
                        binding.btnNext.setOnClickListener { loadTab(nextEntry.tabId, binding, nextEntry) }
                        binding.btnTopSkipNext.setOnClickListener { loadTab(nextEntry.tabId, binding, nextEntry) }
                        (activity as AppCompatActivity).runOnUiThread { setPlaylistButtonsEnabled(next = true) }
                    } else {
                        Log.e(LOG_NAME, "Warning! nextId != null, but fetching the referenced entry returned null.  This should not happen. nextId: $nextId")
                    }
                }
            } else {
                // no next tab in this playlist
                (activity as AppCompatActivity).runOnUiThread { setPlaylistButtonsEnabled(next = false) }
            }

            // update PREV buttons
            Log.d(LOG_NAME, "setting playlist buttons")
            val prevId = playlistEntry.prevEntryId
            if (prevId != null) {
                val getPrevJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistEntryDao().getEntryById(prevId) }
                getPrevJob.invokeOnCompletion {
                    val prevEntry = getPrevJob.getCompleted()
                    if (prevEntry != null) {
                        // we now have the info for when the user clicks the PREV buttons.
                        (activity as AppCompatActivity).runOnUiThread { setPlaylistButtonsEnabled(prev = true) }
                        binding.btnPrev.setOnClickListener { loadTab(prevEntry.tabId, binding, prevEntry) }
                        binding.btnTopSkipPrev.setOnClickListener { loadTab(prevEntry.tabId, binding, prevEntry) }
                        Log.d(LOG_NAME, "set playlist buttons")
                    } else {
                        Log.w(LOG_NAME, "Warning! prevId != null, but fetching the referenced entry returned null.  This should not happen.  prevId: $prevId")
                    }
                }
            } else {
                // no previous tab in this playlist
                (activity as AppCompatActivity).runOnUiThread { setPlaylistButtonsEnabled(prev = false) }
            }
        }
    }

    /**
     * Set the scroll listener, which shows and hides the toolbar based on scroll position.
     */
    private fun setScrollListener(binding: FragmentTabDetailBinding) {
        // create toolbar scroll change worker
        var isToolbarShown = false
        val scrollChangeListener = NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

            // User scrolled past image to height of toolbar and the title text is
            // underneath the toolbar, so the toolbar should be shown.
            val shouldShowToolbar = scrollY > binding.toolbar.height

            // The new state of the toolbar differs from the previous state; update
            // appbar and toolbar attributes.
            if (isToolbarShown != shouldShowToolbar) {
                isToolbarShown = shouldShowToolbar

                // Use shadow animator to add elevation if toolbar is shown
                binding.appbar.isActivated = shouldShowToolbar

                // Show the plant name if toolbar is shown
                // hacking this using the Activity title.  It seems to show whenever title isn't enabled
                // and our normal title won't show so I'm just using reverse psychology here
                binding.toolbarLayout.isTitleEnabled = !shouldShowToolbar
            }
        }
        // scroll change listener begins at Y = 0 when image is fully collapsed
        binding.tabDetailScrollview.setOnScrollChangeListener(scrollChangeListener)
    }

    /**
     * Sets the floating action button callback function to start and stop autoscroll
     */
    private fun setScrollCallback(binding: FragmentTabDetailBinding) {
        val timerRunnable: Runnable = object : Runnable {
            override fun run() {
                binding.tabDetailScrollview.smoothScrollBy(0, 1) // 5 is how many pixels you want it to scroll vertically by
                timerHandler.postDelayed(this, scrollDelayMs) // 10 is how many milliseconds you want this thread to run
            }
        }

        binding.apply{
            fab.setOnClickListener { _ ->
                if (isScrolling) {
                    // stop scrolling
                    autoscrollSpeed.alpha = 1.0F
                    fab.alpha = 1.0F
                    timerHandler.removeCallbacks(timerRunnable)
                    fab.setImageResource(R.drawable.ic_fab_autoscroll)
                    autoscrollSpeed.isGone = true
                    binding.appbar.setExpanded(true, true)  // thanks https://stackoverflow.com/a/32137264/3437608
                } else {
                    // start scrolling
                    timerHandler.postDelayed(timerRunnable, 0)
                    fab.setImageResource(R.drawable.ic_fab_pause_autoscroll)
                    autoscrollSpeed.isGone = false
                    autoscrollSpeed.alpha = 1.0F
                    binding.appbar.setExpanded(false, true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        autoscrollSpeed.alpha = 0.4F
                        fab.alpha = 0.4F
                    }, 100)
                }
                isScrolling = !isScrolling
            }
        }
    }

    /**
     * Listener for the scroll speed seek bar.  Changes the autoscroll speed based on the seek bar position.
     */
    private var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        /**
         * updated continuously as the user slides the thumb, based on the current position of the seekbar. This function
         * converts the "progress" or seek position to scroll speed.  (Or, rather, milliseconds of delay between each 1px
         * scroll action.)
         */
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            //convert progress to delay between 1px updates
            var myDelay = (100 - progress) / 100.0  // delay on a scale of 0 to 1
            myDelay *= 63                           // delay on a scale of 0 to 63  -- this sets the slowest autoscroll speed
            myDelay += 2                            // delay on a scale of 2 to 65  -- this sets the fastest autoscroll speed
            scrollDelayMs = (myDelay).toLong()
        }

        /**
         * Called when the user starts touching the seekbar, setting the opacity to 100%
         */
        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // thanks stackoverflow.com/a/7689776
            binding.autoscrollSpeed.alpha = 1.0F
            binding.fab.alpha = 1.0F
        }

        /**
         * called when the user stops touching the SeekBar, to set opacity to 40%
         */
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // thanks stackoverflow.com/a/7689776
            binding.autoscrollSpeed.alpha = 0.4F
            binding.fab.alpha = 0.4F
        }
    }

    /**
     * Transposes the tab up or down one half-step.  If playlistEntry isn't null, updates the saved transposition
     * amount for this playlist entry.
     */
    private fun transpose(up: Boolean, playlistEntry: IntPlaylistEntry?){
        val howMuch = if(up) 1 else -1
        transpose(howMuch, playlistEntry)
    }

    /**
     * update local variables, database, and the tab content view with a new transposition level.
     * Updates the chords (content) as well as the TextView showing the current transposition level.
     *
     * @param howMuch       the amount to transpose, relative to the current transposition amount.
     * @param playlistEntry (nullable) the playlist entry to update with the new transposition amount.
     */
    private fun transpose(howMuch: Int, playlistEntry: IntPlaylistEntry?){
        var currentTransposeAmount = binding.transposed!!.toInt()
        currentTransposeAmount += howMuch  // update the view

        //12 half steps in an octave
        if (currentTransposeAmount >= 12) {
            currentTransposeAmount -= 12
        } else if (currentTransposeAmount <= -12) {
            currentTransposeAmount += 12
        }

        Log.v(LOG_NAME, "Updating transpose level to $currentTransposeAmount by transposing $howMuch")
        if (binding.tonalityName != "") {
            binding.tonalityName =
                TabTextView.transposeChord(binding.tonalityName!!, howMuch)
        }
        binding.tabContent.transpose(howMuch)  // the actual transposition
        binding.transposed = currentTransposeAmount.toString()  // update view

        // only contact the database if there's a change in transposition AND the tab's in a playlist (e.g. favorites)
        if (howMuch != 0 && playlistEntry != null) {
            playlistEntry.transpose = currentTransposeAmount
            Log.d(LOG_NAME, "updated playlist entry transposition to ${currentTransposeAmount}: ${playlistEntry.transpose}")
            GlobalScope.launch {
                AppDatabase.getInstance(requireContext()).playlistEntryDao()
                    .update(PlaylistEntry(playlistEntry))
            }
        }
    }

    private fun onDataStored(tabId: Int, binding: FragmentTabDetailBinding, playlistEntry: IntPlaylistEntry?) = { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.w(LOG_NAME, "Error fetching and storing tab data from online source on the async thread.  Internet connection likely not available.")
            requireActivity().runOnUiThread {
                binding.progressBar2.isGone = true
                view?.let { Snackbar.make(it, "This tab is not available offline.", Snackbar.LENGTH_INDEFINITE).show() }
            }
        } else {
            startGetData(tabId, playlistEntry)
        }
    }

    /**
     * starts here coming from the favorite tabs page; assumes data is already in db
     *
     * @param tabId         the ID of the tab to retrieve from the database
     * @param playlistEntry (optional) if part of a playlist, set this option to use the playlist specific transposition settings, etc.
     */
    private fun startGetData(tabId: Int, playlistEntry: IntPlaylistEntry?) {
        try {
            val getTabFromDbJob = GlobalScope.async {
                Log.v(LOG_NAME, "Fetching tab $tabId from the database.")
                val db = AppDatabase.getInstance(requireContext()).tabFullDao()
                Log.d(LOG_NAME, "database acquired")
                db.getTab(tabId)
            }

            getTabFromDbJob.invokeOnCompletion { cause: Throwable? ->
                if (cause != null) {
                    //oh no; something happened and it failed.  whoops.
                    Log.e(LOG_NAME, "Error fetching tab data from database.")
                    Unit
                } else {
                    Log.v(LOG_NAME, "Data Received for tab fetch")
                    val fetchedTab = getTabFromDbJob.getCompleted()  // actually get the data

                    binding.tabContent.doOnLayout {
                        activity?.runOnUiThread { showTab(fetchedTab, playlistEntry) }
                    }
                }
            }
        } catch (ex: IllegalStateException){
            Log.w(LOG_NAME, "TabDetailFragment could not get data.  Likely the window was closed before the process could start, in which case this message can be ignored.", ex)
        }
    }


    /**
     * Display a TabFull.  Updates UI based on the tab, the given playlistEntry if we're reading from a playlist,
     * and the favorite status of this tab.
     */
    private fun showTab(tabToShow: Tab, playlistEntry: IntPlaylistEntry?) {
        (activity as AppCompatActivity).title = tabToShow.toString()  // toolbar title
        binding.tab = tabToShow
        binding.tabContent.setTabContent(tabToShow.content)
        Log.v(LOG_NAME, "Processed tab content for tab (${binding.tab?.tabId}) '${tabToShow.songName}'.  Length: ${tabToShow.content.length}")
        binding.tonalityName = tabToShow.tonalityName
        binding.progressBar2.isGone = true

        val getFavoritesPlaylistEntryJob = GlobalScope.async {
            AppDatabase.getInstance(requireContext()).playlistEntryDao().getFavoritesPlaylistEntry(tabToShow.tabId)
        }
        getFavoritesPlaylistEntryJob.invokeOnCompletion {
            val favoritesPlaylistEntry = getFavoritesPlaylistEntryJob.getCompleted()
            (activity as AppCompatActivity).runOnUiThread { setHeartIconState(favoritesPlaylistEntry != null) }  // set heart icon

            // update transposition
            var tspAmt = 0
            if (playlistEntry != null) {
                // prioritize getting the transposition amount from the non-favorites playlist entry, if applicable
                tspAmt = playlistEntry.transpose
            } else if (favoritesPlaylistEntry != null) {
                // get the transposition amount from the Favorites playlist
                tspAmt = favoritesPlaylistEntry.transpose
            }

            // currently, the tab text hasn't been transposed.  The transpose() function will change the binding.transposed variable
            binding.transposed = "0"
            (activity as AppCompatActivity).runOnUiThread { transpose(tspAmt, playlistEntry) }
        }

        Log.v(LOG_NAME, "Updated Tab UI for tab (${tabToShow.tabId}) '${tabToShow.songName}'")
    }


    private fun setHeartIconState(tabId: Int) {
        val getFavoritesPlaylistEntryJob = GlobalScope.async {
            AppDatabase.getInstance(requireContext()).playlistEntryDao().getFavoritesPlaylistEntry(tabId)
        }
        getFavoritesPlaylistEntryJob.invokeOnCompletion {
            val favoritesPlaylistEntry = getFavoritesPlaylistEntryJob.getCompleted()
            (activity as AppCompatActivity).runOnUiThread { setHeartIconState(favoritesPlaylistEntry != null) }  // set heart icon
        }
    }

    /**
     * Helper UI function to set the heart icon to a specific value (filled or unfilled)
     */
    private fun setHeartIconState(favorite: Boolean) {
        val favoriteMenuItem = optionsMenu.findItem(R.id.action_favorite)
        if (favorite) {
            favoriteMenuItem.setIcon(R.drawable.ic_favorite)
        } else {
            favoriteMenuItem.setIcon(R.drawable.ic_unfavorite)
        }
    }

    /**
     * Helper function for creating a share intent.  Creates a basic share link and text, and tells Android to open a Share dialog.
     */
    private fun createShareIntent() {
        val shareText = binding.tab.let { tab ->
            if (tab == null) {
                ""
            } else {
                getString(R.string.share_text_plant, tab.toString(), binding.tab!!.getUrl())
            }
        }

        val shareIntent = ShareCompat
                .IntentBuilder(requireActivity())
                .setText(shareText)
                .setType("text/plain")
                .createChooserIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }

    /**
     * Show a chord fingering.
     *
     * This method gets the requested chord from the internal database (if cached) or the internet and the displays
     * it in a bottom sheet dialog.
     *
     * @param chordName     The chord to display
     * @param noUpdate      (Optional) If true, the chord will not be loaded from the internet.  This is used as a retry
     *                      recursive parameter to try an internet fetch twice before failing
     */
    private fun chordClicked(chordName: CharSequence, noUpdate: Boolean = false){
        val input = ArrayList<CharSequence>()
        input.add(chordName)

        val getChordsJob = GlobalScope.async { UgApi.getChordVariations(chordName, AppDatabase.getInstance(requireContext())) }
        getChordsJob.invokeOnCompletion { cause ->
            if (cause != null) {
                Log.w(LOG_NAME, "Getting chords from db didn't work.", cause.cause)
                Unit
            } else {
                val chordVars = getChordsJob.getCompleted()
                if (chordVars.isEmpty()) {
                    if(!noUpdate) {
                        // get from the internet
                        val updateJob = GlobalScope.async { UgApi.updateChordVariations(input, AppDatabase.getInstance(requireContext())) }
                        view?.let { Snackbar.make(it, "Loading chord $chordName...", Snackbar.LENGTH_SHORT).show() }
                        updateJob.invokeOnCompletion { cause1 ->
                            if (cause1 != null) {
                                Log.w(LOG_NAME, "Chord update didn't work.", cause1.cause)
                            }
                            // try again
                            chordClicked(chordName, true)
                        }
                    } else {
                        // we already tried and failed from the internet.  Just show an explanation
                        (activity as AppCompatActivity).runOnUiThread {
                            view?.let { Snackbar.make(it, "Chord could not be loaded.  Check your internet connection.", Snackbar.LENGTH_SHORT).show() }
                        }
                    }
                } else {
                    (activity as AppCompatActivity).runOnUiThread {
                        // we made it!  show the chord diagrams
                        currentChordDialog?.dismiss()
                        currentChordDialog = ChordBottomSheetDialogFragment.newInstance(chordVars)
                        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        currentChordDialog?.show((activity as AppCompatActivity).supportFragmentManager, null)
                    }
                }
            }
        }
    }

}
