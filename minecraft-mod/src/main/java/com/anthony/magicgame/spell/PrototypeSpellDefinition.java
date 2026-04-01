package com.anthony.magicgame.spell;

import java.util.List;

/**
 * Data-driven spell preset used for command-driven prototype testing before a full authoring UI exists.
 *
 * @param id stable spell identifier used by commands and persistence
 * @param displayName user-facing spell name
 * @param description short explanation of the spell's intended behavior
 * @param glyphIds ordered glyph ids that form the raw spell chain
 */
public record PrototypeSpellDefinition(
        String id,
        String displayName,
        String description,
        List<String> glyphIds
) {
    public PrototypeSpellDefinition {
        glyphIds = List.copyOf(glyphIds);
    }
}