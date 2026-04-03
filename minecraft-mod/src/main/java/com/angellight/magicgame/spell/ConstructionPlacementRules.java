package com.angellight.magicgame.spell;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Small rule helpers for prototype construction spells so terrain-shaping behavior can be tested separately from command wiring.
 */
public final class ConstructionPlacementRules {
    private ConstructionPlacementRules() {
    }

    /**
     * Returns whether a targeted block should be replaced directly when shaping a path.
     *
     * @param state targeted block state
     * @param hasBlockEntity true when the targeted block is backed by a block entity
     * @return true when the path should replace the targeted surface block instead of floating above it
     */
    public static boolean shouldReplaceTargetedSurface(BlockState state, boolean hasBlockEntity) {
        return !hasBlockEntity && !state.canBeReplaced() && state.blocksMotion();
    }

    /**
     * Returns whether a path segment can be placed at the current block position.
     *
     * @param state current block state at the target position
     * @param hasBlockEntity true when the current block is backed by a block entity
     * @param belowState supporting block state below the target position
     * @return true when the position should accept a path segment
     */
    public static boolean canPlacePathSegment(BlockState state, boolean hasBlockEntity, BlockState belowState) {
        if (hasBlockEntity) {
            return false;
        }
        if (state.canBeReplaced()) {
            return belowState.blocksMotion();
        }
        return state.blocksMotion();
    }
}
