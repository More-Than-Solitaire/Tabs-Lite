package com.gbros.tabslite.data

import com.gbros.tabslite.data.chord.Chord
import org.junit.Assert
import org.junit.Test

class ChordTest {
    //region tests for transposeChord

    @Test
    fun testTransposeChord_Up() {
        Assert.assertEquals("Cm", Chord.transposeChord("Abm", 4, false))
        Assert.assertEquals("F", Chord.transposeChord("C", 5, false))
        Assert.assertEquals("A#", Chord.transposeChord("G", 3, false))
        Assert.assertEquals("Bm/D", Chord.transposeChord("Am/C", 2, false))
    }

    @Test
    fun testTransposeChord_Down() {
        Assert.assertEquals("Gm", Chord.transposeChord("Am", -2, false))
        Assert.assertEquals("A", Chord.transposeChord("C", -3, false))
        Assert.assertEquals("G", Chord.transposeChord("G#", -1, false))
        Assert.assertEquals("Cm/D#", Chord.transposeChord("Dm/F", -2, false))
    }

    @Test
    fun testTransposeChord_WithBaseNote() {
        Assert.assertEquals("D/F", Chord.transposeChord("C/Eb", 2, false))
        Assert.assertEquals("F#/A#", Chord.transposeChord("A/C#", -3, false))
        Assert.assertEquals("A/E", Chord.transposeChord("F/C", 4, false))
    }

    @Test
    fun testTransposeChord_WeirdChord() {
        Assert.assertEquals("X", Chord.transposeChord("X", 3, false))
        Assert.assertEquals("X", Chord.transposeChord("X", -5, false))
    }

    @Test
    fun testTransposeChord_Quality() {
        Assert.assertEquals("G#maj7", Chord.transposeChord("Amaj7", -1, false))
        Assert.assertEquals("Dmaj7", Chord.transposeChord("Emaj7", -2, false))
        Assert.assertEquals("AMaj", Chord.transposeChord("BbMaj", -1, false))
    }

    @Test
    fun testTransposeChord_HowMuchGreaterThan12() {
        Assert.assertEquals("C#", Chord.transposeChord("C", 13, false))
        Assert.assertEquals("Cm", Chord.transposeChord("Am", 15, false))
        Assert.assertEquals("F", Chord.transposeChord("C", 17, false))
        Assert.assertEquals("Bmaj7", Chord.transposeChord("Amaj7", 14, false))
    }


    @Test
    fun testTransposeChord_UppercaseConversion() {
        Assert.assertEquals("C#m", Chord.transposeChord("am", 4, false))
        Assert.assertEquals("F", Chord.transposeChord("c", 5, false))
        Assert.assertEquals("A#", Chord.transposeChord("g", 3, false))
        Assert.assertEquals("Bm/D", Chord.transposeChord("am/c", 2, false))
        Assert.assertEquals("Fm", Chord.transposeChord("gm", -2, false))
        Assert.assertEquals("A", Chord.transposeChord("c", -3, false))
        Assert.assertEquals("G", Chord.transposeChord("g#", -1, false))
        Assert.assertEquals("Cm/D#", Chord.transposeChord("dm/f", -2, false))
        Assert.assertEquals("D/F#", Chord.transposeChord("c/e", 2, false))
        Assert.assertEquals("F#/A#", Chord.transposeChord("a/c#", -3, false))
        Assert.assertEquals("A/E", Chord.transposeChord("f/c", 4, false))
        Assert.assertEquals("G#maj7", Chord.transposeChord("amaj7", -1, false))
        Assert.assertEquals("Dmaj7", Chord.transposeChord("emaj7", -2, false))
        Assert.assertEquals("AMaj", Chord.transposeChord("bbMaj", -1, false))
        Assert.assertEquals("C#", Chord.transposeChord("c", 13, false))
        Assert.assertEquals("Cm", Chord.transposeChord("am", 15, false))
        Assert.assertEquals("F", Chord.transposeChord("c", 17, false))
        Assert.assertEquals("Bmaj7", Chord.transposeChord("amaj7", 14, false))
    }

    //endregion
}