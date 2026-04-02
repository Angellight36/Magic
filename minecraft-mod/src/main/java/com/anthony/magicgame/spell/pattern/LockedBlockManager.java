package com.anthony.magicgame.spell.pattern;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stores persistent magical lock states for world blocks.
 */
public final class LockedBlockManager extends SavedData {
    private static final String DATA_ID = "magicgame_locked_blocks";
    private static final Codec<LockedBlockManager> CODEC = LockedBlockState.CODEC.listOf()
            .xmap(LockedBlockManager::fromSerialized, LockedBlockManager::toSerialized);
    private static final SavedDataType<LockedBlockManager> TYPE = new SavedDataType<>(
            DATA_ID,
            LockedBlockManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final List<LockedBlockState> lockedBlocks = new ArrayList<>();

    public static LockedBlockManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean lock(ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = LockingPatternBlocks.canonicalize(level, pos);
        if (isLocked(level, canonicalPos)) {
            return false;
        }
        lockedBlocks.add(LockedBlockState.from(level, canonicalPos));
        setDirty();
        return true;
    }

    public boolean unlock(ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = LockingPatternBlocks.canonicalize(level, pos);
        boolean removed = lockedBlocks.removeIf(state -> state.matches(level, canonicalPos));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public boolean isLocked(ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = LockingPatternBlocks.canonicalize(level, pos);
        return lockedBlocks.stream().anyMatch(state -> state.matches(level, canonicalPos));
    }

    public int size() {
        return lockedBlocks.size();
    }

    private static LockedBlockManager fromSerialized(List<LockedBlockState> states) {
        LockedBlockManager manager = new LockedBlockManager();
        manager.lockedBlocks.addAll(states);
        return manager;
    }

    private List<LockedBlockState> toSerialized() {
        return List.copyOf(lockedBlocks);
    }
}
