package com.gbros.tabslite.view.createtab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.TabSearchBarViewModel

const val CREATE_TAB_ROUTE = "createtab"

fun NavController.navigateToCreateTab() {
    navigate(CREATE_TAB_ROUTE)
}

fun NavGraphBuilder.createTabScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSongSelection: (query: String) -> Unit
) {
    composable(CREATE_TAB_ROUTE) {
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel = TabSearchBarViewModel(dataAccess = db.dataAccess())

        CreateTabScreen(
            navigateBack = onNavigateBack,
            searchBarViewState = viewModel,
            onQueryChange = viewModel::onQueryChange,
            onNavigateToSongSelection = onNavigateToSongSelection
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTabScreen(
    navigateBack: () -> Unit,
    searchBarViewState: ITabSearchBarViewState,
    onQueryChange: (newQuery: String) -> Unit,
    onNavigateToSongSelection: (query: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Tab") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Flow/Stepper UI
            Step(
                stepNumber = "1",
                title = "Select a song",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                isActive = true
            ) {
                // Content for Step 1
                Text("Note: to start with pre-filled tab content for an existing song, navigate to that song and select Edit from the options menu.")
                Spacer(Modifier.height(16.dp))
                TabsSearchBar(
                    leadingIcon = {},
                    viewState = searchBarViewState,
                    onQueryChange = onQueryChange,
                    onSearch = onNavigateToSongSelection
                )
            }

            Spacer(Modifier.height(16.dp))
            Step(
                stepNumber = "2",
                title = "Write the song",
                icon = Icons.Default.Edit,
                isActive = false
            ) {}
            Spacer(Modifier.height(16.dp))
            Step(
                stepNumber = "3",
                title = "Confirm and submit",
                icon = Icons.Default.Check,
                isActive = false
            ) {}
        }
    }
}

@Composable
private fun Step(
    stepNumber: String,
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    content: @Composable () -> Unit
) {
    val cardColors = if (isActive) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
        colors = cardColors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title)
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Step $stepNumber: $title",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (isActive) {
                Spacer(Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTabScreenPreview() {
    AppTheme {
        CreateTabScreen(
            navigateBack = {},
            searchBarViewState = TabSearchBarViewModel(dataAccess = AppDatabase.getInstance(LocalContext.current).dataAccess()),
            onQueryChange = {},
            onNavigateToSongSelection = {}
        )
    }
}