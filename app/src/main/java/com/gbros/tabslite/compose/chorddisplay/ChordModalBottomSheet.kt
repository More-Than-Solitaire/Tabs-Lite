package com.gbros.tabslite.compose.chorddisplay

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gbros.tabslite.data.chord.ICompleteChord
import com.gbros.tabslite.ui.theme.AppTheme

private const val LOG_NAME = "tabslite.ChordModalB"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordModalBottomSheet(chords: ICompleteChord, onDismissRequest: () -> Unit){
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        ChordPager(chords = chords)
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
fun ChordModalBottomSheetPreview (onDismissRequest: () -> Unit) {
    AppTheme {
        ChordModalBottomSheet(chords = CompleteChordForTest(), onDismissRequest)
    }
}
