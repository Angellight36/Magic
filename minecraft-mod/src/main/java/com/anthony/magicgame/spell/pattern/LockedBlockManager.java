package com.anthony.magicgame.spell.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * Backward-compatible facade over the generalized block pattern tag system for magical locks.
 */
public final class LockedBlockManager {
    public static LockedBlockManager get(MinecraftServer server) {
        return new LockedBlockManager(server);
    }

    private final MinecraftServer server;

    private LockedBlockManager(MinecraftServer server) {
        this.server = server;
    }

    public boolean lock(ServerLevel level, BlockPos pos) {
        return BlockPatternTagManager.get(server).addTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
    }

    public boolean unlock(ServerLevel level, BlockPos pos) {
        return BlockPatternTagManager.get(server).removeTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
    }

    public boolean isLocked(ServerLevel level, BlockPos pos) {
        return BlockPatternTagManager.get(server).hasTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
    }

    public int size() {
        return BlockPatternTagManager.get(server).size();
    }
}
