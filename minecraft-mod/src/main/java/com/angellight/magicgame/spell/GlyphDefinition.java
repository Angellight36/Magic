package com.angellight.magicgame.spell;

import java.util.Set;

/**
 * Immutable metadata for a single glyph that can appear in a spell chain.
 *
 * @param id stable machine-readable identifier
 * @param displayName user-facing glyph name
 * @param category structural role the glyph plays when a spell is interpreted
 * @param domainHints likely resolution domains for early prototype routing
 * @param manaWeight rough mana cost contribution used for balancing passes
 * @param stabilityWeight rough stability contribution used for balancing passes
 */
public record GlyphDefinition(
        String id,
        String displayName,
        GlyphCategory category,
        Set<MagicDomain> domainHints,
        int manaWeight,
        int stabilityWeight
) {
    public GlyphDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Glyph id must not be blank.");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Glyph display name must not be blank.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Glyph category is required.");
        }
        domainHints = Set.copyOf(domainHints);
    }
}
