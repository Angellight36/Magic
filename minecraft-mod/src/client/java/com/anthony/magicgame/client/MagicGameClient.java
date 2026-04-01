package com.anthony.magicgame.client;

import com.anthony.magicgame.MagicGameMod;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side hooks for the Magic prototype.
 */
public final class MagicGameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MagicGameMod.LOGGER.info("Magic client bootstrap ready.");
    }
}
