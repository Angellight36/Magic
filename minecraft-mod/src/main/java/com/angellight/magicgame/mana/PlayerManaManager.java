package com.angellight.magicgame.mana;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stores server-persistent mana values for players so the prototype remains multiplayer-safe.
 */
public final class PlayerManaManager extends SavedData {
    private static final String DATA_ID = "magicgame_player_mana";
    private static final Codec<PlayerManaManager> CODEC = Codec.unboundedMap(Codec.STRING, ManaProfile.CODEC)
            .xmap(PlayerManaManager::fromSerialized, PlayerManaManager::toSerialized);
    private static final SavedDataType<PlayerManaManager> TYPE = new SavedDataType<>(
            DATA_ID,
            PlayerManaManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, ManaProfile> manaByPlayer = new HashMap<>();

    public static PlayerManaManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public ManaProfile getOrCreate(UUID playerId) {
        ManaProfile mana = manaByPlayer.get(playerId);
        if (mana == null) {
            mana = ManaProfile.createDefault();
            manaByPlayer.put(playerId, mana);
            setDirty();
        }
        return mana;
    }

    private static PlayerManaManager fromSerialized(Map<String, ManaProfile> serialized) {
        PlayerManaManager manager = new PlayerManaManager();
        for (Map.Entry<String, ManaProfile> entry : serialized.entrySet()) {
            manager.manaByPlayer.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        return manager;
    }

    private Map<String, ManaProfile> toSerialized() {
        Map<String, ManaProfile> serialized = new HashMap<>();
        for (Map.Entry<UUID, ManaProfile> entry : manaByPlayer.entrySet()) {
            serialized.put(entry.getKey().toString(), entry.getValue());
        }
        return serialized;
    }
}