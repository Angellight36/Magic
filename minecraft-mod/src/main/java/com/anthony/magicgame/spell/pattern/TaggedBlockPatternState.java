package com.anthony.magicgame.spell.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Persistent record describing a magical pattern tag attached to a world block.
 *
 * @param tag attached pattern tag
 * @param dimensionId serialized dimension identifier
 * @param x block x position
 * @param y block y position
 * @param z block z position
 * @param openState captured open-state snapshot when the tag was applied
 * @param poweredState captured powered-state snapshot when the tag was applied
 * @param extendedState captured extended-state snapshot when the tag was applied
 */
public record TaggedBlockPatternState(
        BlockPatternTag tag,
        String dimensionId,
        int x,
        int y,
        int z,
        Boolean openState,
        Boolean poweredState,
        Boolean extendedState
) {
    public static final Codec<TaggedBlockPatternState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tag").xmap(BlockPatternTag::valueOf, BlockPatternTag::name).forGetter(TaggedBlockPatternState::tag),
            Codec.STRING.fieldOf("dimension_id").forGetter(TaggedBlockPatternState::dimensionId),
            Codec.INT.fieldOf("x").forGetter(TaggedBlockPatternState::x),
            Codec.INT.fieldOf("y").forGetter(TaggedBlockPatternState::y),
            Codec.INT.fieldOf("z").forGetter(TaggedBlockPatternState::z),
            Codec.BOOL.optionalFieldOf("open_state").forGetter(state -> Optional.ofNullable(state.openState())),
            Codec.BOOL.optionalFieldOf("powered_state").forGetter(state -> Optional.ofNullable(state.poweredState())),
            Codec.BOOL.optionalFieldOf("extended_state").forGetter(state -> Optional.ofNullable(state.extendedState()))
    ).apply(instance, (tag, dimensionId, x, y, z, openState, poweredState, extendedState) -> new TaggedBlockPatternState(
            tag,
            dimensionId,
            x,
            y,
            z,
            openState.orElse(null),
            poweredState.orElse(null),
            extendedState.orElse(null)
    )));

    public static TaggedBlockPatternState from(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return new TaggedBlockPatternState(
                tag,
                level.dimension().identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                state.hasProperty(BlockStateProperties.OPEN) ? state.getValue(BlockStateProperties.OPEN) : null,
                state.hasProperty(BlockStateProperties.POWERED) ? state.getValue(BlockStateProperties.POWERED) : null,
                state.hasProperty(BlockStateProperties.EXTENDED) ? state.getValue(BlockStateProperties.EXTENDED) : null
        );
    }

    public boolean matches(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        return this.tag == tag
                && dimensionId.equals(level.dimension().identifier().toString())
                && x == pos.getX()
                && y == pos.getY()
                && z == pos.getZ();
    }
}
