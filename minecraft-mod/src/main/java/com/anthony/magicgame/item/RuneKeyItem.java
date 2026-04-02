package com.anthony.magicgame.item;

import com.anthony.magicgame.spell.pattern.LockKeying;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * A prototype attunable key that can open keyed magical or physical locks.
 */
public class RuneKeyItem extends Item {
    private static final String KEY_SIGNATURE_TAG = "magicgame_key_signature";

    public RuneKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (getSignature(stack) != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "Rune key attuned to " + LockKeying.displaySignature(getSignature(stack)) + "."
                ));
            }
            return InteractionResult.SUCCESS;
        }

        String signature = UUID.randomUUID().toString();
        setSignature(stack, signature);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "Rune key attuned to " + LockKeying.displaySignature(signature) + "."
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public static String getSignature(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains(KEY_SIGNATURE_TAG) ? tag.getString(KEY_SIGNATURE_TAG).orElse(null) : null;
    }

    public static void setSignature(ItemStack stack, String signature) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(KEY_SIGNATURE_TAG, signature);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(
                "Rune Key [" + LockKeying.displaySignature(signature) + "]"
        ).withStyle(ChatFormatting.AQUA));
    }

    public static String findMatchingSignature(Player player, String requiredSignature) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(MagicItems.RUNE_KEY)) {
                continue;
            }
            String signature = getSignature(stack);
            if (LockKeying.matches(requiredSignature, signature)) {
                return signature;
            }
        }
        return null;
    }

    public static String findAnyAttunedSignature(Player player, InteractionHand preferredExcludedHand) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (hand == preferredExcludedHand) {
                continue;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(MagicItems.RUNE_KEY)) {
                continue;
            }
            String signature = getSignature(stack);
            if (signature != null) {
                return signature;
            }
        }
        return null;
    }
}
