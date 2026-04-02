package com.anthony.magicgame.spell;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared source and recipient scoring rules for the current prototype interpreter/runtime.
 *
 * <p>The goal is not to produce a single hardcoded answer. Instead, these rules score multiple
 * plausible flows so interpretation, analysis, and runtime resolution can all reason from the
 * same competing evidence.</p>
 */
public final class SpellFlowRules {
    private SpellFlowRules() {
    }

    /**
     * Scores structural recipient candidates implied by the glyph chain.
     *
     * @param spell source spell chain
     * @return candidate recipient weights
     */
    public static Map<SpellRecipient, Integer> scoreRecipients(SpellChain spell) {
        EnumMap<SpellRecipient, Integer> scores = emptyRecipientScores();

        add(scores, SpellRecipient.SELF, 2);
        if (hasGlyph(spell, "self") || hasGlyph(spell, "caster")) {
            add(scores, SpellRecipient.SELF, 8);
        }
        if (hasGlyph(spell, "seen_target")) {
            add(scores, SpellRecipient.LOOK_TARGET, 8);
        }
        if (hasGlyph(spell, "direct")) {
            add(scores, SpellRecipient.LOOK_TARGET, 4);
        }
        if (hasGlyph(spell, "forward")) {
            add(scores, SpellRecipient.LOOK_TARGET, 3);
        }
        if (hasGlyph(spell, "perception")) {
            add(scores, SpellRecipient.LOOK_TARGET, 2);
        }
        if (hasGlyph(spell, "life_pattern") || hasGlyph(spell, "locking_pattern")) {
            add(scores, SpellRecipient.LOOK_TARGET, 1);
        }
        if (hasGlyph(spell, "transfer")) {
            add(scores, SpellRecipient.LOOK_TARGET, 2);
        }
        if (hasGlyph(spell, "chosen_point")) {
            add(scores, SpellRecipient.CHOSEN_POINT, 8);
        }
        if (hasGlyph(spell, "anchor") || hasGlyph(spell, "boundary") || hasGlyph(spell, "field")) {
            add(scores, SpellRecipient.ANCHORED_POINT, 3);
        }
        if (hasGlyph(spell, "anchor") && hasGlyph(spell, "boundary") && hasGlyph(spell, "field")) {
            add(scores, SpellRecipient.ANCHORED_POINT, 8);
        }

        return Map.copyOf(scores);
    }

    /**
     * Scores likely spell resource sources implied by the glyph chain.
     *
     * @param spell source spell chain
     * @return candidate source weights
     */
    public static Map<SpellSource, Integer> scoreSources(SpellChain spell) {
        EnumMap<SpellSource, Integer> scores = emptySourceScores();

        add(scores, SpellSource.CASTER_MANA, 3);
        add(scores, SpellSource.AMBIENT_MANA, 1);

        if (hasGlyph(spell, "gather")) {
            add(scores, SpellSource.CASTER_MANA, 2);
            add(scores, SpellSource.AMBIENT_MANA, 2);
        }
        if (hasGlyph(spell, "restore")) {
            add(scores, SpellSource.CASTER_MANA, 2);
        }
        if (hasGlyph(spell, "refine") || hasGlyph(spell, "strengthen") || hasGlyph(spell, "stabilize")) {
            add(scores, SpellSource.CASTER_MANA, 1);
        }
        if (hasGlyph(spell, "transfer")) {
            add(scores, SpellSource.SELF_HEALTH, 3);
            add(scores, SpellSource.TARGET_VITALITY, 2);
        }
        if (hasGlyph(spell, "life")) {
            add(scores, SpellSource.CASTER_MANA, 1);
            add(scores, SpellSource.TARGET_VITALITY, 1);
        }
        if (hasGlyph(spell, "life_pattern")) {
            add(scores, SpellSource.CASTER_MANA, 1);
            add(scores, SpellSource.TARGET_VITALITY, 1);
        }
        if (hasGlyph(spell, "self") || hasGlyph(spell, "caster")) {
            add(scores, SpellSource.SELF_HEALTH, 5);
        }
        if (hasGlyph(spell, "seen_target")) {
            add(scores, SpellSource.TARGET_VITALITY, 4);
        }
        if (hasGlyph(spell, "transfer") && hasGlyph(spell, "self")) {
            add(scores, SpellSource.SELF_HEALTH, 4);
        }

        return Map.copyOf(scores);
    }

    /**
     * Adapts structural recipient scores to the current restoration runtime context.
     *
     * @param structuralScores recipient scores inferred from the glyph chain
     * @param hasLookTarget whether a valid looked-at living target exists right now
     * @return runtime-usable self/look-target weights
     */
    public static SpellTargetingRules.RestorationTargetWeights restorationTargetWeights(
            Map<SpellRecipient, Integer> structuralScores,
            boolean hasLookTarget
    ) {
        int selfWeight = structuralScores.getOrDefault(SpellRecipient.SELF, 0);
        int lookTargetWeight = hasLookTarget ? structuralScores.getOrDefault(SpellRecipient.LOOK_TARGET, 0) : 0;
        return new SpellTargetingRules.RestorationTargetWeights(selfWeight, lookTargetWeight);
    }

    private static EnumMap<SpellRecipient, Integer> emptyRecipientScores() {
        EnumMap<SpellRecipient, Integer> scores = new EnumMap<>(SpellRecipient.class);
        for (SpellRecipient recipient : SpellRecipient.values()) {
            scores.put(recipient, 0);
        }
        return scores;
    }

    private static EnumMap<SpellSource, Integer> emptySourceScores() {
        EnumMap<SpellSource, Integer> scores = new EnumMap<>(SpellSource.class);
        for (SpellSource source : SpellSource.values()) {
            scores.put(source, 0);
        }
        return scores;
    }

    private static <T extends Enum<T>> void add(Map<T, Integer> scores, T key, int amount) {
        scores.merge(key, amount, Integer::sum);
    }

    private static boolean hasGlyph(SpellChain spell, String glyphId) {
        return spell.glyphs().stream().anyMatch(glyph -> glyph.id().equals(glyphId));
    }
}
