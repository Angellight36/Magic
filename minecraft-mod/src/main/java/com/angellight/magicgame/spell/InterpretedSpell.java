package com.angellight.magicgame.spell;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Semantic view of a raw spell chain after the first prototype interpretation pass.
 *
 * @param spell chain that was interpreted
 * @param intent inferred high-level outcome bucket
 * @param domainCandidates likely resolution domains for future execution systems
 * @param domainScores weighted domain preferences derived from glyphs, traits, and inferred intent
 * @param traits semantic traits that remain important even when they are not the dominant intent
 * @param recipientScores structural recipient candidates inferred from the glyph chain
 * @param sourceScores structural source candidates inferred from the glyph chain
 * @param intentScores scored intent candidates before the best-fit intent was selected
 * @param confidenceScore rough margin between the best and second-best intent candidates
 * @param warnings structural concerns discovered during interpretation
 */
public record InterpretedSpell(
        SpellChain spell,
        SpellIntent intent,
        Set<MagicDomain> domainCandidates,
        Map<MagicDomain, Integer> domainScores,
        Set<SpellTrait> traits,
        Map<SpellRecipient, Integer> recipientScores,
        Map<SpellSource, Integer> sourceScores,
        Map<SpellIntent, Integer> intentScores,
        int confidenceScore,
        List<String> warnings
) {
    public InterpretedSpell {
        domainCandidates = Set.copyOf(domainCandidates);
        domainScores = Map.copyOf(domainScores);
        traits = Set.copyOf(traits);
        recipientScores = Map.copyOf(recipientScores);
        sourceScores = Map.copyOf(sourceScores);
        intentScores = Map.copyOf(intentScores);
        warnings = List.copyOf(warnings);
    }

    public int domainScore(MagicDomain domain) {
        return domainScores.getOrDefault(domain, 0);
    }

    public int intentScore(SpellIntent candidate) {
        return intentScores.getOrDefault(candidate, 0);
    }

    public int recipientScore(SpellRecipient candidate) {
        return recipientScores.getOrDefault(candidate, 0);
    }

    public int sourceScore(SpellSource candidate) {
        return sourceScores.getOrDefault(candidate, 0);
    }
}
