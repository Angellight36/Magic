package com.anthony.magicgame.spell;

import java.util.EnumSet;
import java.util.List;

/**
 * Ordered glyph list representing the raw form of a spell before full interpretation.
 */
public record SpellChain(List<GlyphDefinition> glyphs) {
    public SpellChain {
        glyphs = List.copyOf(glyphs);
        if (glyphs.isEmpty()) {
            throw new IllegalArgumentException("A spell chain must contain at least one glyph.");
        }
    }

    public EnumSet<GlyphCategory> categoriesPresent() {
        EnumSet<GlyphCategory> categories = EnumSet.noneOf(GlyphCategory.class);
        for (GlyphDefinition glyph : glyphs) {
            categories.add(glyph.category());
        }
        return categories;
    }

    public long countByCategory(GlyphCategory category) {
        return glyphs.stream().filter(glyph -> glyph.category() == category).count();
    }

    public boolean isPrototypeCastable() {
        EnumSet<GlyphCategory> categories = categoriesPresent();
        return categories.contains(GlyphCategory.PRINCIPLE) && categories.contains(GlyphCategory.OPERATION);
    }
}
