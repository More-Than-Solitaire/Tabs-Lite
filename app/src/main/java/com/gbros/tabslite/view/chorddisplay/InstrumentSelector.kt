package com.gbros.tabslite.view.chorddisplay

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.R
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun InstrumentSelector(selectedInstrument: Instrument, onInstrumentSelected: (Instrument) -> Unit) {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectableIconButton(
            onClick = { onInstrumentSelected(Instrument.Guitar) },
            selected = selectedInstrument == Instrument.Guitar,
            iconId = R.drawable.ic_tabslite_guitar,
            contentDescription = stringResource(R.string.instrument_title_guitar),
        )
        SelectableIconButton(
            onClick = { onInstrumentSelected(Instrument.Ukulele) },
            selected = selectedInstrument == Instrument.Ukulele,
            iconId = R.drawable.ic_ukulele,
            contentDescription = stringResource(R.string.instrument_title_ukulele),
        )
    }
}

@Composable
private fun SelectableIconButton(
    onClick: () -> Unit,
    selected: Boolean,
    @DrawableRes iconId: Int,
    contentDescription: String,
    enabled: Boolean = true,
) {
    val icon = @Composable {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconId),
            contentDescription = contentDescription
        )
    }

    if (selected) {
        FilledTonalButton(
            onClick = { },  // this is already selected; ignore the tap
            enabled = enabled,
            content = {
                icon()
                Text(
                    text = contentDescription
                )
            },
        )
    }
    else {
        OutlinedIconButton(
            onClick = onClick,
            enabled = enabled,
            content = icon,
        )
    }
}



@Preview
@Composable
private fun InstrumentSelectorPreview() {
    AppTheme {
        InstrumentSelector(selectedInstrument = Instrument.Guitar, {})
    }
}