package com.gbros.tabslite

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.databinding.FragmentTabDetailBinding
import com.gbros.tabslite.utilities.TabHelper
import com.gbros.tabslite.workers.UgApi
import com.google.android.gms.common.wrappers.InstantApps.isInstantApp
import com.google.android.gms.instantapps.InstantApps
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

private const val LOG_NAME = "tabslite.TabDetailFragm"

/**
 * A fragment representing a single Tab detail screen.  This is the main purpose of the app, containing
 * tab specific info, and a TabView for displaying the actual tab.
 */
class TabDetailFragment : Fragment() {
    private val timerHandler = Handler()
    private var isScrolling: Boolean = false
    private var scrollDelayMs: Long = 34  // default scroll speed (smaller is faster)
    private var currentChordDialog: ChordBottomSheetDialogFragment? = null

    private var tabId: Int? = null

    private lateinit var binding: FragmentTabDetailBinding
    private lateinit var optionsMenu: Menu

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
        setHasOptionsMenu(true)

        // disable next and previous buttons until they're set to the correct action
        binding.enableNext = false
        binding.enablePrev = false

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
        binding.tabContent.setCallback(object : TabTextView.Callback {
            override fun chordClicked(chordName: CharSequence) {
                this@TabDetailFragment.chordClicked(chordName)
            }
        })



        /* ************************************     Load Tab/UI     ************************************ */
        arguments?.let { argBundle ->
            tabId = argBundle.getInt("tabId")  // should we replace this with a TabBasic since that's normally what the sender already has?

            val isPlaylist = argBundle.getBoolean("isPlaylist", false)
            val playlistEntry = argBundle.getParcelable<PlaylistEntry>("playlistEntry") //todo: test what happens when this is null
            loadTab(tabId!!, binding, isPlaylist, playlistEntry)  // load current tab


            // playlist related buttons
            val playlistName = argBundle.getString("playlistName", "")
            binding.isPlaylist = isPlaylist
            binding.playlistNameStr = playlistName
            binding.nextTabButtonText = "Next"
        }

