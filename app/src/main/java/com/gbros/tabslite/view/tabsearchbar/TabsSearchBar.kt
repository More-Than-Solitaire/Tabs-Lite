package com.gbros.tabslite.view.tabsearchbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsSearchBar(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = { Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )},
    viewState: ITabSearchBarViewState,
    onQueryChange: (newQuery: String) -> Unit,
    onSearch: (query: String) -> Unit,
    onNavigateToTabById: (tabId: Int) -> Unit
) {
    val query = viewState.query.observeAsState("")
    var active by remember { mutableStateOf(false) }
    val lazyColumnState = rememberLazyListState()
    val searchSuggestions = viewState.searchSuggestions.observeAsState(listOf())
    val suggestedTabs = viewState.tabSuggestions.observeAsState(listOf())

    val onActiveChange = {expanded: Boolean -> active = expanded}
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query.value,
                onQueryChange = onQueryChange,
                onSearch = {q -> if(q.isNotBlank()) {onSearch(q)}},
                expanded = active,
                onExpandedChange = onActiveChange,
                enabled = true,
                placeholder = {
                    Text(text = stringResource(id = R.string.app_action_search))
                },
                leadingIcon = leadingIcon,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.app_action_description_search)
                    )
                }
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        modifier = modifier,
        windowInsets = SearchBarDefaults.windowInsets,
        content = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), state = lazyColumnState) {
                if (query.value.isNotBlank()) {
                    items(items = suggestedTabs.value) { suggestedTab ->
                        SuggestedTab(
                            modifier = Modifier
                                .fillMaxWidth(),
                            tab = suggestedTab,
                            onClick = onNavigateToTabById
                        )
                    }
                    items(items = searchSuggestions.value) { searchSuggestion ->
                        SearchSuggestion(
                            suggestionText = searchSuggestion,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (searchSuggestion.isNotBlank())
                                onSearch(searchSuggestion)
                        }
                    }
                }
            }
        },
    )
}

@Composable @Preview
fun TabsSearchBarPreview() {
    class TabSearchBarViewStateForTest(
        override val query: LiveData<String>,
        override val searchSuggestions: LiveData<List<String>>,
        override val tabSuggestions: LiveData<List<ITab>>,
    ) : ITabSearchBarViewState

    AppTheme {
        TabsSearchBar(
            viewState = TabSearchBarViewStateForTest(
                query = MutableLiveData("Test query"),
                searchSuggestions = MutableLiveData(listOf("suggestion1", "suggestion 2")),
                tabSuggestions = MutableLiveData(listOf(Tab(0)))
            ),
            leadingIcon = { Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )},
            onQueryChange = {},
            onSearch = {},
            onNavigateToTabById = {}
        )
    }
}