package com.angellight.magicgame.network;

import com.angellight.magicgame.MagicGameMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Carries the current server-authoritative mana state used by the temporary in-game mana HUD.
 *
 * @param currentMana currently available mana
 * @param maxMana maximum mana capacity
 * @param regenPerSecond regeneration amount applied once per second
 * @param showDetails whether extra debug-detail text should be shown beside the core mana indicator
 */
public record ManaHudPayload(int currentMana, int maxMana, int regenPerSecond, boolean showDetails)
        implements CustomPacketPayload {
    public static final Identifier PAYLOAD_ID =
            Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "mana_hud");
    public static final Type<ManaHudPayload> ID = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaHudPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ManaHudPayload::currentMana,
            ByteBufCodecs.INT, ManaHudPayload::maxMana,
            ByteBufCodecs.INT, ManaHudPayload::regenPerSecond,
            ByteBufCodecs.BOOL, ManaHudPayload::showDetails,
            ManaHudPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
