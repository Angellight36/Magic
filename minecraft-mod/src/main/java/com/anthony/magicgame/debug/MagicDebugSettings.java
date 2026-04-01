package com.anthony.magicgame.debug;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stores world-scoped debug flags so temporary prototype visuals can be toggled without code edits.
 */
public final class MagicDebugSettings extends SavedData {
    private static final String DATA_ID = "magicgame_debug_settings";
    private static final Codec<MagicDebugSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(MagicDebugSettings::enabled),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("feature_states", Map.of())
                    .forGetter(MagicDebugSettings::serializedFeatureStates)
    ).apply(instance, MagicDebugSettings::fromSerialized));
    private static final SavedDataType<MagicDebugSettings> TYPE = new SavedDataType<>(
            DATA_ID,
            MagicDebugSettings::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private boolean enabled;
    private final EnumMap<MagicDebugFeature, Boolean> featureStates = new EnumMap<>(MagicDebugFeature.class);

    public static MagicDebugSettings get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setDirty();
    }

    public boolean isFeatureActive(MagicDebugFeature feature) {
        return enabled && isFeatureConfiguredEnabled(feature);
    }

    public boolean isFeatureConfiguredEnabled(MagicDebugFeature feature) {
        return featureStates.getOrDefault(feature, true);
    }

    public void setFeatureEnabled(MagicDebugFeature feature, boolean enabled) {
        featureStates.put(feature, enabled);
        setDirty();
    }

    private Map<String, Boolean> serializedFeatureStates() {
        Map<String, Boolean> serialized = new HashMap<>();
        for (Map.Entry<MagicDebugFeature, Boolean> entry : featureStates.entrySet()) {
            serialized.put(entry.getKey().id(), entry.getValue());
        }
        return serialized;
    }

    private static MagicDebugSettings fromSerialized(boolean enabled, Map<String, Boolean> featureStates) {
        MagicDebugSettings settings = new MagicDebugSettings();
        settings.enabled = enabled;
        for (Map.Entry<String, Boolean> entry : featureStates.entrySet()) {
            settings.featureStates.put(MagicDebugFeature.require(entry.getKey()), entry.getValue());
        }
        return settings;
    }
}
