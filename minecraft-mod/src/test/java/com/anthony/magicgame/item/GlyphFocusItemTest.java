package com.anthony.magicgame.item;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers per-focus written spell storage.
 */
class GlyphFocusItemTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void storedGlyphsRoundTripOnOneFocus() {
        ItemStack stack = new ItemStack(Items.BOOK);

        GlyphFocusItem.setStoredGlyphs(stack, java.util.List.of("fire", "shape", "travel"));

        assertEquals(java.util.List.of("fire", "shape", "travel"), GlyphFocusItem.getStoredGlyphs(stack));
        assertTrue(GlyphFocusItem.storedChainText(stack).contains("Fire"));
    }

    @Test
    void clearingFocusRemovesStoredGlyphs() {
        ItemStack stack = new ItemStack(Items.BOOK);
        GlyphFocusItem.setStoredGlyphs(stack, java.util.List.of("life", "restore"));

        GlyphFocusItem.clearStoredGlyphs(stack);

        assertTrue(GlyphFocusItem.getStoredGlyphs(stack).isEmpty());
        assertEquals("(empty)", GlyphFocusItem.storedChainText(stack));
    }

    @Test
    void twoFocusStacksKeepIndependentWrittenSpells() {
        ItemStack firstFocus = new ItemStack(Items.BOOK);
        ItemStack secondFocus = new ItemStack(Items.BOOK);

        GlyphFocusItem.setStoredGlyphs(firstFocus, java.util.List.of("fire", "travel"));
        GlyphFocusItem.setStoredGlyphs(secondFocus, java.util.List.of("life", "restore"));

        assertEquals(java.util.List.of("fire", "travel"), GlyphFocusItem.getStoredGlyphs(firstFocus));
        assertEquals(java.util.List.of("life", "restore"), GlyphFocusItem.getStoredGlyphs(secondFocus));
    }
}
