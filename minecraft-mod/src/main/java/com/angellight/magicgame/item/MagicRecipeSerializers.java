package com.angellight.magicgame.item;

import com.angellight.magicgame.MagicGameMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Registers the custom recipe serializers needed by prototype magic items.
 */
public final class MagicRecipeSerializers {
    public static final RecipeSerializer<LinkedKeyCopyRecipe> LINKED_KEY_COPYING = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "linked_key_copying"),
            new CustomRecipe.Serializer<>(LinkedKeyCopyRecipe::new)
    );

    private MagicRecipeSerializers() {
    }

    public static void register() {
        // Static registration is triggered by class loading.
    }
}
