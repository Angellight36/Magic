package com.anthony.magicgame.spell.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Helpers for blocks that can participate in persistent magical pattern tags.
 */
public final class PatternTaggedBlocks {
    private PatternTaggedBlocks() {
    }

    public static boolean supportsTag(ServerLevel level, BlockPos pos, BlockPatternTag tag) {
        BlockPos canonicalPos = canonicalize(level, pos);
        BlockState state = level.getBlockState(canonicalPos);
        boolean hasTaggableContainer = level.getBlockEntity(canonicalPos) instanceof Container;
        return supportsTagState(state, hasTaggableContainer, tag);
    }

    public static boolean supportsTagState(BlockState state, boolean hasTaggableContainer, BlockPatternTag tag) {
        return switch (tag) {
            case MAGIC_LOCKED -> state.hasProperty(BlockStateProperties.OPEN)
                    || state.hasProperty(BlockStateProperties.POWERED)
                    || state.hasProperty(BlockStateProperties.EXTENDED)
                    || hasTaggableContainer;
        };
    }

    public static BlockPos canonicalize(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        }
        return pos;
    }

    public static boolean enforceTag(ServerLevel level, BlockPos pos, TaggedBlockPatternState tagState) {
        return switch (tagState.tag()) {
            case MAGIC_LOCKED -> enforceMagicLockedState(level, canonicalize(level, pos), tagState);
        };
    }

    private static boolean enforceMagicLockedState(ServerLevel level, BlockPos pos, TaggedBlockPatternState tagState) {
        BlockState state = level.getBlockState(pos);
        boolean changed = false;

        if (tagState.openState() != null && state.hasProperty(BlockStateProperties.OPEN)) {
            changed |= applyBooleanProperty(level, pos, BlockStateProperties.OPEN, tagState.openState());
        }
        if (tagState.poweredState() != null && state.hasProperty(BlockStateProperties.POWERED)) {
            changed |= applyBooleanProperty(level, pos, BlockStateProperties.POWERED, tagState.poweredState());
        }
        if (tagState.extendedState() != null && state.hasProperty(BlockStateProperties.EXTENDED)) {
            BlockState current = level.getBlockState(pos);
            if (current.getValue(BlockStateProperties.EXTENDED) != tagState.extendedState()) {
                level.setBlock(pos, current.setValue(BlockStateProperties.EXTENDED, tagState.extendedState()), 3);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean applyBooleanProperty(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.level.block.state.properties.BooleanProperty property,
            boolean expectedValue
    ) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(property)) {
            return false;
        }

        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockPos lowerPos = canonicalize(level, pos);
            BlockPos upperPos = lowerPos.above();
            BlockState lowerState = level.getBlockState(lowerPos);
            BlockState upperState = level.getBlockState(upperPos);
            boolean changed = false;
            if (lowerState.hasProperty(property) && lowerState.getValue(property) != expectedValue) {
                level.setBlock(lowerPos, lowerState.setValue(property, expectedValue), 3);
                changed = true;
            }
            if (upperState.getBlock() == lowerState.getBlock()
                    && upperState.hasProperty(property)
                    && upperState.getValue(property) != expectedValue) {
                level.setBlock(upperPos, upperState.setValue(property, expectedValue), 3);
                changed = true;
            }
            return changed;
        }

        if (state.getValue(property) == expectedValue) {
            return false;
        }
        level.setBlock(pos, state.setValue(property, expectedValue), 3);
        return true;
    }
}
