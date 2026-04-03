package com.angellight.magicgame.item;

import com.angellight.magicgame.MagicGameMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import java.util.function.Function;

/**
 * Registers the prototype items used by the current magic systems.
 */
public final class MagicItems {
    public static final Item GLYPH_FOCUS = register("glyph_focus", properties -> new GlyphFocusItem(properties.stacksTo(1)));
    public static final Item LINKED_KEY = register("linked_key", properties -> new LinkedKeyItem(properties.stacksTo(1)));
    public static final Item PHYSICAL_LOCK = register("physical_lock", PhysicalLockItem::new);

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

    private static Item register(String id, Function<Item.Properties, Item> factory) {
        Identifier identifier = Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, id);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
        Item.Properties properties = new Item.Properties().setId(key);
        return Registry.register(BuiltInRegistries.ITEM, identifier, factory.apply(properties));
    }
}
