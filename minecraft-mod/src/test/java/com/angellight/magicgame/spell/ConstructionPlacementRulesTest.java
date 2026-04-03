package com.angellight.magicgame.spell;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Covers the path-placement heuristics used by the current construction spell prototype.
 */
class ConstructionPlacementRulesTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void targetedGroundCanBeReplacedByPath() {
        assertTrue(ConstructionPlacementRules.shouldReplaceTargetedSurface(
                Blocks.GRASS_BLOCK.defaultBlockState(),
                false
        ));
        assertTrue(ConstructionPlacementRules.canPlacePathSegment(
                Blocks.DIRT.defaultBlockState(),
                false,
                Blocks.STONE.defaultBlockState()
        ));
    }

    @Test
    void pathCanFloatOnlyWhenAirHasSupportBelow() {
        assertTrue(ConstructionPlacementRules.canPlacePathSegment(
                Blocks.AIR.defaultBlockState(),
                false,
                Blocks.STONE.defaultBlockState()
        ));
        assertFalse(ConstructionPlacementRules.canPlacePathSegment(
                Blocks.AIR.defaultBlockState(),
                false,
                Blocks.AIR.defaultBlockState()
        ));
    }

    @Test
    void blockEntitiesAreNotReplacedByPathLogic() {
        assertFalse(ConstructionPlacementRules.shouldReplaceTargetedSurface(
                Blocks.CHEST.defaultBlockState(),
                true
        ));
        assertFalse(ConstructionPlacementRules.canPlacePathSegment(
                Blocks.CHEST.defaultBlockState(),
                true,
                Blocks.STONE.defaultBlockState()
        ));
    }
}
