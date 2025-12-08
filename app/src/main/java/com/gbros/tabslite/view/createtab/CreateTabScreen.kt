
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.view.tabview.TabText
import com.gbros.tabslite.viewmodel.CreateTabViewModel
import kotlinx.coroutines.launch

const val CREATE_TAB_ROUTE = "createtab"

fun NavController.navigateToCreateTab() {
    navigate(CREATE_TAB_ROUTE)
}

fun NavGraphBuilder.createTabScreen(navController: NavController) {
    composable(CREATE_TAB_ROUTE) {
        val db = AppDatabase.getInstance(LocalContext.current)
        val createTabViewModel: CreateTabViewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<CreateTabViewModel, CreateTabViewModel.CreateTabViewModelFactory> { factory ->
                factory.create(db.dataAccess())
            }

        CreateTabScreen(
            viewModel = createTabViewModel,
            navigateBack = { navController.popBackStack() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateTabScreen(
    viewModel: CreateTabViewModel,
    navigateBack: () -> Unit
) {
    var songName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var tabContent by remember { mutableStateOf(TextFieldValue("")) }
    var versionDescription by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var tuning by remember { mutableStateOf("") }
    var capo by remember { mutableFloatStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var chordToInsert by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        TopAppBar(
            title = { Text(text = "Create new tab") },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(text = { Text("Details") },
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } })
            Tab(text = { Text("Content") },
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } })
            Tab(text = { Text("Preview") },
                selected = pagerState.currentPage == 2,
                onClick = { scope.launch { pagerState.animateScrollToPage(2) } })
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
                            value = songName,
                            onValueChange = { songName = it },
                            label = { Text("Song Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = artistName,
                            onValueChange = { artistName = it },
                            label = { Text("Artist Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = versionDescription,
                            onValueChange = { versionDescription = it },
                            label = { Text("Version Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = { difficulty = it },
                            label = { Text("Difficulty") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = tuning,
                            onValueChange = { tuning = it },
                            label = { Text("Tuning") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(text = "Capo: ${capo.toInt()}")
                        Slider(
                            value = capo,
                            onValueChange = { capo = it },
                            valueRange = 0f..12f,
                            steps = 11
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
                        OutlinedTextField(
                            value = tabContent,
                            onValueChange = { tabContent = it },
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
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                val textToInsert = "{ch:$chordToInsert}"
                                val selection = tabContent.selection
                                val newText = tabContent.text.replaceRange(selection.start, selection.end, textToInsert)
                                val newSelectionStart = selection.start + textToInsert.length
                                tabContent = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(newSelectionStart))
                            }) {
                                Text("Insert")
                            }
                        }

                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
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
                        TabText(
                            text = parseTabToAnnotatedString(tabContent.text),
                            fontSizeSp = 14f,
                            onZoom = {},
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
                                onClick = {
                                    // todo: save tab
                                 },
                                modifier = Modifier.weight(1f),
                                enabled = false //todo: enable based on contents of fields
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

@Preview
@Composable
private fun CreateTabScreenPreview() {
    // Create a mock view model for preview
    // val mockViewModel = CreateTabViewModel( ... )
    // CreateTabScreen(viewModel = mockViewModel, navigateBack = {})
}
