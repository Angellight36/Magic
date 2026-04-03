package com.angellight.magicgame.client;

import com.angellight.magicgame.MagicGameMod;
import com.angellight.magicgame.network.ManaHudPayload;
import com.angellight.magicgame.network.SpellFeedbackPayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * Renders the current dev-build mana indicator and recent spell feedback while final UI art is still pending.
 */
public final class MagicHudOverlay {
    private static final int MANA_SEGMENTS = 10;
    private static int currentMana;
    private static int maxMana;
    private static int regenPerSecond;
    private static boolean showDetails;
    private static String feedbackTitle = "";
    private static String feedbackDetail = "";
    private static long feedbackExpiresAtMs;

    private MagicHudOverlay() {
    }

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "mana_text_overlay"),
                MagicHudOverlay::render
        );
    }

    public static void updateMana(ManaHudPayload payload) {
        currentMana = payload.currentMana();
        maxMana = payload.maxMana();
        regenPerSecond = payload.regenPerSecond();
        showDetails = payload.showDetails();
    }

    public static void showSpellFeedback(SpellFeedbackPayload payload) {
        feedbackTitle = payload.title();
        feedbackDetail = payload.detail();
        feedbackExpiresAtMs = Util.getMillis() + payload.displayTicks() * 50L;
    }

    private static void render(GuiGraphics context, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }

        int x = 8;
        int y = context.guiHeight() - 68;
        renderManaIndicator(context, minecraft, x, y);
        y -= showDetails ? minecraft.font.lineHeight + 18 : 16;

        if (feedbackExpiresAtMs > Util.getMillis() && (!feedbackTitle.isBlank() || !feedbackDetail.isBlank())) {
            if (!feedbackDetail.isBlank()) {
                context.drawString(minecraft.font, Component.literal(feedbackDetail), x, y, 0xFFE8A3, true);
                y -= minecraft.font.lineHeight + 2;
            }
            if (!feedbackTitle.isBlank()) {
                context.drawString(minecraft.font, Component.literal(feedbackTitle), x, y, 0xFFFFFF, true);
            }
        }
    }

    private static void renderManaIndicator(GuiGraphics context, Minecraft minecraft, int x, int y) {
        context.drawString(minecraft.font, Component.literal("Mana"), x, y - 10, 0x80D8FF, true);
        int segmentWidth = 10;
        int segmentHeight = 8;
        int gap = 2;
        float manaPerSegment = Math.max(1.0F, maxMana / (float) MANA_SEGMENTS);
        for (int index = 0; index < MANA_SEGMENTS; index++) {
            int segmentX = x + index * (segmentWidth + gap);
            int segmentY = y;
            float filledRatio = Mth.clamp((currentMana - index * manaPerSegment) / manaPerSegment, 0.0F, 1.0F);
            context.fill(segmentX, segmentY, segmentX + segmentWidth, segmentY + segmentHeight, 0xAA132339);
            context.fill(segmentX + 1, segmentY + 1, segmentX + segmentWidth - 1, segmentY + segmentHeight - 1, 0xCC08111F);
            if (filledRatio > 0.0F) {
                int fillHeight = Math.max(1, Math.round((segmentHeight - 2) * filledRatio));
                context.fill(
                        segmentX + 2,
                        segmentY + segmentHeight - 1 - fillHeight,
                        segmentX + segmentWidth - 2,
                        segmentY + segmentHeight - 2,
                        0xFF57C7FF
                );
            }
        }

        if (showDetails) {
            context.drawString(
                    minecraft.font,
                    Component.literal(currentMana + "/" + maxMana + " (+" + regenPerSecond + "/s)"),
                    x,
                    y + 12,
                    0xB7EAFF,
                    true
            );
        }
    }
}
