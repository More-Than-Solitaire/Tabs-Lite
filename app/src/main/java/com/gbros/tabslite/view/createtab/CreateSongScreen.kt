package com.gbros.tabslite.view.createtab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.SongGenre
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.card.InfoCard
import com.gbros.tabslite.viewmodel.CreateSongViewModel

private const val CREATE_SONG_ROUTE = "createtab/createSong/%s"
private const val CREATE_SONG_NAV_ARG = "title"


fun NavController.navigateToCreateSong(title: String = "") {
    navigate(CREATE_SONG_ROUTE.format(title))
}

fun NavGraphBuilder.createSongScreen(onNavigateBack: () -> Unit, navigateToCreateTabContent: (String) -> Unit) {
    composable(route = CREATE_SONG_ROUTE.format("{$CREATE_SONG_NAV_ARG}")) {
        val title = it.arguments?.getString(CREATE_SONG_NAV_ARG) ?: ""
        val createSongViewModel: CreateSongViewModel = hiltViewModel<CreateSongViewModel, CreateSongViewModel.CreateSongViewModelFactory> { factory ->
            factory.create(initialSongName = title) }

        CreateSongScreen(
            viewState = createSongViewModel,
            navigateBack = onNavigateBack,
            navigateToCreateTabContent = navigateToCreateTabContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSongScreen(
    viewState: ICreateSongViewState,
    navigateBack: () -> Unit,
    navigateToCreateTabContent: (String) -> Unit
) {
    val songName by viewState.songName.observeAsState("")
    val artistName by viewState.artistName.observeAsState("")
    val songGenre by viewState.songGenre.observeAsState("")
    val songCreationState by viewState.songCreationState.observeAsState()
    val newSongId by viewState.newSongId.observeAsState(null)

    // success dialog
    var successDialogSuppressed by remember(songCreationState) { mutableStateOf(false) }
    if (songCreationState is LoadingState.Success && !successDialogSuppressed) {
        AlertDialog(
            onDismissRequest = { successDialogSuppressed = true },
            title = { Text(stringResource(id = R.string.message_song_creation_success_title)) },
            text = { Text(stringResource(id = R.string.message_song_creation_success_description)) },
            confirmButton = {
                TextButton(onClick = { navigateToCreateTabContent(newSongId ?: "") }) {
                    Text(stringResource(id = R.string.generic_action_continue))
                }
            }
        )
    }

    // error message dialog
    var errorDialogSuppressed by remember(songCreationState) { mutableStateOf(false) }
    if (songCreationState is LoadingState.Error && !errorDialogSuppressed) {
        val errorMessage = (songCreationState as LoadingState.Error)
        AlertDialog(
            onDismissRequest = { errorDialogSuppressed = true },
            title = { Text(stringResource(id = R.string.message_song_creation_failed_title)) },
            text = { Text(stringResource(id = errorMessage.messageStringRef).format(errorMessage.errorDetails )) },
            confirmButton = {
                TextButton(onClick = { errorDialogSuppressed = true }) {
                    Text(stringResource(id = R.string.generic_action_close))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.title_create_song)) },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = songName,
                onValueChange = viewState::songNameUpdated,
                label = { Text(stringResource(id = R.string.label_create_tab_song_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            var currentArtistName by remember(artistName) { mutableStateOf(artistName) }
            OutlinedTextField(
                value = currentArtistName,
                onValueChange = { currentArtistName = it },
                label = { Text(stringResource(id = R.string.label_create_tab_artist_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().onFocusChanged({ focusState -> if (!focusState.isFocused) viewState.artistNameUpdated(currentArtistName) })
            )

            val artistFetchState by viewState.artistFetchState.observeAsState(LoadingState.NotStarted)
            if (artistFetchState is LoadingState.Error) {
                val errorMessage = (artistFetchState as LoadingState.Error)
                if (!errorMessage.errorDetails.isBlank()) {
                    ErrorCard(stringResource(id = errorMessage.messageStringRef).format(errorMessage.errorDetails))
                } else {
                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(all = 8.dp)

                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(id = R.string.info),
                                    modifier = Modifier.padding(all = 8.dp)
                                )
                                Text(
                                    text = stringResource(id = errorMessage.messageStringRef),
                                    modifier = Modifier.padding(all = 4.dp)
                                )
                            }
                            Button(onClick = viewState::createNewArtist) {
                                Text(stringResource(id = R.string.action_create_artist))
                            }
                        }
                    }
                }
            } else if (artistFetchState is LoadingState.Success) {
                InfoCard("Artist found!")
            }

            val genreState by viewState.songGenre.observeAsState(SongGenre.Other)
            var genreExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = genreExpanded,
                onExpandedChange = { genreExpanded = !genreExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val fillMaxWidth = Modifier
                    .fillMaxWidth()
                OutlinedTextField(
                    modifier = fillMaxWidth.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, true),
                    value = genreState .name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.label_create_song_genre)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = genreExpanded,
                    onDismissRequest = { genreExpanded = false }
                ) {
                    SongGenre.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                viewState.songGenreUpdated(item)
                                genreExpanded = false
                            }
                        )
                    }
                }
            }

            val fieldValidation by viewState.fieldValidation.observeAsState(false)
            Button(
                onClick = { viewState.createSong() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = fieldValidation && (songCreationState is LoadingState.NotStarted || songCreationState is LoadingState.Error)
            ) {
                Text(stringResource(id = R.string.action_create_song_and_continue))
            }
        }
    }
}
