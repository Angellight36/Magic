package com.anthony.magicgame.mana;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers basic mana spending and regeneration rules for the prototype resource loop.
 */
class ManaProfileTest {
    @Test
    void spendingManaFailsWhenInsufficient() {
        ManaProfile mana = new ManaProfile(5, 10, 2);

        assertFalse(mana.trySpend(6));
        assertEquals(5, mana.currentMana());
        assertTrue(mana.trySpend(4));
        assertEquals(1, mana.currentMana());
    }

    @Test
    void regenerationClampsAtMaximum() {
        ManaProfile mana = new ManaProfile(9, 10, 3);

        mana.regenerate();

        assertEquals(10, mana.currentMana());
    }
}