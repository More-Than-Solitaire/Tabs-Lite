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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.TabDifficulty
import com.gbros.tabslite.data.tab.TabTuning
import com.gbros.tabslite.utilities.KeepScreenOn
import com.gbros.tabslite.view.tabview.TabText
import com.gbros.tabslite.viewmodel.CreateTabViewModel
import kotlinx.coroutines.launch

//#region use case start with blank content

private const val CREATE_TAB_SONG_ID_NAV_ARG = "songId"
private const val CREATE_TAB_CONTENT_ROUTE = "createtab/forSong/%s"

fun NavController.navigateToCreateTabContent(songId: String) {
    navigate(CREATE_TAB_CONTENT_ROUTE.format(songId))
}

fun NavGraphBuilder.createTabContentScreen(onNavigateBack: () -> Unit, onNavigateToTabByTabId: (String) -> Unit) {
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
            insertChord = createTabViewModel::insertChord,
            saveTab = createTabViewModel::submitTab,
            navigateBack = onNavigateBack,
            navigateToTabByTabId = onNavigateToTabByTabId
        )
    }
}

//#endregion

//#region use case start with prefilled content

private const val CREATE_TAB_PREFILLED_SONG_ID_NAV_ARG = "songId"
private const val CREATE_TAB_PREFILLED_TAB_ID_NAV_ARG = "tabId"
private const val CREATE_TAB_PREFILLED_CONTENT_ROUTE = "createtab/forSong/%s/withContent/%s"

fun NavController.navigateToPrefilledCreateTabContent(songId: String, tabId: String) {
    navigate(CREATE_TAB_PREFILLED_CONTENT_ROUTE.format(songId, tabId))
}

fun NavGraphBuilder.createTabContentPrefilledScreen(onNavigateBack: () -> Unit, onNavigateToTabByTabId: (String) -> Unit) {
    composable(route = CREATE_TAB_PREFILLED_CONTENT_ROUTE.format("{$CREATE_TAB_PREFILLED_SONG_ID_NAV_ARG}", "{$CREATE_TAB_PREFILLED_TAB_ID_NAV_ARG}")) { navBackStackEntry ->
        val songId = navBackStackEntry.arguments!!.getString(CREATE_TAB_PREFILLED_SONG_ID_NAV_ARG, "")
        val tabId = navBackStackEntry.arguments!!.getString(CREATE_TAB_PREFILLED_TAB_ID_NAV_ARG, "")
        val db = AppDatabase.getInstance(LocalContext.current)
        val createTabViewModel: CreateTabViewModel = hiltViewModel<CreateTabViewModel, CreateTabViewModel.CreateTabViewModelFactory> { factory ->
            factory.create(dataAccess = db.dataAccess(), selectedSongId = songId, startingContentTabId = tabId)
        }

        CreateTabContentScreen(
            viewState = createTabViewModel,
            capoUpdated = createTabViewModel::capoUpdated,
            contentUpdated = createTabViewModel::contentUpdated,
            difficultyUpdated = createTabViewModel::difficultyUpdated,
            tuningUpdated = createTabViewModel::tuningUpdated,
            versionDescriptionUpdated = createTabViewModel::versionDescriptionUpdated,
            insertChord = createTabViewModel::insertChord,
            saveTab = createTabViewModel::submitTab,
            navigateBack = onNavigateBack,
            navigateToTabByTabId = onNavigateToTabByTabId
        )
    }
}

//#endregion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateTabContentScreen(
    viewState: CreateTabViewModel,
    capoUpdated: (Int) -> Unit,
    contentUpdated: (TextFieldValue) -> Unit,
    difficultyUpdated: (TabDifficulty) -> Unit,
    tuningUpdated: (TabTuning) -> Unit,
    versionDescriptionUpdated: (String) -> Unit,
    insertChord: (String) -> Unit,
    saveTab: () -> Unit,
    navigateBack: () -> Unit,
    navigateToTabByTabId: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val submissionStatus = viewState.submissionStatus.observeAsState(LoadingState.NotStarted)
    KeepScreenOn()

    // success dialog
    var successDialogSuppressed by remember { mutableStateOf(false) }
    if (submissionStatus.value is LoadingState.Success && !successDialogSuppressed) {
        AlertDialog(
            onDismissRequest = { successDialogSuppressed = true },
            title = { Text(stringResource(id = R.string.message_tab_creation_success_title)) },
            text = { Text(stringResource(id = R.string.message_tab_creation_success_description)) },
            confirmButton = {
                TextButton(onClick = { navigateToTabByTabId(viewState.createdTabId.value ?: "") }) {
                    Text(stringResource(id = R.string.action_navigate_to_new_tab))
                }
            }
        )
    }

    // error dialog
    var errorDialogSuppressed by remember(submissionStatus.value) { mutableStateOf(false) }
    if (submissionStatus.value is LoadingState.Error && !errorDialogSuppressed) {
        val errorMessage = (submissionStatus.value as LoadingState.Error)
        AlertDialog(
            onDismissRequest = { errorDialogSuppressed = true },
            title = { Text(stringResource(id = R.string.message_tab_creation_failed_title)) },
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
                            value = viewState.selectedSongName.observeAsState(" ").value,
                            onValueChange = {},
                            label = { Text(stringResource(id = R.string.label_create_tab_song_name)) },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewState.selectedArtistName.observeAsState(" ").value,
                            onValueChange = { },
                            label = { Text(stringResource(id = R.string.label_create_tab_artist_name)) },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewState.versionDescription.observeAsState("").value,
                            onValueChange = versionDescriptionUpdated,
                            label = { Text(stringResource(id = R.string.label_create_tab_version_description)) },
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
                                label = { Text(stringResource(id = R.string.label_create_tab_difficulty)) },
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
                                label = { Text(stringResource(id = R.string.label_create_tab_tuning)) },
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
                            label = { Text(stringResource(id = R.string.label_create_tab_capo)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )


                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.generic_action_next))
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
                            label = { Text(stringResource(id = R.string.label_create_tab_content)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = chordToInsert,
                                onValueChange = { chordToInsert = it },
                                label = { Text(stringResource(id = R.string.label_create_tab_chord)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                            )
                            Button(onClick = {
                                insertChord(chordToInsert)
                                chordToInsert = ""
                            }) {
                                Text(stringResource(id = R.string.action_insert_chord))
                            }
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                scope.launch { pagerState.animateScrollToPage(2) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.generic_action_preview))
                        }
                    }
                }
                2 -> { // Preview page
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        val content by viewState.annotatedContent.observeAsState(AnnotatedString(""))
                        val scrollState = rememberScrollState()
                        TabText(
                            text = content,
                            fontSizeSp = 14f,
                            onChordClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scrollState)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(id = R.string.generic_action_back))
                            }
                            Button(
                                onClick = saveTab,
                                modifier = Modifier.weight(1f),
                                enabled = viewState.dataValidated.observeAsState(false).value && (submissionStatus.value is LoadingState.NotStarted || submissionStatus.value is LoadingState.Error)
                            ) {
                                Text(stringResource(id = R.string.action_create_tab))
                            }
                        }
                    }
                }
            }
        }
    }
}
