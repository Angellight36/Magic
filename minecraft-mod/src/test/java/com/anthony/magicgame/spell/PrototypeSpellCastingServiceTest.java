package com.anthony.magicgame.spell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers small but important prototype casting rules that the command layer delegates to shared helpers.
 */
class PrototypeSpellCastingServiceTest {
    @Test
    void onlyAlertWardIsCurrentlyAnchorable() {
        assertTrue(PrototypeSpellCastingService.isAnchorablePrototype("alert_ward"));
        assertFalse(PrototypeSpellCastingService.isAnchorablePrototype("fireball"));
    }
}
