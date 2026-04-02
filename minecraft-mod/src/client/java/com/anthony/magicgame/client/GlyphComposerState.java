package com.anthony.magicgame.client;

import com.anthony.magicgame.spell.GlyphDefinition;
import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stores lightweight client-only composer state that does not belong on the held focus item itself.
 */
public final class GlyphComposerState {
    private static List<String> lastCastGlyphs = List.of();

    private GlyphComposerState() {
    }

    public static void rememberLastCast(List<String> glyphIds) {
        lastCastGlyphs = List.copyOf(glyphIds);
    }

    public static boolean hasLastCastChain() {
        return !lastCastGlyphs.isEmpty();
    }

    public static String lastCastChainText() {
        return lastCastGlyphs.stream()
                .map(GlyphComposerState::displayGlyphLabel)
                .collect(Collectors.joining(" -> "));
    }

    private static String displayGlyphLabel(String glyphId) {
        Optional<GlyphDefinition> glyph = CoreGlyphRegistry.find(glyphId);
        return glyph.map(GlyphDefinition::displayName).orElse(glyphId.replace('_', ' '));
    }
}
