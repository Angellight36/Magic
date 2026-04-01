package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import com.anthony.magicgame.spell.registry.PrototypeSpellRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the prototype interpreter maps the seeded spells into the intended buckets.
 */
class SpellInterpreterTest {
    @Test
    void fireballClassifiesAsTravelingEffect() {
        SpellChain spell = CoreGlyphRegistry.chain("gather", "fire", "shape", "separate", "forward", "direct", "stabilize", "on_impact", "release");

        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);

        assertEquals(SpellIntent.TRAVELING_EFFECT, interpreted.intent());
        assertTrue(interpreted.domainCandidates().contains(MagicDomain.DAMAGE));
    }

    @Test
    void alertWardResolvesTowardPatternDomain() {
        SpellResolutionPlan plan = SpellResolver.resolve(PrototypeSpellRegistry.require("alert_ward"));

        assertEquals(SpellIntent.BOUNDARY_WARD, plan.intent());
        assertEquals(MagicDomain.PATTERN, plan.primaryDomain());
    }
}