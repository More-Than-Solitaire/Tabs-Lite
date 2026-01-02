package com.gbros.tabslite.data.tab

enum class SongGenre {
    Other,
    Blues,
    Classical,
    Comedy,
    Country,
    Darkwave,
    Disco,
    Electronic,
    Experimental,
    Folk,
    HipHop,
    Jazz,
    Metal,
    NewAge,
    Pop,
    RAndBFunkAndSoul,
    ReggaeAndSka,
    ReligiousMusic,
    Rock,
    Soundtrack,
    WorldMusic;

    override fun toString(): String {
        return when (this) {
            Other -> ""
            Blues -> "blues"
            Classical -> "classical"
            Comedy -> "comedy"
            Country -> "country"
            Darkwave -> "darkwave"
            Disco -> "disco"
            Electronic -> "electronic"
            Experimental -> "experimental"
            Folk -> "folk"
            HipHop -> "hip hop"
            Jazz -> "jazz"
            Metal -> "metal"
            NewAge -> "new age"
            Pop -> "pop"
            RAndBFunkAndSoul -> "r&b funk & soul"
            ReggaeAndSka -> "reggae & ska"
            ReligiousMusic -> "religious music"
            Rock -> "rock"
            Soundtrack -> "soundtrack"
            WorldMusic -> "world music"
        }
    }
}