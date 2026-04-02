package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers structural source and recipient scoring for prototype spell flow interpretation.
 */
class SpellFlowRulesTest {
    @Test
    void healingTouchLeansTowardManaBackedRestoration() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "life", "life_pattern", "refine", "strengthen", "stabilize");

        Map<SpellSource, Integer> sourceScores = SpellFlowRules.scoreSources(spell);

        assertTrue(sourceScores.get(SpellSource.CASTER_MANA) > sourceScores.get(SpellSource.SELF_HEALTH));
    }

    @Test
    void vitalityTransferLeansTowardSelfHealthAndLookTarget() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "self", "life_pattern", "transfer", "life", "refine", "seen_target", "stabilize");

        Map<SpellSource, Integer> sourceScores = SpellFlowRules.scoreSources(spell);
        Map<SpellRecipient, Integer> recipientScores = SpellFlowRules.scoreRecipients(spell);

        assertTrue(sourceScores.get(SpellSource.SELF_HEALTH) > sourceScores.get(SpellSource.CASTER_MANA));
        assertTrue(recipientScores.get(SpellRecipient.LOOK_TARGET) > recipientScores.get(SpellRecipient.SELF));
    }

    @Test
    void anchoredWardLeansTowardAnchoredPointRecipient() {
        SpellChain spell = CoreGlyphRegistry.chain("anchor", "boundary", "field", "persist", "perception", "on_entry", "attune", "caster", "bind");

        Map<SpellRecipient, Integer> recipientScores = SpellFlowRules.scoreRecipients(spell);

        assertTrue(recipientScores.get(SpellRecipient.ANCHORED_POINT) > recipientScores.get(SpellRecipient.SELF));
    }
}
