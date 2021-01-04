package com.gbros.tabslite

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.databinding.FragmentTabDetailBinding
import com.gbros.tabslite.utilities.TabHelper
import com.gbros.tabslite.workers.UgApi
import com.google.android.gms.common.wrappers.InstantApps.isInstantApp
import com.google.android.gms.instantapps.InstantApps
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.TabDetailFragm"

/**
 * A fragment representing a single Tab detail screen
 */
class TabDetailFragment : Fragment() {
    private val timerHandler = Handler()
    private var isScrolling: Boolean = false
    private var scrollDelayMs: Long = 34  // default scroll speed (smaller is faster)
    private var currentChordDialog: ChordBottomSheetDialogFragment? = null

    private var tabId: Int? = null
    private var isPlaylist: Boolean = false
    private var playlistEntry: PlaylistEntry? = null

//    private lateinit var viewModel: TabDetailViewModel
    private lateinit var binding: FragmentTabDetailBinding
    private lateinit var optionsMenu: Menu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(LOG_NAME, "Starting TabDetailFragment")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_detail, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        arguments?.let { argBundle ->
            isPlaylist = argBundle.getBoolean("isPlaylist", false)
            tabId = argBundle.getInt("tabId")  // should we replace this with a TabBasic since that's normally what the sender already has?
            playlistEntry = argBundle.getParcelable<PlaylistEntry>("playlistEntry") //todo: test what happens when this is null


            val playlistName = argBundle.getString("playlistName", "")
            binding.isPlaylist = isPlaylist
            binding.playlistNameStr = playlistName
            binding.nextTabButtonText = "Next" // todo: get the next tab's title from db

            //todo: set next and previous buttons to navigate to the next tab.
            // also check whether the back button goes to the previous tab or back to the playlist
            // (should go back to playlist
        }

