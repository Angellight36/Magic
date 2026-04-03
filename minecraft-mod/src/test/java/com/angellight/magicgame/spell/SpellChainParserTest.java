package com.angellight.magicgame.spell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the text parser used by the temporary custom spell commands.
 */
class SpellChainParserTest {
    @Test
    void parserSupportsMultipleSeparatorStyles() {
        SpellChain spell = SpellChainParser.parse("Gather -> Fire, Shape direct");

        assertEquals(4, spell.glyphs().size());
        assertEquals("gather", spell.glyphs().getFirst().id());
        assertEquals("direct", spell.glyphs().getLast().id());
    }

    @Test
    void parserRejectsUnknownGlyphs() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SpellChainParser.parse("fire mystery_glyph release")
        );

        assertEquals(
                "Unknown glyph ids: mystery_glyph. Use /magic glyphs to inspect the current prototype registry.",
                exception.getMessage()
        );
    }
}
