package com.anthony.magicgame.client;

import com.anthony.magicgame.item.GlyphFocusItem;
import com.anthony.magicgame.item.MagicItems;
import com.anthony.magicgame.network.FocusGlyphChainPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Handles the current dev-build casting loop built around a held focus item, a composer key, and a last-spell quick cast.
 */
public final class MagicCastingClientController {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("magicgame", "controls"));
    private static final KeyMapping OPEN_COMPOSER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.magicgame.open_composer",
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY
    ));
    private static final KeyMapping QUICK_CAST_LAST = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.magicgame.quick_cast_last",
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY
    ));

    private MagicCastingClientController() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MagicCastingClientController::tick);
    }

    public static boolean isHoldingFocus(LocalPlayer player) {
        return activeFocusHand(player) != null;
    }

    public static void castCurrentChain(Minecraft minecraft) {
        InteractionHand hand = activeFocusHand(minecraft.player);
        if (hand != null) {
            castCurrentChain(minecraft, hand);
        }
    }

    public static void analyzeCurrentChain(Minecraft minecraft) {
        InteractionHand hand = activeFocusHand(minecraft.player);
        if (hand != null) {
            analyzeCurrentChain(minecraft, hand);
        }
    }

    private static void tick(Minecraft minecraft) {
        while (OPEN_COMPOSER.consumeClick()) {
            openComposer(minecraft);
        }
        while (QUICK_CAST_LAST.consumeClick()) {
            quickCastLast(minecraft);
        }
    }

    private static void openComposer(Minecraft minecraft) {
        InteractionHand hand = activeFocusHand(minecraft.player);
        if (!hasFocusReady(minecraft, hand)) {
            return;
        }
        minecraft.setScreen(new GlyphComposerScreen(hand));
    }

    private static void quickCastLast(Minecraft minecraft) {
        InteractionHand hand = activeFocusHand(minecraft.player);
        if (!hasFocusReady(minecraft, hand)) {
            return;
        }
        List<String> glyphIds = storedGlyphs(minecraft, hand);
        if (glyphIds.isEmpty()) {
            showActionBar(minecraft, "Write at least one glyph onto this focus before quick-casting.");
            return;
        }
        sendSpellCommand(minecraft, hand, "cast", glyphIds, true);
    }

    public static void castCurrentChain(Minecraft minecraft, InteractionHand hand) {
        sendSpellCommand(minecraft, hand, "cast", storedGlyphs(minecraft, hand), true);
    }

    public static void analyzeCurrentChain(Minecraft minecraft, InteractionHand hand) {
        sendSpellCommand(minecraft, hand, "analyze", storedGlyphs(minecraft, hand), false);
    }

    public static List<String> storedGlyphs(Minecraft minecraft, InteractionHand hand) {
        ItemStack stack = heldFocusStack(minecraft, hand);
        if (stack.isEmpty()) {
            return List.of();
        }
        return GlyphFocusItem.getStoredGlyphs(stack);
    }

    public static String storedChainText(Minecraft minecraft, InteractionHand hand) {
        ItemStack stack = heldFocusStack(minecraft, hand);
        return stack.isEmpty() ? "(focus missing)" : GlyphFocusItem.storedChainText(stack);
    }

    public static void appendGlyph(Minecraft minecraft, InteractionHand hand, String glyphId) {
        List<String> glyphIds = new ArrayList<>(storedGlyphs(minecraft, hand));
        glyphIds.add(glyphId);
        updateStoredGlyphs(minecraft, hand, glyphIds);
    }

    public static void removeLastGlyph(Minecraft minecraft, InteractionHand hand) {
        List<String> glyphIds = new ArrayList<>(storedGlyphs(minecraft, hand));
        if (!glyphIds.isEmpty()) {
            glyphIds.removeLast();
            updateStoredGlyphs(minecraft, hand, glyphIds);
        }
    }

    public static void clearGlyphs(Minecraft minecraft, InteractionHand hand) {
        updateStoredGlyphs(minecraft, hand, List.of());
    }

    public static InteractionHand activeFocusHand(LocalPlayer player) {
        if (player == null) {
            return null;
        }
        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(MagicItems.GLYPH_FOCUS)) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getItemInHand(InteractionHand.OFF_HAND).is(MagicItems.GLYPH_FOCUS)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean hasFocusReady(Minecraft minecraft, InteractionHand hand) {
        if (minecraft.player == null || minecraft.player.connection == null) {
            return false;
        }
        if (hand == null || !minecraft.player.getItemInHand(hand).is(MagicItems.GLYPH_FOCUS)) {
            showActionBar(minecraft, "Hold a Glyph Focus to compose or quick-cast magic.");
            return false;
        }
        return true;
    }

    private static ItemStack heldFocusStack(Minecraft minecraft, InteractionHand hand) {
        if (minecraft.player == null || hand == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.is(MagicItems.GLYPH_FOCUS) ? stack : ItemStack.EMPTY;
    }

    private static void updateStoredGlyphs(Minecraft minecraft, InteractionHand hand, List<String> glyphIds) {
        if (!hasFocusReady(minecraft, hand)) {
            return;
        }

        ItemStack stack = heldFocusStack(minecraft, hand);
        if (stack.isEmpty()) {
            return;
        }

        GlyphFocusItem.setStoredGlyphs(stack, glyphIds);
        ClientPlayNetworking.send(new FocusGlyphChainPayload(hand, glyphIds));
    }

    private static void sendSpellCommand(
            Minecraft minecraft,
            InteractionHand hand,
            String action,
            List<String> glyphIds,
            boolean rememberAsLastCast
    ) {
        if (!hasFocusReady(minecraft, hand)) {
            return;
        }
        if (glyphIds.isEmpty()) {
            showActionBar(minecraft, "Write at least one glyph onto this focus before sending the spell.");
            return;
        }

        String glyphChain = String.join(" ", glyphIds);
        minecraft.player.connection.sendCommand("magic " + action + " chain " + glyphChain);
        if (rememberAsLastCast) {
            GlyphComposerState.rememberLastCast(glyphIds);
        }
    }

    private static void showActionBar(Minecraft minecraft, String message) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), true);
        }
    }
}
