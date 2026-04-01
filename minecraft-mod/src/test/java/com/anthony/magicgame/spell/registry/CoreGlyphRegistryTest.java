package com.anthony.magicgame.spell.registry;

import com.anthony.magicgame.spell.GlyphCategory;
import com.anthony.magicgame.spell.SpellChain;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the initial registry slice so later content additions do not silently break the prototype.
 */
class CoreGlyphRegistryTest {
    @Test
    void registryIdsStayUnique() {
        Set<String> ids = CoreGlyphRegistry.all().stream()
                .map(glyph -> glyph.id())
                .collect(Collectors.toSet());

        assertEquals(CoreGlyphRegistry.size(), ids.size());
    }

    @Test
    void fireballSkeletonMeetsPrototypeCastRequirements() {
        SpellChain fireball = CoreGlyphRegistry.chain(
                "gather",
                "fire",
                "shape",
                "separate",
                "forward",
                "direct",
                "stabilize",
                "on_impact",
                "release"
        );

        assertTrue(fireball.isPrototypeCastable());
        assertTrue(fireball.categoriesPresent().containsAll(Set.of(
                GlyphCategory.PRINCIPLE,
                GlyphCategory.OPERATION,
                GlyphCategory.CONSTRAINT
        )));
    }
}
