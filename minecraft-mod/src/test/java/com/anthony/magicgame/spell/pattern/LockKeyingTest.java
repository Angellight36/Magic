package com.anthony.magicgame.spell.pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the shared matching and presentation rules for keyed locks.
 */
class LockKeyingTest {
    @Test
    void matchingRequiresExactStoredSignature() {
        assertTrue(LockKeying.matches("alpha-1234", "alpha-1234"));
        assertFalse(LockKeying.matches("alpha-1234", "beta-9999"));
        assertFalse(LockKeying.matches(null, "alpha-1234"));
    }

    @Test
    void displaySignatureShortensForReadableFeedback() {
        assertEquals("UNBOUND", LockKeying.displaySignature(null));
        assertEquals("ABCD1234", LockKeying.displaySignature("abcd1234"));
        assertEquals("12345678", LockKeying.displaySignature("1234567890abcdef"));
    }
}
