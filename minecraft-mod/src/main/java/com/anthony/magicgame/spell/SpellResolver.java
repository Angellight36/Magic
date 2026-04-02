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

        return new SpellResolutionPlan(interpreted, primaryDomain, manaCost, stabilityScore, warnings);
    }
}
