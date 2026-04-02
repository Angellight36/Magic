package com.anthony.magicgame.spell.pattern;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * Prevents manual interaction with blocks currently held by a magical locking pattern.
 */
public final class LockingPatternInteractionGuard {
    private LockingPatternInteractionGuard() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || !(world instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            if (serverPlayer.isSpectator()) {
                return InteractionResult.PASS;
            }

            if (!LockingPatternBlocks.isLockable(level, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }

            if (!LockedBlockManager.get(level.getServer()).isLocked(level, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }

            serverPlayer.sendSystemMessage(Component.literal("A locking pattern prevents the block from being altered by hand."));
            return InteractionResult.FAIL;
        });
    }
}
