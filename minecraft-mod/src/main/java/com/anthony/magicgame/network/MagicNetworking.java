package com.anthony.magicgame.network;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.anthony.magicgame.mana.ManaProfile;
import com.anthony.magicgame.mana.PlayerManaManager;
import com.anthony.magicgame.spell.pattern.BlockPatternTag;
import com.anthony.magicgame.spell.pattern.BlockPatternTagManager;
import com.anthony.magicgame.spell.pattern.TaggedBlockPatternState;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers payload types and provides small helper methods for syncing prototype HUD state.
 */
public final class MagicNetworking {
    private static final int DEFAULT_SPELL_FEEDBACK_TICKS = 120;

    private MagicNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ManaHudPayload.ID, ManaHudPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpellFeedbackPayload.ID, SpellFeedbackPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LockStatePayload.ID, LockStatePayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncMana(handler.player);
            syncLockedBlocks(handler.player);
        });
    }

    public static void syncMana(ServerPlayer player) {
        ManaProfile mana = PlayerManaManager.get(player.level().getServer()).getOrCreate(player.getUUID());
        syncMana(player, mana);
    }

    public static void syncMana(ServerPlayer player, ManaProfile mana) {
        MagicDebugSettings settings = MagicDebugSettings.get(player.level().getServer());
        boolean showDetails = settings.isFeatureActive(MagicDebugFeature.MANA_HUD_TEXT);
        ServerPlayNetworking.send(player, new ManaHudPayload(
                mana.currentMana(),
                mana.maxMana(),
                mana.regenPerSecond(),
                showDetails
        ));
    }

    public static void syncManaForAll(MinecraftServer server) {
        PlayerManaManager manaManager = PlayerManaManager.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncMana(player, manaManager.getOrCreate(player.getUUID()));
        }
    }

    public static void sendSpellFeedback(ServerPlayer player, String title, String detail) {
        ServerPlayNetworking.send(player, new SpellFeedbackPayload(title, detail, DEFAULT_SPELL_FEEDBACK_TICKS));
    }

    public static void syncLockedBlocks(ServerPlayer player) {
        List<LockStatePayload.LockStateEntry> entries = BlockPatternTagManager.get(player.level().getServer())
                .statesForTag(BlockPatternTag.MAGIC_LOCKED).stream()
                .map(state -> new LockStatePayload.LockStateEntry(
                        state.dimensionId(),
                        state.x(),
                        state.y(),
                        state.z(),
                        state.keySignature()
                ))
                .toList();
        ServerPlayNetworking.send(player, new LockStatePayload(entries));
    }

    public static void syncLockedBlocksForAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncLockedBlocks(player);
        }
    }
}
