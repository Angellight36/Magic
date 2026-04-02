package com.anthony.magicgame.item;

import com.anthony.magicgame.spell.pattern.LockKeying;
import com.anthony.magicgame.spell.pattern.LockedBlockManager;
import com.anthony.magicgame.spell.pattern.PatternTaggedBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Prototype physical lock item that applies a keyed lock to entryways and container-style targets.
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

        BlockPos pos = PatternTaggedBlocks.canonicalize(level, context.getClickedPos());
        if (!PatternTaggedBlocks.supportsPhysicalLock(level, pos)) {
            player.sendSystemMessage(Component.literal(
                    "Physical locks currently only fit entryways and chest-like containers."
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

        String keySignature = LinkedKeyItem.findAnyLinkedSignature(player, context.getHand());
        boolean mintedNewKey = keySignature == null;
        if (mintedNewKey) {
            keySignature = UUID.randomUUID().toString();
        }

        boolean changed = lockManager.lockWithKey(level, pos, keySignature);
        if (!changed) {
            return InteractionResult.FAIL;
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.9F, 0.85F);
        if (mintedNewKey) {
            ItemStack newKey = LinkedKeyItem.createKeyStack(keySignature);
            if (!player.getInventory().add(newKey)) {
                player.drop(newKey, false);
            }
        }
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        player.sendSystemMessage(Component.literal(
                mintedNewKey
                        ? "Applied a keyed physical lock and minted linked key " + LockKeying.displaySignature(keySignature) + "."
                        : "Applied a keyed physical lock attuned to linked key " + LockKeying.displaySignature(keySignature) + "."
        ));
        return InteractionResult.SUCCESS;
    }
}
