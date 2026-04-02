package com.anthony.magicgame.spell.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Persistent record describing a block position currently held by a magical locking pattern.
 *
 * @param dimensionId serialized dimension identifier
 * @param x block x position
 * @param y block y position
 * @param z block z position
 */
public record LockedBlockState(
        String dimensionId,
        int x,
        int y,
        int z
) {
    public static final Codec<LockedBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension_id").forGetter(LockedBlockState::dimensionId),
            Codec.INT.fieldOf("x").forGetter(LockedBlockState::x),
            Codec.INT.fieldOf("y").forGetter(LockedBlockState::y),
            Codec.INT.fieldOf("z").forGetter(LockedBlockState::z)
    ).apply(instance, LockedBlockState::new));

    public static LockedBlockState from(ServerLevel level, BlockPos pos) {
        return new LockedBlockState(level.dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean matches(ServerLevel level, BlockPos pos) {
        return dimensionId.equals(level.dimension().identifier().toString())
                && x == pos.getX()
                && y == pos.getY()
                && z == pos.getZ();
    }
}
