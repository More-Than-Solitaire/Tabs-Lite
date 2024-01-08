package com.gbros.tabslite.data

import com.gbros.tabslite.data.chord.ICompleteChord
import org.junit.Assert
import org.junit.Test

class ICompleteChordTest {
    //region tests for transposeChord

    @Test
    fun testTransposeChord_Up() {
        Assert.assertEquals("Cm", ICompleteChord.transposeChord("Abm", 4))
        Assert.assertEquals("F", ICompleteChord.transposeChord("C", 5))
        Assert.assertEquals("A#", ICompleteChord.transposeChord("G", 3))
        Assert.assertEquals("Bm/D", ICompleteChord.transposeChord("Am/C", 2))
    }

    @Test
    fun testTransposeChord_Down() {
        Assert.assertEquals("Gm", ICompleteChord.transposeChord("Am", -2))
        Assert.assertEquals("A", ICompleteChord.transposeChord("C", -3))
        Assert.assertEquals("G", ICompleteChord.transposeChord("G#", -1))
        Assert.assertEquals("Cm/D#", ICompleteChord.transposeChord("Dm/F", -2))
    }

    @Test
    fun testTransposeChord_WithBaseNote() {
        Assert.assertEquals("D/F", ICompleteChord.transposeChord("C/Eb", 2))
        Assert.assertEquals("F#/A#", ICompleteChord.transposeChord("A/C#", -3))
        Assert.assertEquals("A/E", ICompleteChord.transposeChord("F/C", 4))
    }

    @Test
    fun testTransposeChord_WeirdChord() {
        Assert.assertEquals("X", ICompleteChord.transposeChord("X", 3))
        Assert.assertEquals("X", ICompleteChord.transposeChord("X", -5))
    }

    @Test
    fun testTransposeChord_Quality() {
        Assert.assertEquals("G#maj7", ICompleteChord.transposeChord("Amaj7", -1))
        Assert.assertEquals("Dmaj7", ICompleteChord.transposeChord("Emaj7", -2))
        Assert.assertEquals("AMaj", ICompleteChord.transposeChord("BbMaj", -1))
    }

    @Test
    fun testTransposeChord_HowMuchGreaterThan12() {
        Assert.assertEquals("C#", ICompleteChord.transposeChord("C", 13))
        Assert.assertEquals("Cm", ICompleteChord.transposeChord("Am", 15))
        Assert.assertEquals("F", ICompleteChord.transposeChord("C", 17))
        Assert.assertEquals("Bmaj7", ICompleteChord.transposeChord("Amaj7", 14))
    }


    @Test
    fun testTransposeChord_UppercaseConversion() {
        Assert.assertEquals("C#m", ICompleteChord.transposeChord("am", 4))
        Assert.assertEquals("F", ICompleteChord.transposeChord("c", 5))
        Assert.assertEquals("A#", ICompleteChord.transposeChord("g", 3))
        Assert.assertEquals("Bm/D", ICompleteChord.transposeChord("am/c", 2))
        Assert.assertEquals("Fm", ICompleteChord.transposeChord("gm", -2))
        Assert.assertEquals("A", ICompleteChord.transposeChord("c", -3))
        Assert.assertEquals("G", ICompleteChord.transposeChord("g#", -1))
        Assert.assertEquals("Cm/D#", ICompleteChord.transposeChord("dm/f", -2))
        Assert.assertEquals("D/F#", ICompleteChord.transposeChord("c/e", 2))
        Assert.assertEquals("F#/A#", ICompleteChord.transposeChord("a/c#", -3))
        Assert.assertEquals("A/E", ICompleteChord.transposeChord("f/c", 4))
        Assert.assertEquals("G#maj7", ICompleteChord.transposeChord("amaj7", -1))
        Assert.assertEquals("Dmaj7", ICompleteChord.transposeChord("emaj7", -2))
        Assert.assertEquals("AMaj", ICompleteChord.transposeChord("bbMaj", -1))
        Assert.assertEquals("C#", ICompleteChord.transposeChord("c", 13))
        Assert.assertEquals("Cm", ICompleteChord.transposeChord("am", 15))
        Assert.assertEquals("F", ICompleteChord.transposeChord("c", 17))
        Assert.assertEquals("Bmaj7", ICompleteChord.transposeChord("amaj7", 14))
    }

    //endregion
}