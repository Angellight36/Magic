package com.angellight.magicgame.client;

import com.angellight.magicgame.network.ManaHudPayload;
import com.angellight.magicgame.network.LockStatePayload;
import com.angellight.magicgame.network.SpellFeedbackPayload;
import com.angellight.magicgame.spell.pattern.LockedBlockClientCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
        ClientPlayNetworking.registerGlobalReceiver(LockStatePayload.ID, (payload, context) ->
                context.client().execute(() -> LockedBlockClientCache.replaceAll(payload.entries())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LockedBlockClientCache.clear());
    }
}
