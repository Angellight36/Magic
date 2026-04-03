package com.angellight.magicgame.mana;

import com.angellight.magicgame.network.MagicNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Regenerates player mana on the server so early tests have a reusable resource loop.
 */
public final class ManaRegenerationService {
    private static int tickCounter;

    private ManaRegenerationService() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ManaRegenerationService::tick);
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 20) {
            return;
        }
        tickCounter = 0;

        PlayerManaManager manaManager = PlayerManaManager.get(server);
        boolean changed = false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ManaProfile mana = manaManager.getOrCreate(player.getUUID());
            int before = mana.currentMana();
            mana.regenerate();
            changed |= before != mana.currentMana();
            MagicNetworking.syncMana(player, mana);
        }

        if (changed) {
            manaManager.setDirty();
        }
    }
}
