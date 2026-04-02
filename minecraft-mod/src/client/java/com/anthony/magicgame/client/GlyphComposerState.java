package com.anthony.magicgame.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the current and last-composed glyph chains on the client so the early composer and quick-cast loop share one state.
 */
public final class GlyphComposerState {
    private static final List<String> currentGlyphs = new ArrayList<>();
    private static List<String> lastCastGlyphs = List.of();

    private GlyphComposerState() {
    }

    public static void appendGlyph(String glyphId) {
        currentGlyphs.add(glyphId);
    }

    public static void removeLastGlyph() {
        if (!currentGlyphs.isEmpty()) {
            currentGlyphs.removeLast();
        }
    }

    public static void clearCurrentGlyphs() {
        currentGlyphs.clear();
    }

    public static List<String> currentGlyphs() {
        return List.copyOf(currentGlyphs);
    }

    public static String currentChainText() {
        return String.join(" ", currentGlyphs);
    }

    public static boolean hasCurrentChain() {
        return !currentGlyphs.isEmpty();
    }

    public static void rememberCurrentAsLastCast() {
        lastCastGlyphs = List.copyOf(currentGlyphs);
    }

    public static boolean hasLastCastChain() {
        return !lastCastGlyphs.isEmpty();
    }

    public static String lastCastChainText() {
        return String.join(" ", lastCastGlyphs);
    }
}
