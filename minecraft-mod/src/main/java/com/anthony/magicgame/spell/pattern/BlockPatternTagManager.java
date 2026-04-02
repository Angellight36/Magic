package com.anthony.magicgame.spell.pattern;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stores persistent magical pattern tags attached to blocks.
 */
public final class BlockPatternTagManager extends SavedData {
    private static final String DATA_ID = "magicgame_block_pattern_tags";
    private static final Codec<BlockPatternTagManager> CODEC = TaggedBlockPatternState.CODEC.listOf()
            .xmap(BlockPatternTagManager::fromSerialized, BlockPatternTagManager::toSerialized);
    private static final SavedDataType<BlockPatternTagManager> TYPE = new SavedDataType<>(
            DATA_ID,
            BlockPatternTagManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final List<TaggedBlockPatternState> tags = new ArrayList<>();

    public static BlockPatternTagManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean addTag(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        return addTag(tag, level, pos, null);
    }

    public boolean addTag(BlockPatternTag tag, ServerLevel level, BlockPos pos, String keySignature) {
        BlockPos canonicalPos = PatternTaggedBlocks.canonicalize(level, pos);
        if (hasTag(tag, level, canonicalPos)) {
            return false;
        }
        tags.add(TaggedBlockPatternState.from(tag, level, canonicalPos, keySignature));
        setDirty();
        return true;
    }

    public boolean removeTag(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = PatternTaggedBlocks.canonicalize(level, pos);
        boolean removed = tags.removeIf(state -> state.matches(tag, level, canonicalPos));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public boolean hasTag(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = PatternTaggedBlocks.canonicalize(level, pos);
        return tags.stream().anyMatch(state -> state.matches(tag, level, canonicalPos));
    }

    public TaggedBlockPatternState findTag(BlockPatternTag tag, ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = PatternTaggedBlocks.canonicalize(level, pos);
        return tags.stream()
                .filter(state -> state.matches(tag, level, canonicalPos))
                .findFirst()
                .orElse(null);
    }

    public EnumSet<BlockPatternTag> tagsAt(ServerLevel level, BlockPos pos) {
        BlockPos canonicalPos = PatternTaggedBlocks.canonicalize(level, pos);
        EnumSet<BlockPatternTag> presentTags = EnumSet.noneOf(BlockPatternTag.class);
        for (TaggedBlockPatternState state : tags) {
            if (state.dimensionId().equals(level.dimension().identifier().toString())
                    && state.x() == canonicalPos.getX()
                    && state.y() == canonicalPos.getY()
                    && state.z() == canonicalPos.getZ()) {
                presentTags.add(state.tag());
            }
        }
        return presentTags;
    }

    public int size() {
        return tags.size();
    }

    public void tick(MinecraftServer server) {
        boolean changed = false;
        Iterator<TaggedBlockPatternState> iterator = tags.iterator();
        while (iterator.hasNext()) {
            TaggedBlockPatternState state = iterator.next();
            ServerLevel level = findLevel(server, state.dimensionId());
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(state.x(), state.y(), state.z());
            if (!PatternTaggedBlocks.supportsTag(level, pos, state.tag())) {
                iterator.remove();
                changed = true;
                continue;
            }
            changed |= PatternTaggedBlocks.enforceTag(level, pos, state);
        }
        if (changed) {
            setDirty();
        }
    }

    private static BlockPatternTagManager fromSerialized(List<TaggedBlockPatternState> states) {
        BlockPatternTagManager manager = new BlockPatternTagManager();
        manager.tags.addAll(states);
        return manager;
    }

    private List<TaggedBlockPatternState> toSerialized() {
        return List.copyOf(tags);
    }

    private ServerLevel findLevel(MinecraftServer server, String dimensionId) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(dimensionId)) {
                return level;
            }
        }
        return null;
    }
}
