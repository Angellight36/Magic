package com.anthony.magicgame.spell.pattern;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the structural block support rules for persistent block pattern tags.
 */
class PatternTaggedBlocksTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void doorLikeBlocksSupportMagicLockTag() {
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.OAK_DOOR.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.OAK_TRAPDOOR.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.OAK_FENCE_GATE.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
    }

    @Test
    void poweredAndExtendedBlocksSupportMagicLockTag() {
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.LEVER.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.PISTON.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
    }

    @Test
    void containersSupportMagicLockTag() {
        assertTrue(PatternTaggedBlocks.supportsTagState(
                Blocks.CHEST.defaultBlockState(),
                true,
                BlockPatternTag.MAGIC_LOCKED
        ));
    }

    @Test
    void simplePlantsDoNotSupportMagicLockTag() {
        assertFalse(PatternTaggedBlocks.supportsTagState(
                Blocks.OAK_SAPLING.defaultBlockState(),
                false,
                BlockPatternTag.MAGIC_LOCKED
        ));
    }
}
