package com.angellight.magicgame.spell;

import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Covers first-pass failure profiling on top of the weighted resolver.
 */
class SpellResolverTest {
    @Test
    void understructuredPatternChainReportsStructuralFailureRisk() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "order", "binding", "locking_pattern");

        SpellResolutionPlan plan = SpellResolver.resolve(spell);

        assertNotNull(plan.likelyFailureProfile());
        assertEquals(SpellFailureType.STRUCTURAL_FAILURE, plan.likelyFailureProfile().failureType());
    }

    @Test
    void ordinaryHealingKeepsAResolvableFailureProfileShape() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "life", "life_pattern", "refine", "strengthen", "stabilize");

        SpellResolutionPlan plan = SpellResolver.resolve(spell);

        assertEquals(SpellIntent.RESTORATION_EFFECT, plan.intent());
    }
}
