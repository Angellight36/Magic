package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the weighted targeting policy used by prototype healing spells.
 */
class SpellTargetingRulesTest {
    @Test
    void directHealingBiasesTowardLookTargetWithoutForcingIt() {
        SpellChain spell = CoreGlyphRegistry.chain("direct", "perception", "life", "life_pattern", "refine", "restore", "strengthen", "stabilize");

        SpellTargetingRules.RestorationTargetWeights weights = SpellTargetingRules.scoreRestorationTargets(spell, true);

        assertTrue(weights.lookTargetWeight() > weights.selfWeight());
    }

    @Test
    void explicitSelfStillHeavilyBiasesTowardSelf() {
        SpellChain spell = CoreGlyphRegistry.chain("self", "life", "life_pattern", "refine", "strengthen", "stabilize");

        SpellTargetingRules.RestorationTargetWeights weights = SpellTargetingRules.scoreRestorationTargets(spell, true);

        assertTrue(weights.selfWeight() > weights.lookTargetWeight());
    }

    @Test
    void weightedRollCanStillSelectEitherCandidateWhenBothExist() {
        SpellTargetingRules.RestorationTargetWeights weights = new SpellTargetingRules.RestorationTargetWeights(2, 5);

        assertEquals(
                SpellTargetingRules.RestorationTargetChoice.LOOK_TARGET,
                SpellTargetingRules.chooseRestorationTarget(weights, 4)
        );
        assertEquals(
                SpellTargetingRules.RestorationTargetChoice.SELF,
                SpellTargetingRules.chooseRestorationTarget(weights, 1)
        );
    }

    @Test
    void transferFromSelfTowardSeenTargetStronglyBiasesTheRecipientOutward() {
        SpellChain spell = CoreGlyphRegistry.chain("self", "life_pattern", "transfer", "life", "refine", "seen_target", "stabilize");

        SpellTargetingRules.RestorationTargetWeights weights = SpellTargetingRules.scoreRestorationTargets(spell, true);

        assertTrue(weights.lookTargetWeight() > weights.selfWeight());
    }

    @Test
    void noLookTargetLeavesOnlySelfAvailable() {
        SpellChain spell = CoreGlyphRegistry.chain("perception", "life", "life_pattern", "refine", "strengthen", "stabilize");

        SpellTargetingRules.RestorationTargetWeights weights = SpellTargetingRules.scoreRestorationTargets(spell, false);

        assertEquals(
                0,
                weights.lookTargetWeight()
        );
        assertTrue(weights.selfWeight() > 0);
    }
}
