package com.anthony.magicgame.spell;

/**
 * Helper rules for deciding whether a spell chain can actively manipulate an existing pattern.
 */
public final class PatternInteractionRules {
    private PatternInteractionRules() {
    }

    /**
     * Returns whether the chain contains an operation that can actually alter a target pattern.
     *
     * @param spell source spell chain
     * @return true when the chain contains a meaningful manipulation operation
     */
    public static PatternInteractionMode classifyMode(SpellChain spell) {
        boolean hasLockingPattern = hasGlyph(spell, "locking_pattern");
        boolean hasBinding = hasGlyph(spell, "binding");
        boolean hasUnlockOperation = hasGlyph(spell, "separate") || hasGlyph(spell, "gentle");
        boolean hasDisruptiveOperation = hasGlyph(spell, "disrupt")
                || hasGlyph(spell, "terminate")
                || hasGlyph(spell, "unravel");

        if (hasLockingPattern && hasUnlockOperation) {
            return PatternInteractionMode.UNLOCK;
        }
        if (hasLockingPattern && hasDisruptiveOperation) {
            return PatternInteractionMode.DISRUPT;
        }
        if (hasLockingPattern && hasBinding) {
            return PatternInteractionMode.LOCK;
        }
        return PatternInteractionMode.NONE;
    }

    private static boolean hasGlyph(SpellChain spell, String glyphId) {
        return spell.glyphs().stream().anyMatch(glyph -> glyph.id().equals(glyphId));
    }

    /**
     * Current high-level pattern interaction modes.
     */
    public enum PatternInteractionMode {
        NONE,
        LOCK,
        UNLOCK,
        DISRUPT
    }
}
