package com.angellight.magicgame.spell;

import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers the lightweight manipulation semantics for pattern interaction chains.
 */
class PatternInteractionRulesTest {
    @Test
    void bindingFocusedLockingPatternDefaultsToLocking() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "order", "binding", "locking_pattern");

        assertEquals(PatternInteractionRules.PatternInteractionMode.LOCK, PatternInteractionRules.classifyMode(spell));
    }

    @Test
    void unlockStyleChainClassifiesAsUnlock() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "order", "binding", "locking_pattern", "separate", "gentle");

        assertEquals(PatternInteractionRules.PatternInteractionMode.UNLOCK, PatternInteractionRules.classifyMode(spell));
    }

    @Test
    void disruptivePatternChainClassifiesAsDisrupt() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "binding", "locking_pattern", "disrupt");

        assertEquals(PatternInteractionRules.PatternInteractionMode.DISRUPT, PatternInteractionRules.classifyMode(spell));
    }
}
