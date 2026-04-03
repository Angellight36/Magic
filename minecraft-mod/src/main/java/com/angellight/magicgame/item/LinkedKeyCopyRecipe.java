package com.angellight.magicgame.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Copies a linked key's signature onto a fresh iron blank so multiple keys can share one lock signature.
 */
public final class LinkedKeyCopyRecipe extends CustomRecipe {
    public LinkedKeyCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findSourceKey(input) != ItemStack.EMPTY && countIronPieces(input) == 1 && countNonEmpty(input) == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack sourceKey = findSourceKey(input);
        if (sourceKey.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String signature = LinkedKeyItem.getSignature(sourceKey);
        if (signature == null) {
            return ItemStack.EMPTY;
        }
        return LinkedKeyItem.createKeyStack(signature);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(MagicItems.LINKED_KEY)) {
                remaining.set(slot, stack.copyWithCount(1));
                break;
            }
        }
        return remaining;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return MagicRecipeSerializers.LINKED_KEY_COPYING;
    }

    private static ItemStack findSourceKey(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!stack.is(MagicItems.LINKED_KEY)) {
                continue;
            }
            if (!found.isEmpty() || LinkedKeyItem.getSignature(stack) == null) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }

    private static int countIronPieces(CraftingInput input) {
        int count = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(Items.IRON_INGOT)) {
                count++;
            }
        }
        return count;
    }

    private static int countNonEmpty(CraftingInput input) {
        int count = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            if (!input.getItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
