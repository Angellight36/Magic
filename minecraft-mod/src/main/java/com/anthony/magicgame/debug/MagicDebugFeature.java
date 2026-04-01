package com.anthony.magicgame.debug;

import java.util.Arrays;
import java.util.List;

/**
 * Fine-grained debug features that can be toggled while the prototype is still using temporary visuals.
 */
public enum MagicDebugFeature {
    WARD_MESSAGES("ward_messages"),
    WARD_BOUNDARY_PARTICLES("ward_boundary_particles"),
    FIREBALL_VISUALS("fireball_visuals");

    private final String id;

    MagicDebugFeature(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(MagicDebugFeature::id).toList();
    }

    public static MagicDebugFeature require(String id) {
        return Arrays.stream(values())
                .filter(feature -> feature.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown debug feature: " + id));
    }
}
