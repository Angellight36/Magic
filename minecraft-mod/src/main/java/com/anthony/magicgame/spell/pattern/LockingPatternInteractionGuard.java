package com.anthony.magicgame.spell.pattern;

import com.anthony.magicgame.item.RuneKeyItem;
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

            LockedBlockManager lockManager = LockedBlockManager.get(level.getServer());
            if (!lockManager.isLocked(level, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }

            String requiredSignature = lockManager.keySignature(level, hitResult.getBlockPos());
            if (requiredSignature != null) {
                String presentedSignature = RuneKeyItem.findMatchingSignature(serverPlayer, requiredSignature);
                if (LockKeying.matches(requiredSignature, presentedSignature)) {
                    lockManager.unlockWithKey(level, hitResult.getBlockPos(), presentedSignature);
                    serverPlayer.sendSystemMessage(Component.literal(
                            "The keyed lock yields to rune " + LockKeying.displaySignature(presentedSignature) + "."
                    ));
                    return InteractionResult.PASS;
                }
            }

            serverPlayer.sendSystemMessage(Component.literal("A locking pattern prevents the block from being altered by hand."));
            return InteractionResult.FAIL;
        });
    }
}
