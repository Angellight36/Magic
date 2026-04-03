package com.angellight.magicgame.client;

import com.angellight.magicgame.MagicGameMod;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side hooks for the Magic prototype.
 */
public final class MagicGameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MagicCastingClientController.register();
        MagicClientNetworking.register();
        MagicHudOverlay.register();
        MagicGameMod.LOGGER.info("Magic client bootstrap ready.");
    }
}
