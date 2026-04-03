package com.angellight.magicgame.spell;

import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the raw spell-chain model used by the first registry-driven prototype.
 */
class SpellChainTest {
    @Test
    void emptyChainsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SpellChain(java.util.List.of()));
    }

    @Test
    void categoryCountingReflectsPrototypeSkeletons() {
        SpellChain ward = CoreGlyphRegistry.chain(
                "anchor",
                "boundary",
                "field",
                "perception",
                "on_entry",
                "attune",
                "caster",
                "bind"
        );

        assertEquals(1L, ward.countByCategory(GlyphCategory.PRINCIPLE));
        assertEquals(3L, ward.countByCategory(GlyphCategory.OPERATION));
        assertEquals(3L, ward.countByCategory(GlyphCategory.CONSTRAINT));
        assertEquals(1L, ward.countByCategory(GlyphCategory.REFERENCE));
        assertTrue(ward.isPrototypeCastable());
    }
}
