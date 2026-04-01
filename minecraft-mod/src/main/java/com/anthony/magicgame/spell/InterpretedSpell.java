package com.anthony.magicgame.spell;

import java.util.List;
import java.util.Set;

/**
 * Semantic view of a raw spell chain after the first prototype interpretation pass.
 *
 * @param spell chain that was interpreted
 * @param intent inferred high-level outcome bucket
 * @param domainCandidates likely resolution domains for future execution systems
 * @param warnings structural concerns discovered during interpretation
 */
public record InterpretedSpell(
        SpellChain spell,
        SpellIntent intent,
        Set<MagicDomain> domainCandidates,
        List<String> warnings
) {
    public InterpretedSpell {
        domainCandidates = Set.copyOf(domainCandidates);
        warnings = List.copyOf(warnings);
    }
}