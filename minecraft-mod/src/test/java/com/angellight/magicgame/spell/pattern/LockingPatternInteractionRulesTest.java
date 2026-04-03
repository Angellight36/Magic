package com.angellight.magicgame.spell.pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the shared local allow/deny rules for locked-block interaction prediction.
 */
class LockingPatternInteractionRulesTest {
    @Test
    void unkeyedLocksAlwaysBlockManualInteraction() {
        assertTrue(LockingPatternInteractionRules.shouldBlockClientInteraction(null, null));
        assertTrue(LockingPatternInteractionRules.shouldBlockClientInteraction(null, "alpha-1234"));
    }

    @Test
    void keyedLocksBlockMismatchedKeys() {
        assertTrue(LockingPatternInteractionRules.shouldBlockClientInteraction("alpha-1234", null));
        assertTrue(LockingPatternInteractionRules.shouldBlockClientInteraction("alpha-1234", "beta-9999"));
    }

    @Test
    void keyedLocksAllowMatchingKeys() {
        assertFalse(LockingPatternInteractionRules.shouldBlockClientInteraction("alpha-1234", "alpha-1234"));
    }
}
