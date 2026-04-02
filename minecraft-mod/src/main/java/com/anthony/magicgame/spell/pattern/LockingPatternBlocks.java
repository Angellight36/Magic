package com.anthony.magicgame.spell.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Block helpers for lockable/openable pattern targets.
 */
public final class LockingPatternBlocks {
    private LockingPatternBlocks() {
    }

    public static boolean isLockable(BlockState state) {
        return state.hasProperty(BlockStateProperties.OPEN);
    }

    public static BlockPos canonicalize(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        }
        return pos;
    }
}
