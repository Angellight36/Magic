package com.angellight.magicgame.spell.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Ticks anchored spell effects once per second to keep ward logic and decay centralized.
 */
public final class AnchoredEffectTicker {
    private static int tickCounter;

    private AnchoredEffectTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AnchoredEffectTicker::tick);
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        AnchoredEffectManager.get(server).tick(server);
    }
}