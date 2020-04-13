package com.gbros.tabslite

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.databinding.FragmentTabDetailBinding
import com.gbros.tabslite.utilities.InjectorUtils
import com.gbros.tabslite.viewmodels.TabDetailViewModel
import com.google.android.gms.common.wrappers.InstantApps.isInstantApp
import com.google.android.gms.instantapps.InstantApps
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min

private const val LOG_NAME = "tabslite.TabDetailFragment"

/**
 * A fragment representing a single Tab detail screen
 */
class TabDetailFragment : Fragment() {
    private val timerHandler = Handler()
    private var isScrolling: Boolean = false
    private var scrollDelayMs: Long = 20  // default scroll speed (smaller is faster)
    private var currentChordDialog: ChordBottomSheetDialogFragment? = null

    //private lateinit var tab: TabFull
    private lateinit var viewModel: TabDetailViewModel
    private lateinit var binding: FragmentTabDetailBinding
    private lateinit var optionsMenu: Menu

    private var spannableText: SpannableStringBuilder = SpannableStringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(LOG_NAME, "Starting TabDetailFragment")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_detail, container, false)

        setHasOptionsMenu(true)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner


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
            tabDetailScrollview.setOnScrollChangeListener(scrollChangeListener)

            // title bar
            (activity as AppCompatActivity).apply {
                setSupportActionBar(binding.toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
            }

            // transpose
            transposeUp.setOnClickListener { _ -> transpose(true) }
            transposeDown.setOnClickListener { _ -> transpose(false) }

            // autoscroll speed seek bar
            autoscrollSpeed.clipToOutline = true  // not really needed since the background is enough bigger
            autoscrollSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
            autoscrollSpeed.isGone = true
        }

        binding.cancelTranspose.setOnClickListener { if(::viewModel.isInitialized) {
            val currentTransposeAmt = viewModel.tab!!.transposed
            transpose(-currentTransposeAmt)
        }}

        binding.tabContent.setCallback(object : TabTextView.Callback {
            override fun chordClicked(chordName: CharSequence) {
                this@TabDetailFragment.chordClicked(chordName)
            }
        })
        return binding.root
    }

    private fun getTabId(): Int {
        Log.d(LOG_NAME, "Getting tab ID")
        val id = (activity as TabDetailActivity).tabId
        Log.d(LOG_NAME, "Tab ID: $id")
        return id
    }
    override fun onStart() {
        super.onStart()
        if (activity is SearchResultsActivity && (activity as SearchResultsActivity).getVersions != null) {
            (activity as SearchResultsActivity).getVersions!!.invokeOnCompletion(onDataStored())
        } else {
            val getDataJob = GlobalScope.async { (activity as ISearchHelper).searchHelper?.fetchTab(getTabId()) }
            getDataJob.invokeOnCompletion(onDataStored())
        }


        // autoscroll
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
                        (activity as AppCompatActivity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        binding.appbar.setExpanded(false, true)
                    }
                    isScrolling = !isScrolling
                }
            }
        }
    }

    private var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // updated continuously as the user slides the thumb
            //convert progress to delay between 1px updates
            var myDelay = (100 - progress) / 100.0  // delay on a scale of 0 to 1
            myDelay *= 34                           // delay on a scale of 0 to 34
            myDelay += 2                            // delay on a scale of 2 to 36
            scrollDelayMs = (myDelay).toLong()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}  // called when the user first touches the SeekBar
        override fun onStopTrackingTouch(seekBar: SeekBar) {}  // called after the user finishes moving the SeekBar
    }

    override fun onPause() {
        currentChordDialog?.dismiss()  // this doesn't parcelize well, so get rid of it before we pause
        super.onPause()
    }

    private fun transpose(up: Boolean){
        if(! ::viewModel.isInitialized) {
            return
        }

        val howMuch = if(up) 1 else -1
        transpose(howMuch)
    }
    private fun transpose(howMuch: Int){
        viewModel.tab?.apply {
            transposed += howMuch

            //13 half steps in an octave (both sides inclusive)
            if (transposed >= 12) {
                transposed -= 12
            } else if (transposed <= -12) {
                transposed += 12
            }

            Log.v(LOG_NAME, "Updating transpose level to $transposed by transposing $howMuch")
            binding.tabContent.transpose(howMuch)
            binding.transposeAmt.text = transposed.toString()
            GlobalScope.launch { (activity as ISearchHelper).searchHelper?.updateTabTransposeLevel(tabId, transposed) }
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
            Log.v(LOG_NAME, "Getting Data")
            val tabDetailViewModel: TabDetailViewModel by viewModels {
                val mActivity = activity
                InjectorUtils.provideTabDetailViewModelFactory(requireActivity(), getTabId())
            }
            viewModel = tabDetailViewModel
            viewModel.getTabJob.invokeOnCompletion(onDataReceived())
        } catch (ex: IllegalStateException){
            Log.w(LOG_NAME, "TabDetailFragment could not get data.  Likely the window was closed before the process could start, in which case this message can be ignored.", ex)
        }
    }

    // app might currently crash if the database actually doesn't have the data (tab = null).  Shouldn't happen irl, but happened in early development
    private fun onDataReceived() =  { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.e(LOG_NAME, "Error fetching tab data from database.")
            Unit
        } else {
            Log.v(LOG_NAME, "Data Received for tab fetch")
            var reloaded = false
            var favorite = false
            val scrollSpeed = scrollDelayMs
            var tspAmt = 0
            if(viewModel.tab != null) {
                reloaded = true
                favorite = viewModel.tab!!.favorite  //reloading would reset favorite status, so save that
                //todo: when scroll speed is a database field, we'll need to save it here
                tspAmt = viewModel.tab!!.transposed
            }

            viewModel.tab = viewModel.getTabJob.getCompleted()  // actually get the data
            Log.v(LOG_NAME, "Set tab to viewmodel.")

            if (reloaded) {
                viewModel.tab!!.favorite = favorite
                viewModel.setFavorite(favorite)

                scrollDelayMs = scrollSpeed
                //todo: save scroll speed to db

                viewModel.tab!!.transposed = tspAmt
            }

            // thanks https://cheesecakelabs.com/blog/understanding-android-views-dimensions-set/
            binding.tabContent.doOnLayout {
                activity?.runOnUiThread {
                    binding.tabContent.setTabContent(viewModel.tab!!.content)
                    Log.v(LOG_NAME, "Processed tab content for tab (${viewModel.tab?.tabId}) '${viewModel.tab?.songName}'")

                    binding.tab = viewModel.tab  // set view data
                    setHeartInitialState()  // set initial state of "save" heart
                    (activity as AppCompatActivity).title = viewModel.tab.toString()  // toolbar title


                    binding.progressBar2.isGone = true

                    viewModel.tab?.apply {
                        if ((activity as TabDetailActivity).tsp != 0) {
                            // launched via a link with a set transpose option;  override current settings
                            transposed = (activity as TabDetailActivity).tsp
                        }

                        binding.transposeAmt.text = transposed.toString()
                        transpose(transposed)
                        Log.v(LOG_NAME, "Updated Tab UI for tab ($tabId) '$songName'")
                    }
                }
            }

            Unit
        }
    }

    private fun setHeartInitialState(){
        if(::viewModel.isInitialized && viewModel.tab != null && viewModel.tab!!.favorite && this::optionsMenu.isInitialized) {
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
            val link = viewModel.tab!!.getUrl()
            val title = viewModel.tab.toString()
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
        if(::viewModel.isInitialized && viewModel.tab != null){
            postInstall = Intent(Intent.ACTION_VIEW)
            postInstall.data = viewModel.tab!!.getUrl().toUri()
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
                if(::viewModel.isInitialized && !(activity?.application as DefaultApplication).runningOnFirebaseTest()){
                    createShareIntent()     // disable share menu for test lab
                }
                true
            }
            R.id.action_favorite -> {
                if(::viewModel.isInitialized && viewModel.tab != null) {
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        // now it's a favorite
                        item.setIcon(R.drawable.ic_favorite)
                        viewModel.tab!!.favorite = true

                        if(isInstantApp(context)) {
                            favoriteWhileInstant()
                        }
                    } else {
                        item.setIcon(R.drawable.ic_unfavorite)
                        viewModel.tab!!.favorite = false
                    }
                    viewModel.setFavorite(item.isChecked)
                }
                true
            }
            R.id.action_reload -> {  // reload button clicked (refresh page)
                if(::viewModel.isInitialized) {
                    viewModel.getTabJob = viewModel.viewModelScope.async { viewModel.tabRepository.getTab(getTabId()) }
                    val wasFavorite = viewModel.tab?.favorite
                }

                binding.progressBar2.isGone = false
                val searchJob = GlobalScope.async {
                    (activity as ISearchHelper).searchHelper?.fetchTab(tabId = getTabId(), force = true)
                }
                searchJob.start()
                searchJob.invokeOnCompletion(onDataStored())

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
            else -> false
        }
    }

    // Helper function for calling a share functionality.
    private fun createShareIntent() {
        val shareText = viewModel.tab.let { tab ->
            if (viewModel.tab == null) {
                ""
            } else {
                getString(R.string.share_text_plant, tab.toString(), viewModel.tab!!.getUrl())
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
        val api = (activity as ISearchHelper).searchHelper?.api ?: return  // return if api null
        val input = ArrayList<CharSequence>()
        input.add(chordName)

        val getChordsJob = GlobalScope.async { api.getChordVariations(chordName) }
        getChordsJob.invokeOnCompletion { cause ->
            if (cause != null) {
                Log.w(LOG_NAME, "Getting chords from db didn't work.", cause.cause)
                Unit
            } else {
                val chordVars = getChordsJob.getCompleted()
                if (chordVars.isEmpty()) {
                    if(!noUpdate) {
                        // get from the internet
                        val updateJob = GlobalScope.async { api.updateChordVariations(input) }
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
