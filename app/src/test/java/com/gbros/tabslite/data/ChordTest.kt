package com.gbros.tabslite.data

import com.gbros.tabslite.data.chord.Chord
import org.junit.Assert
import org.junit.Test

class ChordTest {
    //region tests for transposeChord

    @Test
    fun testTransposeChord_Up() {
        Assert.assertEquals("Cm", Chord.transposeChord("Abm", 4))
        Assert.assertEquals("F", Chord.transposeChord("C", 5))
        Assert.assertEquals("A#", Chord.transposeChord("G", 3))
        Assert.assertEquals("Bm/D", Chord.transposeChord("Am/C", 2))
    }

    @Test
    fun testTransposeChord_Down() {
        Assert.assertEquals("Gm", Chord.transposeChord("Am", -2))
        Assert.assertEquals("A", Chord.transposeChord("C", -3))
        Assert.assertEquals("G", Chord.transposeChord("G#", -1))
        Assert.assertEquals("Cm/D#", Chord.transposeChord("Dm/F", -2))
    }

    @Test
    fun testTransposeChord_WithBaseNote() {
        Assert.assertEquals("D/F", Chord.transposeChord("C/Eb", 2))
        Assert.assertEquals("F#/A#", Chord.transposeChord("A/C#", -3))
        Assert.assertEquals("A/E", Chord.transposeChord("F/C", 4))
    }

    @Test
    fun testTransposeChord_WeirdChord() {
        Assert.assertEquals("X", Chord.transposeChord("X", 3))
        Assert.assertEquals("X", Chord.transposeChord("X", -5))
    }

    @Test
    fun testTransposeChord_Quality() {
        Assert.assertEquals("G#maj7", Chord.transposeChord("Amaj7", -1))
        Assert.assertEquals("Dmaj7", Chord.transposeChord("Emaj7", -2))
        Assert.assertEquals("AMaj", Chord.transposeChord("BbMaj", -1))
    }

    @Test
    fun testTransposeChord_HowMuchGreaterThan12() {
        Assert.assertEquals("C#", Chord.transposeChord("C", 13))
        Assert.assertEquals("Cm", Chord.transposeChord("Am", 15))
        Assert.assertEquals("F", Chord.transposeChord("C", 17))
        Assert.assertEquals("Bmaj7", Chord.transposeChord("Amaj7", 14))
    }


    @Test
    fun testTransposeChord_UppercaseConversion() {
        Assert.assertEquals("C#m", Chord.transposeChord("am", 4))
        Assert.assertEquals("F", Chord.transposeChord("c", 5))
        Assert.assertEquals("A#", Chord.transposeChord("g", 3))
        Assert.assertEquals("Bm/D", Chord.transposeChord("am/c", 2))
        Assert.assertEquals("Fm", Chord.transposeChord("gm", -2))
        Assert.assertEquals("A", Chord.transposeChord("c", -3))
        Assert.assertEquals("G", Chord.transposeChord("g#", -1))
        Assert.assertEquals("Cm/D#", Chord.transposeChord("dm/f", -2))
        Assert.assertEquals("D/F#", Chord.transposeChord("c/e", 2))
        Assert.assertEquals("F#/A#", Chord.transposeChord("a/c#", -3))
        Assert.assertEquals("A/E", Chord.transposeChord("f/c", 4))
        Assert.assertEquals("G#maj7", Chord.transposeChord("amaj7", -1))
        Assert.assertEquals("Dmaj7", Chord.transposeChord("emaj7", -2))
        Assert.assertEquals("AMaj", Chord.transposeChord("bbMaj", -1))
        Assert.assertEquals("C#", Chord.transposeChord("c", 13))
        Assert.assertEquals("Cm", Chord.transposeChord("am", 15))
        Assert.assertEquals("F", Chord.transposeChord("c", 17))
        Assert.assertEquals("Bmaj7", Chord.transposeChord("amaj7", 14))
    }

    //endregion
}