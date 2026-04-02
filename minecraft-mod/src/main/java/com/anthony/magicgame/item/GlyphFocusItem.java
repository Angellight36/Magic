package com.anthony.magicgame.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

/**
 * Current dev-build casting focus that unlocks the client-side composer and quick-cast controls while held.
 */
public final class GlyphFocusItem extends Item {
    public GlyphFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag tooltipFlag
    ) {
        tooltipAdder.accept(Component.literal("Hold this focus and press G to compose a spell."));
        tooltipAdder.accept(Component.literal("Press R to quick-cast your last composed chain."));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
