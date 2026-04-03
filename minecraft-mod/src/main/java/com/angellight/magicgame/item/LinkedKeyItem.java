package com.angellight.magicgame.item;

import com.angellight.magicgame.spell.pattern.LockKeying;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Carries a persistent lock signature so multiple blocks and copied keys can share one physical key identity.
 */
public class LinkedKeyItem extends Item {
    private static final String KEY_SIGNATURE_TAG = "magicgame_key_signature";

    public LinkedKeyItem(Properties properties) {
        super(properties);
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
                "Linked Key [" + LockKeying.displaySignature(signature) + "]"
        ).withStyle(ChatFormatting.AQUA));
    }

    public static ItemStack createKeyStack(String signature) {
        ItemStack stack = new ItemStack(MagicItems.LINKED_KEY);
        setSignature(stack, signature);
        return stack;
    }

    public static String findMatchingSignature(Player player, String requiredSignature) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(MagicItems.LINKED_KEY)) {
                continue;
            }
            String signature = getSignature(stack);
            if (LockKeying.matches(requiredSignature, signature)) {
                return signature;
            }
        }
        return null;
    }

    public static String findAnyLinkedSignature(Player player, InteractionHand preferredExcludedHand) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (hand == preferredExcludedHand) {
                continue;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(MagicItems.LINKED_KEY)) {
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
