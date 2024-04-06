package com.gbros.tabslite.compose.chorddisplay

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.compose.card.ErrorCard
import com.gbros.tabslite.compose.tabview.TabText
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.Chord
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.ui.theme.AppTheme
import kotlinx.coroutines.delay

private const val LOG_NAME = "tabslite.ChordModalB"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordModalBottomSheet(chord: String, onDismiss: () -> Unit){
    val currentContext = LocalContext.current
    var chordVariations: List<ChordVariation> by remember { mutableStateOf(listOf()) }
    var loading by remember { mutableStateOf(true) }

    ModalBottomSheet(onDismissRequest = onDismiss, windowInsets = WindowInsets(0,0,0,0)) {
        if (chordVariations.isNotEmpty()) {
            loading = false;
            ChordPager(
                chordVariations = chordVariations,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            // show loading progress indicator
            Box(
                modifier = Modifier
                    .height(312.dp)  // this is the size of the components above added together, minus the text
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator()
                    LaunchedEffect(key1 = Unit) {
                        delay(3000)
                        loading = false // loading failed after 5 seconds
                    }
                } else {
                    ErrorCard(text = "Couldn't load chord '$chord'. Please check your internet connection.")
                }
            }
        }

        // leave space for the nav bar on top of the bottom sheet
        val navHeight = NavigationBarDefaults.windowInsets.getBottom(Density(1f))
        Spacer(modifier = Modifier.height(navHeight.dp))
    }

    LaunchedEffect(key1 = Unit) {
        val db = AppDatabase.getInstance(currentContext)
        chordVariations = Chord.getChord(chord, db)
    }
}

@Composable @Preview
private fun ChordModalBottomSheetPreview () {

    AppTheme {
        val testCase1 = """
        [tab]     [ch]C[/ch]                   [ch]Am[/ch] 
        That David played and it pleased the Lord[/tab]
    """.trimIndent()
        var bottomSheetTrigger by remember { mutableStateOf(true) }
        var chordToShow by remember { mutableStateOf("Am") }

        TabText(
            text = testCase1,
            onChordClick = { chordName ->
                chordToShow = chordName
                bottomSheetTrigger = true
            },
            modifier = Modifier.fillMaxSize()
        )

        if (bottomSheetTrigger) {
            ChordModalBottomSheet(chordToShow) {
                Log.d(LOG_NAME, "bottom sheet dismissed")
                bottomSheetTrigger = false
            }
        }
    }

}


