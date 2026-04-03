package com.angellight.magicgame.spell.pattern;

import com.angellight.magicgame.network.LockStatePayload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Client-side cache of server-authoritative lock states used to suppress denied interaction flicker.
 */
public final class LockedBlockClientCache {
    private static final Map<LockedBlockKey, String> LOCKED_BLOCKS = new HashMap<>();

    private LockedBlockClientCache() {
    }

    public static void replaceAll(List<LockStatePayload.LockStateEntry> entries) {
        LOCKED_BLOCKS.clear();
        for (LockStatePayload.LockStateEntry entry : entries) {
            LOCKED_BLOCKS.put(
                    new LockedBlockKey(entry.dimensionId(), new BlockPos(entry.x(), entry.y(), entry.z())),
                    entry.keySignature()
            );
        }
    }

    public static void clear() {
        LOCKED_BLOCKS.clear();
    }

    public static String keySignature(Level level, BlockPos pos) {
        BlockPos canonicalPos = canonicalize(level, pos);
        return LOCKED_BLOCKS.get(new LockedBlockKey(level.dimension().identifier().toString(), canonicalPos));
    }

    public static boolean isLocked(Level level, BlockPos pos) {
        BlockPos canonicalPos = canonicalize(level, pos);
        return LOCKED_BLOCKS.containsKey(new LockedBlockKey(level.dimension().identifier().toString(), canonicalPos));
    }

    private static BlockPos canonicalize(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        }
        return pos;
    }

    private record LockedBlockKey(String dimensionId, BlockPos pos) {
    }
}
