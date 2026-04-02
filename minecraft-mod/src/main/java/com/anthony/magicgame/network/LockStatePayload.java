package com.anthony.magicgame.network;

import com.anthony.magicgame.MagicGameMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Syncs the currently locked block positions so clients can suppress denied interaction flicker.
 *
 * @param entries server-authoritative set of currently magic-locked blocks
 */
public record LockStatePayload(List<LockStatePayload.LockStateEntry> entries) implements CustomPacketPayload {
    public static final Identifier PAYLOAD_ID =
            Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "lock_states");
    public static final Type<LockStatePayload> ID = new Type<>(PAYLOAD_ID);
    private static final StreamCodec<RegistryFriendlyByteBuf, LockStateEntry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LockStateEntry::dimensionId,
            ByteBufCodecs.INT, LockStateEntry::x,
            ByteBufCodecs.INT, LockStateEntry::y,
            ByteBufCodecs.INT, LockStateEntry::z,
            ByteBufCodecs.STRING_UTF8, entry -> entry.keySignature() == null ? "" : entry.keySignature(),
            LockStateEntry::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LockStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ENTRY_CODEC), payload -> new ArrayList<>(payload.entries()),
            LockStatePayload::new
    );

    public LockStatePayload {
        entries = List.copyOf(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    /**
     * Minimal synced lock state for a single position.
     *
     * @param dimensionId serialized dimension identifier
     * @param x block x position
     * @param y block y position
     * @param z block z position
     * @param keySignature keyed signature required for local allow/deny prediction, or null for unkeyed locks
     */
    public record LockStateEntry(String dimensionId, int x, int y, int z, String keySignature) {
        public LockStateEntry {
            keySignature = keySignature == null || keySignature.isBlank() ? null : keySignature;
        }
    }
}
