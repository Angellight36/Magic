package com.angellight.magicgame.item;

import com.angellight.magicgame.spell.GlyphDefinition;
import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.CustomData;
import java.util.function.Consumer;

/**
 * Current dev-build casting focus that unlocks the client-side composer and stores one written glyph chain per item.
 */
public final class GlyphFocusItem extends Item {
    private static final String STORED_CHAIN_TAG = "magicgame_focus_glyph_chain";

    public GlyphFocusItem(Properties properties) {
        super(properties);
    }

    /**
     * Returns the glyph ids currently written onto this focus.
     */
    public static List<String> getStoredGlyphs(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(STORED_CHAIN_TAG)) {
            return List.of();
        }
        String rawChain = tag.getString(STORED_CHAIN_TAG).orElse("");
        if (rawChain.isBlank()) {
            return List.of();
        }
        return List.of(rawChain.trim().split("\\s+")).stream()
                .filter(glyphId -> !glyphId.isBlank())
                .toList();
    }

    /**
     * Replaces the written glyph chain on this focus with the provided ordered glyph ids.
     */
    public static void setStoredGlyphs(ItemStack stack, List<String> glyphIds) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String serializedChain = glyphIds.stream()
                .filter(glyphId -> glyphId != null && !glyphId.isBlank())
                .collect(Collectors.joining(" "));
        if (serializedChain.isBlank()) {
            tag.remove(STORED_CHAIN_TAG);
        } else {
            tag.putString(STORED_CHAIN_TAG, serializedChain);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Removes any written glyph chain from this focus.
     */
    public static void clearStoredGlyphs(ItemStack stack) {
        setStoredGlyphs(stack, List.of());
    }

    /**
     * Formats the stored chain for tooltips and UI surfaces using glyph display names where available.
     */
    public static String storedChainText(ItemStack stack) {
        List<String> glyphIds = getStoredGlyphs(stack);
        if (glyphIds.isEmpty()) {
            return "(empty)";
        }
        return glyphIds.stream()
                .map(GlyphFocusItem::displayGlyphLabel)
                .collect(Collectors.joining(" -> "));
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
        tooltipAdder.accept(Component.literal("Press R to quick-cast the spell written on this focus."));
        tooltipAdder.accept(Component.literal("Stored spell: " + storedChainText(stack)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private static String displayGlyphLabel(String glyphId) {
        Optional<GlyphDefinition> glyph = CoreGlyphRegistry.find(glyphId);
        return glyph.map(GlyphDefinition::displayName).orElse(glyphId.replace('_', ' '));
    }
}
