package com.gbros.tabslite.data

import android.util.Log
import com.gbros.tabslite.data.tab.ITab
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ITabTest {
    //region setup / teardown

    private lateinit var tabFullForTest: ITab

    @Before fun setup() {
        tabFullForTest = ITabTestClass(1, "Chords", "", 2, 4, 3.7, 12334, "status", 1, "", 1, "D", "version description", 123, "Song Title", "Artist Name", false, 5, false, "", "", arrayListOf(), arrayListOf(), 1, "", "E G A B E", 2, "", arrayListOf(), 0, 0, 0, "contributor", "content", 0 )

        // ignore logging
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    //endregion

    //region tests for getCapoText

    @Test fun getCapoTextZero(){
        tabFullForTest.capo = 0
        Assert.assertEquals("None", tabFullForTest.getCapoText())
    }

    @Test fun getCapoTextOneThroughTen(){
        tabFullForTest.capo = 1
        Assert.assertEquals("1st Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 2
        Assert.assertEquals("2nd Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 3
        Assert.assertEquals("3rd Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 4
        Assert.assertEquals("4th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 5
        Assert.assertEquals("5th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 6
        Assert.assertEquals("6th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 7
        Assert.assertEquals("7th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 8
        Assert.assertEquals("8th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 9
        Assert.assertEquals("9th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 10
        Assert.assertEquals("10th Fret", tabFullForTest.getCapoText())
    }

    @Test fun getCapoTextElevenThroughTwenty(){
        tabFullForTest.capo = 11
        Assert.assertEquals("11th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 12
        Assert.assertEquals("12th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 13
        Assert.assertEquals("13th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 14
        Assert.assertEquals("14th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 15
        Assert.assertEquals("15th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 16
        Assert.assertEquals("16th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 17
        Assert.assertEquals("17th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 18
        Assert.assertEquals("18th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 19
        Assert.assertEquals("19th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 20
        Assert.assertEquals("20th Fret", tabFullForTest.getCapoText())
    }

    @Test fun getCapoTextTwentyOneThroughThirty(){
        tabFullForTest.capo = 21
        Assert.assertEquals("21st Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 22
        Assert.assertEquals("22nd Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 23
        Assert.assertEquals("23rd Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 24
        Assert.assertEquals("24th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 25
        Assert.assertEquals("25th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 26
        Assert.assertEquals("26th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 27
        Assert.assertEquals("27th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 28
        Assert.assertEquals("28th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 29
        Assert.assertEquals("29th Fret", tabFullForTest.getCapoText())

        tabFullForTest.capo = 30
        Assert.assertEquals("30th Fret", tabFullForTest.getCapoText())
    }

    //endregion

    //region tests for transpose

    @Test
    fun testTranspose_TonalityNameAndChords() {
        tabFullForTest.content = "[ch]Am[/ch] [ch]C[/ch] [ch]G[/ch]"
        tabFullForTest.tonalityName = "A"

        // Transpose up
        tabFullForTest.transpose(2)
        assertEquals("[ch]Bm[/ch] [ch]D[/ch] [ch]A[/ch]", tabFullForTest.content)
        assertEquals("B", tabFullForTest.tonalityName)

        // Transpose down
        tabFullForTest.transpose(-3)
        assertEquals("[ch]G#m[/ch] [ch]B[/ch] [ch]F#[/ch]", tabFullForTest.content)
        assertEquals("G#", tabFullForTest.tonalityName)
    }

    @Test
    fun testTranspose_EmptyContent() {
        tabFullForTest.content = ""
        tabFullForTest.tonalityName = "Bb"

        // Transpose has no effect on empty content
        tabFullForTest.transpose(4)
        assertEquals("", tabFullForTest.content)
        assertEquals("D", tabFullForTest.tonalityName)
    }

    @Test
    fun testTranspose_InvalidChordFormat() {
        tabFullForTest.content = "[ch]InvalidChord[/ch] [ch]C[/ch] [ch]G[/ch]"
        tabFullForTest.tonalityName = "F#"

        // Invalid chord format remains unchanged
        tabFullForTest.transpose(-2)
        assertEquals("[ch]InvalidChord[/ch] [ch]A#[/ch] [ch]F[/ch]", tabFullForTest.content)
        assertEquals("E", tabFullForTest.tonalityName)
    }

    @Test
    fun testTranspose_MultiLine() {
        tabFullForTest.content = """
            [Intro]
            [ch]C[/ch] [ch]Em[/ch] [ch]C[/ch] [ch]Em[/ch]
             
            [Verse]
            [tab][ch]C[/ch]                [ch]Em[/ch]
              Hey there Delilah, What’s it like in New York City?[/tab]
            [tab]      [ch]C[/ch]                                      [ch]Em[/ch]                                  [ch]Am[/ch]   [ch]G[/ch]
            I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]
            
            [tab]F                   [ch]G[/ch]                  [ch]Am[/ch]
              Time Square can’t shine as bright as you, [/tab]
            [tab]             [ch]G[/ch]
            I swear it’s true. [/tab]
            [tab][ch]C[/ch]
              Hey there Delilah, [/tab]
            [tab]          [ch]Em[/ch]
            Don’t you worry about the distance, [/tab]
            [tab]          [ch]C[/ch]
            I’m right there if you get lonely, [/tab]
            [tab]          [ch]Em[/ch]
            [ch]G[/ch]ive this song another listen, [/tab]
            [tab]           [ch]Am[/ch]     [ch]G[/ch]
            Close your eyes, [/tab]
            [tab]F              [ch]G[/ch]                [ch]Am[/ch]
              Listen to my voice it’s my disguise, [/tab]
            [tab]            [ch]G[/ch]
            I’m by your side.[/tab]    """.trimIndent()
        tabFullForTest.transpose(13)

        val expectedContent = """
            [Intro]
            [ch]C#[/ch] [ch]Fm[/ch] [ch]C#[/ch] [ch]Fm[/ch]
             
            [Verse]
            [tab][ch]C#[/ch]                [ch]Fm[/ch]
              Hey there Delilah, What’s it like in New York City?[/tab]
            [tab]      [ch]C#[/ch]                                      [ch]Fm[/ch]                                  [ch]A#m[/ch]   [ch]G#[/ch]
            I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]

            [tab]F                   [ch]G#[/ch]                  [ch]A#m[/ch]
              Time Square can’t shine as bright as you, [/tab]
            [tab]             [ch]G#[/ch]
            I swear it’s true. [/tab]
            [tab][ch]C#[/ch]
              Hey there Delilah, [/tab]
            [tab]          [ch]Fm[/ch]
            Don’t you worry about the distance, [/tab]
            [tab]          [ch]C#[/ch]
            I’m right there if you get lonely, [/tab]
            [tab]          [ch]Fm[/ch]
            [ch]G#[/ch]ive this song another listen, [/tab]
            [tab]           [ch]A#m[/ch]     [ch]G#[/ch]
            Close your eyes, [/tab]
            [tab]F              [ch]G#[/ch]                [ch]A#m[/ch]
              Listen to my voice it’s my disguise, [/tab]
            [tab]            [ch]G#[/ch]
            I’m by your side.[/tab]    
        """.trimIndent()
        assertEquals(expectedContent, tabFullForTest.content)
    }

    //endregion

    //region classes for test

    private class ITabTestClass(
        override val tabId: Int,
        override val type: String,
        override val part: String,
        override val version: Int,
        override val votes: Int,
        override val rating: Double,
        override val date: Int,
        override val status: String,
        override val presetId: Int,
        override val tabAccessType: String,
        override val tpVersion: Int,
        override var tonalityName: String,
        override val versionDescription: String,
        override val songId: Int,
        override val songName: String,
        override val artistName: String,
        override val isVerified: Boolean,
        override val numVersions: Int,
        override val recordingIsAcoustic: Boolean,
        override val recordingTonalityName: String,
        override val recordingPerformance: String,
        override val recordingArtists: ArrayList<String>,
        override var recommended: ArrayList<String>,
        override var userRating: Int,
        override var difficulty: String,
        override var tuning: String,
        override var capo: Int,
        override var urlWeb: String,
        override var strumming: ArrayList<String>,
        override var videosCount: Int,
        override var proBrother: Int,
        override var contributorUserId: Int,
        override var contributorUserName: String,
        override var content: String, override val transpose: Int
    ) : ITab {
    }

    //endregion
}