        return binding.root
    }

    override fun onPause() {
        // consider pausing the autoscroll
        currentChordDialog?.dismiss()  // this doesn't parcelize well, so get rid of it before we pause
        super.onPause()
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
     * @param   tabid           the ID of the tab to load into the view
     * @param   binding         the view binding to reset UI elements
     * @param   isPlaylist      whether this tab is to be loaded as a playlist entry
     * @param   playlistEntry   if [isPlaylist], the playlist entry corresponding to the tab to be loaded
     */
    private fun loadTab(tabid: Int, binding: FragmentTabDetailBinding, isPlaylist: Boolean = false, playlistEntry: PlaylistEntry? = null) {
        Log.i(LOG_NAME, "Loading tab $tabid")
        tabId = tabid // hopefully we can get rid of this global variable eventually
        val getDataJob = GlobalScope.async { TabHelper.fetchTabFromInternet(getTabId(), AppDatabase.getInstance(requireContext())) }
        getDataJob.invokeOnCompletion(onDataStored(tabid, playlistEntry))

        // transpose
        binding.transposeUp.setOnClickListener { transpose(true, playlistEntry) }
        binding.transposeDown.setOnClickListener { transpose(false, playlistEntry) }
        binding.cancelTranspose.setOnClickListener { binding.tab?.let { transpose(-binding.tab!!.transposed, playlistEntry) } }


        // get next and previous tabs if they exist  //todo: move to new function
        if (isPlaylist && playlistEntry != null ) {
            // update NEXT buttons
            val nextId = playlistEntry.nextEntryId
            if (nextId != null) {
                val getNextJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistEntryDao().getEntryById(nextId) }
                getNextJob.invokeOnCompletion {
                    val nextEntry = getNextJob.getCompleted()
                    if (nextEntry != null) {
                        // we now have the info for when the user clicks the Next buttons.
                        binding.enableNext = true
                        binding.btnNext.setOnClickListener { loadTab(nextEntry.tabId, binding, isPlaylist, nextEntry) }
                        binding.btnTopSkipNext.setOnClickListener { loadTab(nextEntry.tabId, binding, isPlaylist, nextEntry) }

                    } else {
                        Log.w(LOG_NAME, "Warning! nextId != null, but fetching the referenced entry returned null.  This should not happen.  TabId $tabid, nextId $nextId")
                    }
                }
            } else {
                // no next tab in this playlist
                binding.enableNext = false
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
                        binding.enablePrev = true
                        binding.btnPrev.setOnClickListener { loadTab(prevEntry.tabId, binding, isPlaylist, prevEntry) }
                        binding.btnTopSkipPrev.setOnClickListener { loadTab(prevEntry.tabId, binding, isPlaylist, prevEntry) }
                        Log.d(LOG_NAME, "set playlist buttons")
                    } else {
                        Log.w(LOG_NAME, "Warning! prevId != null, but fetching the referenced entry returned null.  This should not happen.  TabId $tabid, prevId $prevId")
                    }
                }
            } else {
                // no previous tab in this playlist
                binding.enablePrev = false
            }
        }
    }

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
    private fun setScrollCallback(binding: FragmentTabDetailBinding) {
        binding.apply {
            // autoscroll
            val timerRunnable: Runnable = object : Runnable {
                override fun run() {
                    tabDetailScrollview.smoothScrollBy(0, 1) // 5 is how many pixels you want it to scroll vertically by
                    timerHandler.postDelayed(this, scrollDelayMs) // 10 is how many milliseconds you want this thread to run
                }
            }

            callback = object : Callback {
                override fun scrollButtonClicked() {
                    if (isScrolling) {
                        // stop scrolling
                        autoscrollSpeed.alpha = 1.0F
                        fab.alpha = 1.0F

                        timerHandler.removeCallbacks(timerRunnable)
                        fab.setImageResource(R.drawable.ic_fab_autoscroll)
                        autoscrollSpeed.isGone = true
                        (activity as AppCompatActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        binding.appbar.setExpanded(true, true)  // thanks https://stackoverflow.com/a/32137264/3437608
                    } else {
                        // start scrolling
                        timerHandler.postDelayed(timerRunnable, 0)
                        fab.setImageResource(R.drawable.ic_fab_pause_autoscroll)
                        autoscrollSpeed.isGone = false
                        autoscrollSpeed.alpha = 1.0F
                        (activity as AppCompatActivity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        binding.appbar.setExpanded(false, true)
                        Handler().postDelayed({
                            autoscrollSpeed.alpha = 0.4F
                            fab.alpha = 0.4F
                        }, 100)
                    }
                    isScrolling = !isScrolling
                }
            }
        }
    }
    private fun getTabId(): Int {
        return if (tabId != null) {
            Log.v(LOG_NAME, "Getting tab ID (local): $tabId")
            tabId!!
        } else {
            throw Exception("TabID requested before being set.")
        }
    }
    private var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // updated continuously as the user slides the thumb
            //convert progress to delay between 1px updates
            var myDelay = (100 - progress) / 100.0  // delay on a scale of 0 to 1
            myDelay *= 63                           // delay on a scale of 0 to 63  -- this sets the slowest autoscroll speed
            myDelay += 2                            // delay on a scale of 2 to 65  -- this sets the fastest autoscroll speed
            scrollDelayMs = (myDelay).toLong()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // called when the user first touches the SeekBar
            // thanks stackoverflow.com/a/7689776
            binding.autoscrollSpeed.alpha = 1.0F  // set opacity to 100% while touching the bar
            binding.fab.alpha = 1.0F
        }
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // called when the user first touches the SeekBar
            // thanks stackoverflow.com/a/7689776
            binding.autoscrollSpeed.alpha = 0.4F  // set opacity to 40% while not touching the bar
            binding.fab.alpha = 0.4F
        }
    }

    private fun transpose(up: Boolean, playlistEntry: PlaylistEntry?){
        val howMuch = if(up) 1 else -1
        transpose(howMuch, playlistEntry)
    }
    /**
     * update local variables, database, and the tab content view with a new transposition level.
     * Updates the chords (content) as well as the TextView showing the current transposition level.
     *
     * @param howMuch   the amount to transpose, relative to the current transposition amount.
     */
    private fun transpose(howMuch: Int, playlistEntry: PlaylistEntry?){
        binding.tab?.apply {
            transposed += howMuch  // update the view

            //13 half steps in an octave (both sides inclusive)
            if (transposed >= 12) {
                transposed -= 12
            } else if (transposed <= -12) {
                transposed += 12
            }

            Log.v(LOG_NAME, "Updating transpose level to $transposed by transposing $howMuch")
            binding.key.text = TabTextView.transposeChord(binding.key.text, howMuch)
            binding.tabContent.transpose(howMuch)  // the actual transposition
            binding.transposeText = transposed.toString()


            if (playlistEntry != null) {
                playlistEntry.transpose = transposed
                Log.d(
                    LOG_NAME,
                    "updated playlist entry transposition to $transposed: ${playlistEntry.transpose}"
                )
                GlobalScope.launch {
                    AppDatabase.getInstance(requireContext()).playlistEntryDao().update(
                        playlistEntry
                    )
                }
            } else {
                GlobalScope.launch {
                    TabHelper.updateTabTransposeLevel(
                        tabId,
                        transposed,
                        AppDatabase.getInstance(requireContext())
                    )
                }  // todo: do something different if it's a playlist
            }
        }
    }

    private fun onDataStored(tabid: Int, playlistEntry: PlaylistEntry? = null) = { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.w(LOG_NAME, "Error fetching and storing tab data from online source on the async thread.  Internet connection likely not available.")
            requireActivity().runOnUiThread {
                binding.progressBar2.isGone = true
                view?.let { Snackbar.make(it, "This tab is not available offline.", Snackbar.LENGTH_INDEFINITE).show() }
            }
        } else {
            startGetData(tabid, playlistEntry)
        }
    }

    /**
     * starts here coming from the favorite tabs page; assumes data is already in db
     *
     * @param tabId         the ID of the tab to retrieve from the database
     * @param playlistEntry (optional) if part of a playlist, set this option to use the playlist specific transposition settings, etc.
     */
    private fun startGetData(tabId: Int, playlistEntry: PlaylistEntry? = null) {
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

                    if (binding.tab != null && binding.tab!!.tabId == tabId) {  // we're reloading the same tab
                        val favorite: Boolean = binding.tab!!.favorite
                        TabHelper.setFavorite(getTabId(), favorite, AppDatabase.getInstance(requireContext()))  // reloading would reset favorite status, so save that
                        fetchedTab.favorite = favorite  // update UI
                    }

                    Log.v(LOG_NAME, "Set binding.tab to tab fetched from database.")
                    // thanks https://cheesecakelabs.com/blog/understanding-android-views-dimensions-set/
                    binding.tabContent.doOnLayout {
                        activity?.runOnUiThread {
                            binding.tabContent.setTabContent(fetchedTab.content)
                            Log.v(LOG_NAME, "Processed tab content for tab (${getTabId()}) '${fetchedTab.songName}'.  Length: ${fetchedTab.content.length}")

                            setHeartInitialState()  // set initial state of "save" heart
                            (activity as AppCompatActivity).title = fetchedTab.toString()  // toolbar title
                            binding.progressBar2.isGone = true

                            binding.tab = fetchedTab

                            // update transposition
                            var tspAmt = fetchedTab.transposed
                            binding.tab!!.transposed = 0        // currently, the tab hasn't been transposed.  The transpose() function will change the tab.transpose variable
                            if (playlistEntry != null)
                                tspAmt = playlistEntry.transpose
                            transpose(tspAmt, playlistEntry)

                            Log.
                            v(LOG_NAME, "Updated Tab UI for tab ($tabId) '${fetchedTab.songName}'")
                        }
                    }
                }
            }
        } catch (ex: IllegalStateException){
            Log.w(LOG_NAME, "TabDetailFragment could not get data.  Likely the window was closed before the process could start, in which case this message can be ignored.", ex)
        }
    }


    private fun setHeartInitialState(){
        if(binding.tab != null && binding.tab!!.favorite && this::optionsMenu.isInitialized) {
            val heart = optionsMenu.findItem(R.id.action_favorite)
            heart.isChecked = true
            heart.setIcon(R.drawable.ic_favorite)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tab_detail, menu)
        optionsMenu = menu

        context?.let {if( isInstantApp(it) ){
            menu.findItem(R.id.get_app).isVisible = true
        }}

        setHeartInitialState()
    }

    // assumes viewModel is initialized and tab exists
    private fun favoriteWhileInstant(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Download full version for all features")
        builder.setMessage("This tab has been saved for offline in your favorites, but the only " +
                "way to access it is via link since you're using the Instant version.  To view " +
                "your favorite tabs in a list, please upgrade to the full version of the app.")

        builder.setPositiveButton("Upgrade") { dialog: DialogInterface, _: Int ->
            showInstallPrompt()
            dialog.dismiss()
        }

        builder.setNeutralButton("Copy Link") { dialog: DialogInterface, _: Int ->
            val link = binding.tab!!.getUrl()
            val title = binding.tab.toString()
            val clipBoard = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(title, link)
            clipBoard.setPrimaryClip(clipData)

            view?.let { Snackbar.make(it, "Link copied to clipboard.", Snackbar.LENGTH_SHORT) }  // todo: @string-ify
            dialog.dismiss()
        }

        builder.create()
        builder.show()
    }

    private fun showInstallPrompt() {
        val postInstall: Intent
        if(binding.tab != null){
            postInstall = Intent(Intent.ACTION_VIEW)
            postInstall.data = binding.tab!!.getUrl().toUri()
            postInstall.setPackage("com.gbros.tabslite")
            postInstall.setClassName("com.gbros.tabslite", "com.gbros.tabslite.TabDetailActivity")
        } else {
           postInstall = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .setPackage("com.gbros.tabslite")
        }
        InstantApps.showInstallPrompt((activity as Activity), postInstall, 0, null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                if(!(activity?.application as DefaultApplication).runningOnFirebaseTest()){
                    createShareIntent()     // disable share menu for test lab
                }
                true
            }
            R.id.action_favorite -> {
                if(binding.tab != null) {
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        // now it's a favorite
                        item.setIcon(R.drawable.ic_favorite)
                        binding.tab!!.favorite = true

                        context?.let {if( isInstantApp(it) ) {
                            favoriteWhileInstant()
                        }}
                    } else {
                        item.setIcon(R.drawable.ic_unfavorite)
                        binding.tab!!.favorite = false
                    }
                    tabId?.let { TabHelper.setFavorite(it, item.isChecked, AppDatabase.getInstance(requireContext())) }
                }
                true
            }
            R.id.action_reload -> {  // reload button clicked (refresh page)
                binding.progressBar2.isGone = false
                val fetchTabFromInternetJob = GlobalScope.async {
                    TabHelper.fetchTabFromInternet(tabId = getTabId(), force = true, database = AppDatabase.getInstance(requireContext()))
                }
                fetchTabFromInternetJob.invokeOnCompletion(onDataStored(getTabId()))
                true
            }
            R.id.dark_mode_toggle -> {
                // show dialog asking user which mode they want
                context?.let { (activity?.application as DefaultApplication).darkModeDialog(it) }
                true
            }
            R.id.get_app -> {
                showInstallPrompt()
                true
            }
            R.id.action_add_to_playlist -> {
                // show add to playlist dialog
                Log.v(LOG_NAME, "Adding tab to playlist")
                val getPlaylistsFromDbJob = GlobalScope.async { AppDatabase.getInstance(requireContext()).playlistDao().getCurrentPlaylists() }
                getPlaylistsFromDbJob.invokeOnCompletion {
                    val playlists = getPlaylistsFromDbJob.getCompleted()
                    val transposition = if (binding.tab == null) 0 else binding.tab!!.transposed
                    AddToPlaylistDialogFragment(getTabId(), playlists, transposition).show(childFragmentManager, "AddToPlaylistDialogTag")
                    Log.v(LOG_NAME, "Add to playlist task handed off to dialog.")
                }

                true
            }
            else -> false
        }
    }

    // Helper function for calling a share functionality.
    private fun createShareIntent() {
        val shareText = binding.tab.let { tab ->
            if (tab == null) {
                ""
            } else {
                getString(R.string.share_text_plant, tab.toString(), binding.tab!!.getUrl())
            }
        }

        val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
                .setText(shareText)
                .setType("text/plain")
                .createChooserIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }

    interface Callback {
        fun scrollButtonClicked()
    }

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
