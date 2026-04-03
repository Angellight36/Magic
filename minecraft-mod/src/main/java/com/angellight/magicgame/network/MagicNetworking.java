package com.angellight.magicgame.network;

import com.angellight.magicgame.debug.MagicDebugFeature;
import com.angellight.magicgame.debug.MagicDebugSettings;
import com.angellight.magicgame.item.GlyphFocusItem;
import com.angellight.magicgame.item.MagicItems;
import com.angellight.magicgame.mana.ManaProfile;
import com.angellight.magicgame.mana.PlayerManaManager;
import com.angellight.magicgame.spell.pattern.BlockPatternTag;
import com.angellight.magicgame.spell.pattern.BlockPatternTagManager;
import com.angellight.magicgame.spell.pattern.TaggedBlockPatternState;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Registers payload types and provides small helper methods for syncing prototype HUD state.
 */
public final class MagicNetworking {
    private static final int DEFAULT_SPELL_FEEDBACK_TICKS = 120;

    private MagicNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FocusGlyphChainPayload.ID, FocusGlyphChainPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ManaHudPayload.ID, ManaHudPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpellFeedbackPayload.ID, SpellFeedbackPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LockStatePayload.ID, LockStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FocusGlyphChainPayload.ID, (payload, context) ->
                applyFocusGlyphChain(context.player(), payload));
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

    private static void applyFocusGlyphChain(ServerPlayer player, FocusGlyphChainPayload payload) {
        ItemStack stack = player.getItemInHand(payload.hand());
        if (!stack.is(MagicItems.GLYPH_FOCUS)) {
            return;
        }
        GlyphFocusItem.setStoredGlyphs(stack, payload.glyphIds());
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
