package com.gbros.tabslite.data.playlist

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PlaylistFileExportType(val playlists: List<SelfContainedPlaylist>) {
    constructor(rootElement: JsonElement) : this(mutableListOf()) {
        // Check if the root is an object and contains the 'playlists' array
        if (rootElement !is JsonObject || "playlists" !in rootElement) {
            throw IllegalArgumentException("Invalid JSON: Root element must be an object with a 'playlists' array.")
        }

        val playlistsArray = rootElement["playlists"]?.jsonArray
            ?: throw IllegalArgumentException("Invalid JSON: 'playlists' array not found in the root object.")

        // Transform the playlists array
        val newPlaylistsArray = JsonArray(playlistsArray.map { playlistElement ->
            if (playlistElement is JsonObject && "entries" in playlistElement) {
                val entriesArray = playlistElement["entries"]?.jsonArray
                val newEntriesArray = entriesArray?.let { oldEntries ->
                    JsonArray(oldEntries.map { entryElement ->
                        // 4. Transform each entry
                        if (entryElement is JsonObject && "tabId" in entryElement) {
                            val oldTabId = entryElement["tabId"]?.jsonPrimitive

                            // If tabId is a number, convert it to a String
                            if (oldTabId?.intOrNull != null) {
                                // Create a new entry with the stringified tabId
                                buildJsonObject {
                                    put("tabId", JsonPrimitive(oldTabId.content))
                                    // Copy all other fields from the original entry
                                    entryElement.entries.forEach { (key, value) ->
                                        if (key != "tabId") {
                                            put(key, value)
                                        }
                                    }
                                }
                            } else {
                                entryElement // No change needed
                            }
                        } else {
                            entryElement // Not an entry object, no change
                        }
                    })
                }
                // Rebuild the playlist object with the new entries array
                buildJsonObject {
                    put("entries", newEntriesArray ?: JsonArray(emptyList()))
                    playlistElement.entries.forEach { (key, value) ->
                        if (key != "entries") {
                            put(key, value)
                        }
                    }
                }
            } else {
                playlistElement // Not a playlist object, no change
            }
        })


        val newRootObject = buildJsonObject {
            put("playlists", newPlaylistsArray)
        }

        // deserialize the new playlists array into SelfContainedPlaylist objects
        val newPlaylists = Json.decodeFromJsonElement(
            element = newRootObject,
            deserializer = serializer()
        )

        // add the new playlists array to this.playlists
        (playlists as MutableList<SelfContainedPlaylist>).addAll(newPlaylists.playlists)
    }
}

