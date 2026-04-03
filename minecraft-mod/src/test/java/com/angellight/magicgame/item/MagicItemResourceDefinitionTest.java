package com.angellight.magicgame.item;

import java.io.InputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that custom items ship the item-definition resources required for the current Minecraft item pipeline.
 */
class MagicItemResourceDefinitionTest {
    @Test
    void glyphFocusDefinitionExists() {
        assertResourceExists("/assets/magicgame/items/glyph_focus.json");
    }

    @Test
    void linkedKeyDefinitionExists() {
        assertResourceExists("/assets/magicgame/items/linked_key.json");
    }

    @Test
    void physicalLockDefinitionExists() {
        assertResourceExists("/assets/magicgame/items/physical_lock.json");
    }

    private static void assertResourceExists(String path) {
        try (InputStream stream = MagicItemResourceDefinitionTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, () -> "Missing resource " + path);
        } catch (Exception exception) {
            throw new AssertionError("Failed while checking resource " + path, exception);
        }
    }
}
