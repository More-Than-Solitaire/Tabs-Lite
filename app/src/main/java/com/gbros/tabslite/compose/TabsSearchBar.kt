package com.gbros.tabslite.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsSearchBar(modifier: Modifier = Modifier) {
    SearchBar(
        query = "",
        onQueryChange = {},
        onSearch = {},
        active = false,
        onActiveChange = {},
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        placeholder = {
            Text(text = "Search TabsLite")
        },
        modifier = modifier
    ) {
    }
}

@Composable @Preview
fun TabsSearchBarPreview() {
    AppTheme {
        TabsSearchBar()
    }
}