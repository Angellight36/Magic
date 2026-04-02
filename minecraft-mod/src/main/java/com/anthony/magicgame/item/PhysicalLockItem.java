package com.anthony.magicgame.item;

import com.anthony.magicgame.spell.pattern.LockKeying;
import com.anthony.magicgame.spell.pattern.LockedBlockManager;
import com.anthony.magicgame.spell.pattern.LockingPatternBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Prototype physical lock item that applies the same persistent locked-state used by spell locks.
 */
public class PhysicalLockItem extends Item {
    public PhysicalLockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = LockingPatternBlocks.canonicalize(level, context.getClickedPos());
        if (!LockingPatternBlocks.isLockable(level, pos)) {
            player.sendSystemMessage(Component.literal(
                    "That block has no lockable pattern for a physical lock to grip."
            ));
            return InteractionResult.FAIL;
        }

        LockedBlockManager lockManager = LockedBlockManager.get(level.getServer());
        if (lockManager.isLocked(level, pos)) {
            player.sendSystemMessage(Component.literal(
                    "The target is already held by a locking pattern."
            ));
            return InteractionResult.FAIL;
        }

        String keySignature = RuneKeyItem.findAnyAttunedSignature(player, context.getHand());
        boolean changed = keySignature == null
                ? lockManager.lock(level, pos)
                : lockManager.lockWithKey(level, pos, keySignature);
        if (!changed) {
            return InteractionResult.FAIL;
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.9F, 0.85F);
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        player.sendSystemMessage(Component.literal(
                keySignature == null
                        ? "Applied a physical lock to the target."
                        : "Applied a keyed physical lock attuned to " + LockKeying.displaySignature(keySignature) + "."
        ));
        return InteractionResult.SUCCESS;
    }
}
