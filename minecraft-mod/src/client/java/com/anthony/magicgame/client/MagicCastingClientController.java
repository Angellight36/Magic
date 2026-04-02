package com.anthony.magicgame.client;

import com.anthony.magicgame.item.MagicItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
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
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(MagicItems.GLYPH_FOCUS)
                || player.getItemInHand(InteractionHand.OFF_HAND).is(MagicItems.GLYPH_FOCUS);
    }

    public static void castCurrentChain(Minecraft minecraft) {
        sendSpellCommand(minecraft, "cast", GlyphComposerState.currentChainText(), true);
    }

    public static void analyzeCurrentChain(Minecraft minecraft) {
        sendSpellCommand(minecraft, "analyze", GlyphComposerState.currentChainText(), false);
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
        if (!hasFocusReady(minecraft)) {
            return;
        }
        minecraft.setScreen(new GlyphComposerScreen());
    }

    private static void quickCastLast(Minecraft minecraft) {
        if (!hasFocusReady(minecraft)) {
            return;
        }
        if (!GlyphComposerState.hasLastCastChain()) {
            showActionBar(minecraft, "No composed spell is ready for quick-cast yet.");
            return;
        }
        sendSpellCommand(minecraft, "cast", GlyphComposerState.lastCastChainText(), false);
    }

    private static boolean hasFocusReady(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.connection == null) {
            return false;
        }
        if (!isHoldingFocus(minecraft.player)) {
            showActionBar(minecraft, "Hold a Glyph Focus to compose or quick-cast magic.");
            return false;
        }
        return true;
    }

    private static void sendSpellCommand(Minecraft minecraft, String action, String glyphChain, boolean rememberAsLastCast) {
        if (!hasFocusReady(minecraft)) {
            return;
        }
        if (glyphChain == null || glyphChain.isBlank()) {
            showActionBar(minecraft, "Compose at least one glyph before sending the spell.");
            return;
        }

        minecraft.player.connection.sendCommand("magic " + action + " chain " + glyphChain);
        if (rememberAsLastCast) {
            GlyphComposerState.rememberCurrentAsLastCast();
        }
    }

    private static void showActionBar(Minecraft minecraft, String message) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), true);
        }
    }
}
