package com.anthony.magicgame.spell.pattern;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Keeps persistent block pattern tags enforced over time.
 */
public final class BlockPatternTagTicker {
    private BlockPatternTagTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> BlockPatternTagManager.get(server).tick(server));
    }
}
