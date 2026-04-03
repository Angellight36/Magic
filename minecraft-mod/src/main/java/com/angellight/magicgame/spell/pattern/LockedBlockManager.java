package com.angellight.magicgame.spell.pattern;

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

    public boolean lockWithKey(ServerLevel level, BlockPos pos, String keySignature) {
        return BlockPatternTagManager.get(server).addTag(BlockPatternTag.MAGIC_LOCKED, level, pos, keySignature);
    }

    public boolean unlock(ServerLevel level, BlockPos pos) {
        return BlockPatternTagManager.get(server).removeTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
    }

    public boolean unlockWithKey(ServerLevel level, BlockPos pos, String keySignature) {
        String storedSignature = keySignature(level, pos);
        if (storedSignature == null || !storedSignature.equals(keySignature)) {
            return false;
        }
        return unlock(level, pos);
    }

    public boolean isLocked(ServerLevel level, BlockPos pos) {
        return BlockPatternTagManager.get(server).hasTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
    }

    public String keySignature(ServerLevel level, BlockPos pos) {
        TaggedBlockPatternState state = BlockPatternTagManager.get(server).findTag(BlockPatternTag.MAGIC_LOCKED, level, pos);
        return state == null ? null : state.keySignature();
    }

    public int size() {
        return BlockPatternTagManager.get(server).size();
    }
}