        // title bar
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }

        setScrollListener(binding) // scroll

        // autoscroll speed seek bar
        binding.autoscrollSpeed.clipToOutline = true  // not really needed since the background is enough bigger
        binding.autoscrollSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.autoscrollSpeed.isGone = true  // start hidden

        // transpose
        binding.transposeUp.setOnClickListener { transpose(true) }
        binding.transposeDown.setOnClickListener { transpose(false) }
        binding.cancelTranspose.setOnClickListener { transpose(-binding.tab!!.transposed) }  // fixme: this could cause a null pointer exception if tab didn't load

        binding.tabContent.setCallback(object : TabTextView.Callback {
            override fun chordClicked(chordName: CharSequence) {
                this@TabDetailFragment.chordClicked(chordName)
            }
        })
        return binding.root
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
    private fun setupAutoscroll(binding: FragmentTabDetailBinding) {
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
            Log.v(LOG_NAME, "Getting tab ID (activity level)")
            (activity as TabDetailActivity).tabId
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

    override fun onStart() {
        super.onStart()
        if (activity is SearchResultsActivity && (activity as SearchResultsActivity).getVersions != null) {
            (activity as SearchResultsActivity).getVersions!!.invokeOnCompletion(onDataStored())
        } else {
            Log.v(LOG_NAME, "Fetching tab from internet")
            val getDataJob = GlobalScope.async { TabHelper.fetchTabFromInternet(getTabId(), AppDatabase.getInstance(requireContext())) }
            getDataJob.invokeOnCompletion(onDataStored())
        }

        setupAutoscroll(binding)
    }
    override fun onPause() {
        currentChordDialog?.dismiss()  // this doesn't parcelize well, so get rid of it before we pause
        super.onPause()
    }

    private fun transpose(up: Boolean){
        val howMuch = if(up) 1 else -1
        transpose(howMuch)
    }

    /**
     * update local variables, database, and the tab content view with a new transposition level
     */
    private fun transpose(howMuch: Int){
        binding.tab?.apply {
            transposed += howMuch  // update the view

            //13 half steps in an octave (both sides inclusive)
            if (transposed >= 12) {
                transposed -= 12
            } else if (transposed <= -12) {
                transposed += 12
            }

            Log.v(LOG_NAME, "Updating transpose level to $transposed by transposing $howMuch")
            binding.tabContent.transpose(howMuch)  // the actual transposition
            binding.transposeText = transposed.toString()
            GlobalScope.launch { TabHelper.updateTabTransposeLevel(tabId, transposed, AppDatabase.getInstance(requireContext())) }  // todo: do something different if it's a playlist
        }
    }

    private fun onDataStored() = { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.w(LOG_NAME, "Error fetching and storing tab data from online source on the async thread.  Internet connection likely not available.")
            requireActivity().runOnUiThread {
                binding.progressBar2.isGone = true
                view?.let { Snackbar.make(it, "This tab is not available offline.", Snackbar.LENGTH_INDEFINITE).show() }
            }
            Unit
        } else {
            startGetData()
            Unit
        }
    }

    //starts here coming from the favorite tabs page; assumes data is already in db
    private fun startGetData() {
        try {
            tabId?.let {
                val ctxt = requireContext()
                val getTabFromDbJob = GlobalScope.async {
                    Log.v(LOG_NAME, "Fetching tab $tabId from the database.")
                    val db = AppDatabase.getInstance(ctxt).tabFullDao()
                    Log.d(LOG_NAME, "database acquired")
                    db.getTab(771119)
                }

                getTabFromDbJob.invokeOnCompletion { cause: Throwable? ->
                    if (cause != null) {
                        //oh no; something happened and it failed.  whoops.
                        Log.e(LOG_NAME, "Error fetching tab data from database.")
                        Unit
                    } else {
                        Log.v(LOG_NAME, "Data Received for tab fetch")
                        var reloaded = false
                        var favorite = false
                        val scrollSpeed = scrollDelayMs
                        var tspAmt = 0
                        if (binding.tab != null) {  // save properties if we're reloading
                            reloaded = true
                            favorite = binding.tab!!.favorite  //reloading would reset favorite status, so save that
                            //todo: when scroll speed is a database field, we'll need to save it here
                            tspAmt = binding.tab!!.transposed
                        }

                        // todo: when isPlaylist set tspAmt from playlistEntry

                        val fetchedTab = getTabFromDbJob.getCompleted()  // actually get the data
                        Log.v(LOG_NAME, "Set binding.tab to tab fetched from database.")

                        if (reloaded) {  // restore some settings from prior to refresh (like transpostion)
                            fetchedTab.favorite = favorite
                            TabHelper.setFavorite(getTabId(), favorite, AppDatabase.getInstance(requireContext()))

                            scrollDelayMs = scrollSpeed     // todo: save scroll speed to db
                        }


                        // thanks https://cheesecakelabs.com/blog/understanding-android-views-dimensions-set/
                        binding.tabContent.doOnLayout {
                            activity?.runOnUiThread {
                                binding.tabContent.setTabContent(fetchedTab!!.content)
                                Log.v(LOG_NAME, "Processed tab content for tab (${getTabId()}) '${fetchedTab.songName}'")

                                setHeartInitialState()  // set initial state of "save" heart
                                (activity as AppCompatActivity).title = fetchedTab.toString()  // toolbar title
                                binding.progressBar2.isGone = true


                                if (tspAmt == 0 && (activity is TabDetailActivity) && (activity as TabDetailActivity).tsp != 0) {  // tspAmt take precedence
                                    // launched via a link with a set transpose option;  override current settings
                                    tspAmt = (activity as TabDetailActivity).tsp
                                }

                                binding.tab = fetchedTab
                                transpose(tspAmt)  // works since we never set fetchedTab.transpose.  This will set that for us

                                Log.v(LOG_NAME, "Updated Tab UI for tab ($tabId) '${fetchedTab.songName}'")
                            }
                        }

                        Unit
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

        if(isInstantApp(context)){
            menu.findItem(R.id.get_app).isVisible = true
        }

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

                        if(isInstantApp(context)) {
                            favoriteWhileInstant()
                        }
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
                fetchTabFromInternetJob.invokeOnCompletion(onDataStored())
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
                        updateJob.invokeOnCompletion { cause ->
                            if (cause != null) {
                                Log.w(LOG_NAME, "Chord update didn't work.", cause.cause)
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
                        currentChordDialog?.show((activity as AppCompatActivity).supportFragmentManager, null)
                    }
                }
            }
        }
    }

}
