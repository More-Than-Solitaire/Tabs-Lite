package com.gbros.tabslite.view.playlists

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gbros.tabslite.R

enum class PlaylistsSortBy {
    Name,
    DateAdded,
    DateModified;


    companion object {
        @Composable
        fun getString(entry: PlaylistsSortBy): String {
            return when(entry) {
                Name -> stringResource(id = R.string.sort_by_title)
                DateAdded -> stringResource(id = R.string.sort_by_date_added)
                DateModified -> stringResource(id = R.string.sort_by_date_modified)
            }
        }
    }
}