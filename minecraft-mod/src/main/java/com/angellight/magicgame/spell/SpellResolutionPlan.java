package com.angellight.magicgame.spell;

import java.util.List;

/**
 * Lightweight resolution output used by the command prototype to report cost, intent, and risk.
 *
 * @param interpretedSpell semantic interpretation of the source spell chain
 * @param primaryDomain best-fit execution domain for the current spell
 * @param manaCost prototype mana cost estimate
 * @param stabilityScore rough 0-100 stability estimate used for early balancing
 * @param likelyFailureProfile most likely failure mode if the spell resolves poorly
 * @param warnings important resolution warnings to surface to the player or developer
 */
public record SpellResolutionPlan(
        InterpretedSpell interpretedSpell,
        MagicDomain primaryDomain,
        int manaCost,
        int stabilityScore,
        SpellFailureProfile likelyFailureProfile,
        List<String> warnings
) {
    public SpellResolutionPlan {
        warnings = List.copyOf(warnings);
    }

    public SpellIntent intent() {
        return interpretedSpell.intent();
    }

    public int confidenceScore() {
        return interpretedSpell.confidenceScore();
    }
}
