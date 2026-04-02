package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts interpreted prototype spells into lightweight resolution plans for debugging and balancing.
 */
public final class SpellResolver {
    private SpellResolver() {
    }

    public static SpellResolutionPlan resolve(PrototypeSpellDefinition definition) {
        return resolve(CoreGlyphRegistry.chain(definition.glyphIds().toArray(String[]::new)));
    }

    public static SpellResolutionPlan resolve(SpellChain spell) {
        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);
        int manaCost = spell.glyphs().stream().mapToInt(GlyphDefinition::manaWeight).sum();
        int structuralPenalty = spell.glyphs().stream().mapToInt(GlyphDefinition::stabilityWeight).sum();
        int uncertaintyPenalty = Math.max(0, 10 - interpreted.confidenceScore()) * 2;
        int stabilityPenalty = structuralPenalty + interpreted.warnings().size() * 8 + uncertaintyPenalty;
        int stabilityScore = Math.max(15, 100 - stabilityPenalty);
        List<String> warnings = new ArrayList<>(interpreted.warnings());
        if (spell.glyphs().size() >= 10) {
            warnings.add("Long manual chains should later support shorthand or ritual tooling.");
        }
        if (interpreted.confidenceScore() <= 2 && interpreted.intent() != SpellIntent.UNKNOWN_UNSTABLE) {
            warnings.add("Interpreter sees multiple plausible outcomes for this chain.");
        }

        MagicDomain primaryDomain = interpreted.domainScores().entrySet().stream()
                .max(Map.Entry.<MagicDomain, Integer>comparingByValue()
                        .thenComparing(entry -> entry.getKey().name()))
                .map(Map.Entry::getKey)
                .orElse(MagicDomain.INFORMATION);

        SpellFailureProfile likelyFailureProfile = selectFailureProfile(spell, interpreted, primaryDomain, stabilityScore);
        return new SpellResolutionPlan(interpreted, primaryDomain, manaCost, stabilityScore, likelyFailureProfile, warnings);
    }

    private static SpellFailureProfile selectFailureProfile(
            SpellChain spell,
            InterpretedSpell interpreted,
            MagicDomain primaryDomain,
            int stabilityScore
    ) {
        if (!spell.isPrototypeCastable()) {
            return new SpellFailureProfile(
                    SpellFailureType.STRUCTURAL_FAILURE,
                    4,
                    primaryDomain,
                    "The chain is missing enough structure to resolve cleanly.",
                    "Likely to partially mis-shape into a crude or unintended pattern effect."
            );
        }
        if (interpreted.intent() == SpellIntent.UNKNOWN_UNSTABLE || interpreted.confidenceScore() <= 1) {
            return new SpellFailureProfile(
                    SpellFailureType.INTERPRETIVE_FAILURE,
                    3,
                    primaryDomain,
                    "The interpreter sees multiple plausible outcomes and cannot strongly disambiguate them.",
                    "Likely to drift into the wrong effect family or resolve inconsistently."
            );
        }
        if (interpreted.warnings().stream().anyMatch(warning -> warning.contains("difficult to aim") || warning.contains("recipient or destination"))) {
            return new SpellFailureProfile(
                    SpellFailureType.INFORMATION_FAILURE,
                    2,
                    primaryDomain,
                    "The chain lacks enough targeting or reference detail for precise resolution.",
                    "Likely to choose the wrong target, miss, or resolve with fuzzy recipients."
            );
        }
        if (interpreted.intent() == SpellIntent.BOUNDARY_WARD
                && interpreted.warnings().stream().anyMatch(warning -> warning.contains("disperse unpredictably"))) {
            return new SpellFailureProfile(
                    SpellFailureType.PERSISTENCE_FAILURE,
                    3,
                    primaryDomain,
                    "The anchored structure is under-defined for stable persistence.",
                    "Likely to collapse early, flicker, or bind to the wrong local structure."
            );
        }
        if (stabilityScore < 55) {
            return new SpellFailureProfile(
                    SpellFailureType.STRUCTURAL_FAILURE,
                    2,
                    primaryDomain,
                    "The chain is technically resolvable but still structurally fragile.",
                    "Likely to lose potency, overbuild, or resolve with sloppy side effects."
            );
        }
        return null;
    }
}
