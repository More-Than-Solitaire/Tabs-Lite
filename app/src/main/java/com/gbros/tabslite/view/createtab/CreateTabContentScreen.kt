package com.gbros.tabslite.view.createtab

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.TabDifficulty
import com.gbros.tabslite.data.tab.TabTuning
import com.gbros.tabslite.utilities.KeepScreenOn
import com.gbros.tabslite.view.tabview.TabText
import com.gbros.tabslite.viewmodel.CreateTabViewModel
import kotlinx.coroutines.launch

const val CREATE_TAB_SONG_ID_NAV_ARG = "songId"
const val CREATE_TAB_CONTENT_ROUTE = "createtab/forSong/%s"

fun NavController.navigateToCreateTabContent(songId: String) {
    navigate(CREATE_TAB_CONTENT_ROUTE.format(songId))
}

fun NavGraphBuilder.createTabContentScreen(onNavigateBack: () -> Unit) {
    composable(route = CREATE_TAB_CONTENT_ROUTE.format("{$CREATE_TAB_SONG_ID_NAV_ARG}")) { navBackStackEntry ->
        val songId = navBackStackEntry.arguments!!.getString(CREATE_TAB_SONG_ID_NAV_ARG, "")
        val db = AppDatabase.getInstance(LocalContext.current)
        val createTabViewModel: CreateTabViewModel = hiltViewModel<CreateTabViewModel, CreateTabViewModel.CreateTabViewModelFactory> { factory ->
            factory.create(dataAccess = db.dataAccess(), selectedSongId = songId)
        }

        CreateTabContentScreen(
            viewState = createTabViewModel,
            capoUpdated = createTabViewModel::capoUpdated,
            contentUpdated = createTabViewModel::contentUpdated,
            difficultyUpdated = createTabViewModel::difficultyUpdated,
            tuningUpdated = createTabViewModel::tuningUpdated,
            versionDescriptionUpdated = createTabViewModel::versionDescriptionUpdated,
            saveTab = createTabViewModel::submitTab,
            navigateBack = onNavigateBack,
            insertChord = createTabViewModel::insertChord
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateTabContentScreen(
    viewState: CreateTabViewModel,
    capoUpdated: (Int) -> Unit,
    contentUpdated: (TextFieldValue) -> Unit,
    difficultyUpdated: (TabDifficulty) -> Unit,
    tuningUpdated: (TabTuning) -> Unit,
    versionDescriptionUpdated: (String) -> Unit,
    saveTab: () -> Unit,
    navigateBack: () -> Unit,
    insertChord: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    KeepScreenOn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        TopAppBar(
            title = { Text(text = "Create new tab") },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(text = { Text("Details") },
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } })
            Tab(text = { Text("Content") },
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } })
            Tab(text = { Text("Preview") },
                selected = pagerState.currentPage == 2,
                onClick = {
                    keyboardController?.hide()
                    scope.launch { pagerState.animateScrollToPage(2) }
                })
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> { // Details page
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = viewState.selectedSongName.observeAsState("").value,
                            onValueChange = {},
                            label = { Text("Song Name") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewState.selectedArtistName.observeAsState("").value,
                            onValueChange = { },
                            label = { Text("Artist Name") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewState.versionDescription.observeAsState("").value,
                            onValueChange = versionDescriptionUpdated,
                            label = { Text("Version Description") },
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val difficultyState = viewState.difficulty.observeAsState(TabDifficulty.NotSet)
                        var difficultyExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = difficultyExpanded,
                            onExpandedChange = { difficultyExpanded = !difficultyExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fillMaxWidth = Modifier
                                .fillMaxWidth()
                            OutlinedTextField(
                                modifier = fillMaxWidth.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, true),
                                value = difficultyState.value.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Difficulty") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = difficultyExpanded,
                                onDismissRequest = { difficultyExpanded = false }
                            ) {
                                TabDifficulty.entries.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            difficultyUpdated(item)
                                            difficultyExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val tuningState = viewState.tuning.observeAsState(TabTuning.Standard)
                        var tuningExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = tuningExpanded,
                            onExpandedChange = { tuningExpanded = !tuningExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fillMaxWidth = Modifier
                                .fillMaxWidth()
                            OutlinedTextField(
                                modifier = fillMaxWidth.menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, true),
                                value = tuningState.value.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tuning") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tuningExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = tuningExpanded,
                                onDismissRequest = { tuningExpanded = false }
                            ) {
                                TabTuning.entries.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            tuningUpdated(item)
                                            tuningExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val capo = viewState.capo.observeAsState(0)
                        OutlinedTextField(
                            value = capo.value.toString(),
                            onValueChange = { capoUpdated(it.toIntOrNull() ?: 0) },
                            label = { Text("Capo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )


                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next")
                        }
                    }
                }
                1 -> { // Content page
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        val tabContent by viewState.content.observeAsState(TextFieldValue())
                        var chordToInsert by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = tabContent,
                            onValueChange = contentUpdated,
                            label = { Text("Tab Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = chordToInsert,
                                onValueChange = { chordToInsert = it },
                                label = { Text("Chord") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                            )
                            Button(onClick = {
                                insertChord(chordToInsert)
                                chordToInsert = ""
                            }) {
                                Text("Insert")
                            }
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                scope.launch { pagerState.animateScrollToPage(2) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Preview")
                        }
                    }
                }
                2 -> { // Preview page
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        val content by viewState.content.observeAsState(TextFieldValue())
                        TabText(
                            text = parseTabToAnnotatedString(content.text),
                            fontSizeSp = 14f,
                            onChordClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = saveTab,
                                modifier = Modifier.weight(1f),
                                enabled = viewState.dataValidated.observeAsState(false).value
                            ) {
                                Text("Save Tab")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseTabToAnnotatedString(content: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val regex = Regex("\\{ch:([^}]+)\\}")
    var lastIndex = 0

    regex.findAll(content).forEach { result ->
        val chord = result.groupValues[1]
        val startIndex = result.range.first

        // Append text before the match
        if (startIndex > lastIndex) {
            builder.append(content.substring(lastIndex, startIndex))
        }

        // Append an annotated space for the chord
        builder.withAnnotation("chord", chord) {
            append(" ")
        }

        lastIndex = result.range.last + 1
    }

    // Append remaining text
    if (lastIndex < content.length) {
        builder.append(content.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}
