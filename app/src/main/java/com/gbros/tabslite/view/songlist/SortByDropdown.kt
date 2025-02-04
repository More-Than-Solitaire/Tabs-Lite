package com.gbros.tabslite.view.songlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gbros.tabslite.R
import com.gbros.tabslite.view.playlists.PlaylistsSortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByDropdown(selectedSort: SortBy?, onOptionSelected: (SortBy) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {  }, modifier = Modifier
        .fillMaxWidth()
    ) {
        Button(
            onClick = { expanded = !expanded},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
        ) {
            Text(String.format(stringResource(id = R.string.sort_by),
                selectedSort?.let { SortBy.getString(it) } ?: ""))
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (sortOption in SortBy.entries) {
                DropdownMenuItem(
                    text = { Text(text = SortBy.getString(sortOption)) },
                    onClick = { expanded = false; onOptionSelected(sortOption) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByDropdown(selectedSort: PlaylistsSortBy?, onOptionSelected: (PlaylistsSortBy) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {  }, modifier = Modifier
        .fillMaxWidth()
    ) {
        Button(
            onClick = { expanded = !expanded},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
        ) {
            Text(String.format(stringResource(id = R.string.sort_by),
                selectedSort?.let { PlaylistsSortBy.getString(it) } ?: ""))
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (sortOption in PlaylistsSortBy.entries) {
                DropdownMenuItem(
                    text = { Text(text = PlaylistsSortBy.getString(sortOption)) },
                    onClick = { expanded = false; onOptionSelected(sortOption) }
                )
            }
        }
    }
}
