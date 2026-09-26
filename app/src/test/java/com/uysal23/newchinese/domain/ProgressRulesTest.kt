package com.uysal23.newchinese.domain

import org.junit.Assert.*
import org.junit.Test

class ProgressRulesTest {
    @Test fun wordExamRequiresNinetyPercent() {
        assertFalse(ProgressRules.wordPassed(89))
        assertTrue(ProgressRules.wordPassed(90))
        assertTrue(ProgressRules.wordPassed(100))
    }

    @Test fun sentenceExamRequiresWordPassAndEightyFivePercent() {
        assertFalse(ProgressRules.sentencePassed(false, 100))
        assertFalse(ProgressRules.sentencePassed(true, 84))
        assertTrue(ProgressRules.sentencePassed(true, 85))
    }

    @Test fun nextSceneWithinLevel() {
        assertEquals("HSK1_SC002", ProgressRules.nextSceneId("HSK1_SC001"))
        assertEquals("HSK4_SC050", ProgressRules.nextSceneId("HSK4_SC049"))
    }

    @Test fun levelBoundaryUnlocksNextLevel() {
        assertEquals("HSK2_SC001", ProgressRules.nextSceneId("HSK1_SC050"))
        assertEquals("HSK6_SC001", ProgressRules.nextSceneId("HSK5_SC050"))
    }

    @Test fun finalSceneHasNoNextScene() {
        assertNull(ProgressRules.nextSceneId("HSK6_SC050"))
    }
}
