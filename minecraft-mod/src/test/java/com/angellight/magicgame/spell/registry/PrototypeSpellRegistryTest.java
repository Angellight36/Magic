package com.angellight.magicgame.spell.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Protects the command-driven prototype spell registry from silent regressions.
 */
class PrototypeSpellRegistryTest {
    @Test
    void seededRegistrySizeMatchesExpectations() {
        assertEquals(8, PrototypeSpellRegistry.size());
    }

    @Test
    void unknownSpellIdsFailFast() {
        assertThrows(IllegalArgumentException.class, () -> PrototypeSpellRegistry.require("missing_spell"));
    }
}
