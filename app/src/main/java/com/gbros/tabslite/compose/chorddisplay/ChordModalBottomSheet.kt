package com.gbros.tabslite.compose.chorddisplay

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.Chord
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.ui.theme.AppTheme

private const val LOG_NAME = "tabslite.ChordModalB"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordModalBottomSheet(chord: String, onDismiss: () -> Unit){
    val currentContext = LocalContext.current
    var chordVariations: List<ChordVariation> by remember { mutableStateOf(listOf()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        ChordPager(chordVariations = chordVariations)
        Spacer(Modifier.navigationBarsPadding())
    }

    LaunchedEffect(key1 = Unit) {
        val db = AppDatabase.getInstance(currentContext)
        chordVariations = Chord.getChord(chord, db)
    }
}

@Composable
fun ChordModalBottomSheetPreview (onDismissRequest: () -> Unit) {
    AppTheme {
        ChordModalBottomSheet(chord = "Am7", onDismissRequest)
    }
}



