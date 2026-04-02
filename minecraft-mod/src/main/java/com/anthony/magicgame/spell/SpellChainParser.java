package com.anthony.magicgame.spell;

import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses temporary text-form glyph sequences into prototype spell chains for command-driven testing.
 */
public final class SpellChainParser {
    private SpellChainParser() {
    }

    /**
     * Parses a glyph sequence using whitespace, commas, {@code >}, or {@code ->} as separators.
     *
     * @param rawGlyphSequence user-provided glyph sequence
     * @return parsed spell chain
     */
    public static SpellChain parse(String rawGlyphSequence) {
        List<String> glyphIds = tokenize(rawGlyphSequence);
        List<GlyphDefinition> glyphs = new ArrayList<>();
        List<String> unknownGlyphs = new ArrayList<>();
        for (String glyphId : glyphIds) {
            CoreGlyphRegistry.find(glyphId)
                    .ifPresentOrElse(glyphs::add, () -> unknownGlyphs.add(glyphId));
        }

        if (!unknownGlyphs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown glyph ids: " + String.join(", ", unknownGlyphs)
                            + ". Use /magic glyphs to inspect the current prototype registry."
            );
        }
        return new SpellChain(glyphs);
    }

    /**
     * Tokenizes a raw glyph sequence into normalized glyph ids.
     *
     * @param rawGlyphSequence user-provided glyph sequence
     * @return ordered glyph ids
     */
    public static List<String> tokenize(String rawGlyphSequence) {
        if (rawGlyphSequence == null || rawGlyphSequence.isBlank()) {
            throw new IllegalArgumentException("Spell chains must include at least one glyph id.");
        }

        String normalized = rawGlyphSequence
                .replace("->", " ")
                .replace(">", " ")
                .replace(",", " ");
        List<String> glyphIds = normalized.lines()
                .flatMap(line -> List.of(line.trim().split("\\s+")).stream())
                .map(token -> token.toLowerCase(Locale.ROOT))
                .filter(token -> !token.isBlank())
                .toList();

        if (glyphIds.isEmpty()) {
            throw new IllegalArgumentException("Spell chains must include at least one glyph id.");
        }
        return glyphIds;
    }
}
