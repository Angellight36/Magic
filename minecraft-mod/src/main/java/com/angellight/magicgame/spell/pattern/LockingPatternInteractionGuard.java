package com.angellight.magicgame.spell.pattern;

import com.angellight.magicgame.item.LinkedKeyItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Prevents manual interaction with blocks currently held by a magical locking pattern.
 */
public final class LockingPatternInteractionGuard {
    private LockingPatternInteractionGuard() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) {
                return InteractionResult.PASS;
            }

            BlockPos pos = canonicalize(world, hitResult.getBlockPos());
            boolean hasTaggableContainer = world.getBlockEntity(pos) instanceof net.minecraft.world.Container;
            if (!PatternTaggedBlocks.supportsTagState(world.getBlockState(pos), hasTaggableContainer, BlockPatternTag.MAGIC_LOCKED)) {
                return InteractionResult.PASS;
            }

            if (world.isClientSide()) {
                if (!LockedBlockClientCache.isLocked(world, pos)) {
                    return InteractionResult.PASS;
                }
                String requiredSignature = LockedBlockClientCache.keySignature(world, pos);
                String presentedSignature = requiredSignature == null ? null : LinkedKeyItem.findMatchingSignature(player, requiredSignature);
                return LockingPatternInteractionRules.shouldBlockClientInteraction(requiredSignature, presentedSignature)
                        ? InteractionResult.FAIL
                        : InteractionResult.PASS;
            }

            if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            LockedBlockManager lockManager = LockedBlockManager.get(level.getServer());
            if (!lockManager.isLocked(level, pos)) {
                return InteractionResult.PASS;
            }

            String requiredSignature = lockManager.keySignature(level, pos);
            if (requiredSignature != null) {
                String presentedSignature = LinkedKeyItem.findMatchingSignature(serverPlayer, requiredSignature);
                if (LockKeying.matches(requiredSignature, presentedSignature)) {
                    lockManager.unlockWithKey(level, pos, presentedSignature);
                    serverPlayer.sendSystemMessage(Component.literal(
                            "The keyed lock yields to linked key " + LockKeying.displaySignature(presentedSignature) + "."
                    ));
                    return InteractionResult.PASS;
                }
            }

            serverPlayer.sendSystemMessage(Component.literal("A locking pattern prevents the block from being altered by hand."));
            return InteractionResult.FAIL;
        });
    }

    private static BlockPos canonicalize(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        }
        return pos;
    }
}
