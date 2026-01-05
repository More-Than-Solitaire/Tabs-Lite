package com.gbros.tabslite.data.playlist

import kotlinx.serialization.Serializable

/**
 * A playlist entry with enough information to reference the tab, but no ordering information. Used for data import and export.
 */
@Serializable
open class IPlaylistEntry(open var tabId: String, open val transpose: Int)
