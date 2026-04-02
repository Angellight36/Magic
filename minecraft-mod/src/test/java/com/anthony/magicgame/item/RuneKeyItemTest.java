package com.anthony.magicgame.item;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers persistent signature storage for rune keys.
 */
class RuneKeyItemTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void signatureRoundTripsThroughCustomData() {
        ItemStack stack = new ItemStack(Items.STICK);
        assertNull(RuneKeyItem.getSignature(stack));

        RuneKeyItem.setSignature(stack, "test-signature-1234");

        assertEquals("test-signature-1234", RuneKeyItem.getSignature(stack));
        assertTrue(stack.getHoverName().getString().contains("TEST-SIG"));
    }
}
