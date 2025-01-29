package com.gbros.tabslite.view.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistHeader(
    title: LiveData<String>,
    description: LiveData<String>,
    titleChanged: (title: String) -> Unit,
    descriptionChanged: (description: String) -> Unit,
    navigateBack: () -> Unit,
    deletePlaylist: () -> Unit
) {
    var titleToDisplay: String by remember(key1 = title.observeAsState().value) { mutableStateOf(title.value ?: "") }
    var descriptionToDisplay: String by remember(key1 = description.observeAsState().value) { mutableStateOf(description.value ?: "") }

    var titleWasFocused: Boolean by remember { mutableStateOf(false) }
    var descriptionWasFocused: Boolean by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                TextField(
                    value = titleToDisplay,
                    onValueChange = {newTitle: String -> titleToDisplay = newTitle},
                    singleLine = true,
                    placeholder = { Text("Playlist Name") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (titleWasFocused && !it.isFocused && titleToDisplay != title.value) {
                                titleChanged(titleToDisplay)
                            }
                            titleWasFocused = it.isFocused
                        }
                )
            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = deletePlaylist) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        )

        TextField(
            value = descriptionToDisplay,
            onValueChange = { newDescription: String -> descriptionToDisplay = newDescription },
            placeholder = { Text("Playlist Description") },
            colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (descriptionWasFocused && !it.isFocused && descriptionToDisplay != description.value) {
                        descriptionChanged(descriptionToDisplay)
                    }
                    descriptionWasFocused = it.isFocused
                }
        )
    }
}

@Composable @Preview
private fun PlaylistHeaderPreview() {
    AppTheme {
        PlaylistHeader(MutableLiveData("Playlist title"), MutableLiveData("playlist description"), {}, {}, {}, {})
    }
}