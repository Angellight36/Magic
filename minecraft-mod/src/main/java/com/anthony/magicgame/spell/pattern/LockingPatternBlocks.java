package com.anthony.magicgame.spell.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Compatibility helpers for magical lock targets on top of the generalized pattern-tag block layer.
 */
public final class LockingPatternBlocks {
    private LockingPatternBlocks() {
    }

    public static boolean isLockable(ServerLevel level, BlockPos pos) {
        return PatternTaggedBlocks.supportsTag(level, pos, BlockPatternTag.MAGIC_LOCKED);
    }

    public static BlockPos canonicalize(ServerLevel level, BlockPos pos) {
        return PatternTaggedBlocks.canonicalize(level, pos);
    }
}
