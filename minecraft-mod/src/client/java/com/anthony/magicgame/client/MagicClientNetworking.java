package com.anthony.magicgame.client;

import com.anthony.magicgame.network.ManaHudPayload;
import com.anthony.magicgame.network.SpellFeedbackPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Registers client-side packet receivers used by the temporary spell and mana HUD.
 */
public final class MagicClientNetworking {
    private MagicClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ManaHudPayload.ID, (payload, context) ->
                context.client().execute(() -> MagicHudOverlay.updateMana(payload)));
        ClientPlayNetworking.registerGlobalReceiver(SpellFeedbackPayload.ID, (payload, context) ->
                context.client().execute(() -> MagicHudOverlay.showSpellFeedback(payload)));
    }
}
