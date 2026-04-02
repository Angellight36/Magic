package com.anthony.magicgame.item;

import com.anthony.magicgame.MagicGameMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Registers the prototype items used by the current magic systems.
 */
public final class MagicItems {
    public static final Item GLYPH_FOCUS = register("glyph_focus", new GlyphFocusItem(new Item.Properties().stacksTo(1)));
    public static final Item LINKED_KEY = register("linked_key", new LinkedKeyItem(new Item.Properties().stacksTo(1)));
    public static final Item PHYSICAL_LOCK = register("physical_lock", new PhysicalLockItem(new Item.Properties()));

    private MagicItems() {
    }

    public static void register() {
        MagicRecipeSerializers.register();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(GLYPH_FOCUS);
            entries.accept(LINKED_KEY);
            entries.accept(PHYSICAL_LOCK);
        });
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, id), item);
    }
}
