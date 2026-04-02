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
        assertTrue(interpreted.traits().contains(SpellTrait.TRAVELING_DELIVERY));
    }

    @Test
    void alertWardResolvesTowardPatternDomain() {
        SpellResolutionPlan plan = SpellResolver.resolve(PrototypeSpellRegistry.require("alert_ward"));

        assertEquals(SpellIntent.BOUNDARY_WARD, plan.intent());
        assertEquals(MagicDomain.PATTERN, plan.primaryDomain());
        assertTrue(plan.interpretedSpell().traits().contains(SpellTrait.ANCHORED));
    }

    @Test
    void healingTouchClassifiesAsRestorationInsteadOfGenericLifeMagic() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "life", "life_pattern", "refine", "strengthen", "stabilize");

        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);

        assertEquals(SpellIntent.RESTORATION_EFFECT, interpreted.intent());
        assertTrue(interpreted.traits().contains(SpellTrait.RESTORATIVE));
        assertTrue(interpreted.intentScore(SpellIntent.RESTORATION_EFFECT) > interpreted.intentScore(SpellIntent.VITALITY_TRANSFER));
        assertTrue(interpreted.sourceScore(SpellSource.CASTER_MANA) > interpreted.sourceScore(SpellSource.SELF_HEALTH));
    }

    @Test
    void sacrificialHealingPrefersVitalityTransferOverPlainRestoration() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "self", "life_pattern", "transfer", "life", "refine", "seen_target", "stabilize");

        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);

        assertEquals(SpellIntent.VITALITY_TRANSFER, interpreted.intent());
        assertTrue(interpreted.traits().contains(SpellTrait.VITALITY_TRANSFER));
        assertTrue(interpreted.traits().contains(SpellTrait.SELF_REFERENCE));
        assertTrue(interpreted.traits().contains(SpellTrait.TARGETED_REFERENCE));
        assertTrue(interpreted.sourceScore(SpellSource.SELF_HEALTH) > interpreted.sourceScore(SpellSource.CASTER_MANA));
        assertTrue(interpreted.recipientScore(SpellRecipient.LOOK_TARGET) > interpreted.recipientScore(SpellRecipient.SELF));
    }

    @Test
    void unlockChainStaysPatternFocused() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "order", "binding", "locking_pattern", "separate", "gentle");

        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);

        assertEquals(SpellIntent.PATTERN_INTERACTION, interpreted.intent());
        assertTrue(interpreted.traits().contains(SpellTrait.PATTERN_SENSITIVE));
    }
}
