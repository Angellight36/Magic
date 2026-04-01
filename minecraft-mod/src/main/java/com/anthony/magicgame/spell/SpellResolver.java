package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Converts interpreted prototype spells into lightweight resolution plans for debugging and balancing.
 */
public final class SpellResolver {
    private SpellResolver() {
    }

    public static SpellResolutionPlan resolve(PrototypeSpellDefinition definition) {
        SpellChain spell = CoreGlyphRegistry.chain(definition.glyphIds().toArray(String[]::new));
        InterpretedSpell interpreted = SpellInterpreter.interpret(spell);
        int manaCost = spell.glyphs().stream().mapToInt(GlyphDefinition::manaWeight).sum();
        int stabilityPenalty = spell.glyphs().size() * 2 + interpreted.warnings().size() * 8;
        int stabilityScore = Math.max(15, 100 - stabilityPenalty);
        List<String> warnings = new ArrayList<>(interpreted.warnings());
        if (definition.glyphIds().size() >= 10) {
            warnings.add("Long manual chains should later support shorthand or ritual tooling.");
        }

        MagicDomain primaryDomain = interpreted.domainCandidates().stream()
                .max(Comparator.comparingInt(domain -> scoreDomain(interpreted.intent(), domain)))
                .orElse(MagicDomain.INFORMATION);

        return new SpellResolutionPlan(interpreted, primaryDomain, manaCost, stabilityScore, warnings);
    }

    private static int scoreDomain(SpellIntent intent, MagicDomain domain) {
        return switch (intent) {
            case TRAVELING_EFFECT -> domain == MagicDomain.DAMAGE ? 4 : domain == MagicDomain.SPATIAL ? 3 : 1;
            case BOUNDARY_WARD -> domain == MagicDomain.PATTERN ? 4 : domain == MagicDomain.SPATIAL ? 3 : 1;
            case PATTERN_INTERACTION -> domain == MagicDomain.PATTERN ? 4 : domain == MagicDomain.INFORMATION ? 3 : 1;
            case HEALING_EFFECT -> domain == MagicDomain.LIFE ? 4 : domain == MagicDomain.INFORMATION ? 2 : 1;
            case CONSTRUCTION_EFFECT -> domain == MagicDomain.STRUCTURE ? 4 : domain == MagicDomain.SPATIAL ? 3 : 1;
            case UNKNOWN_UNSTABLE -> 1;
        };
    }
}