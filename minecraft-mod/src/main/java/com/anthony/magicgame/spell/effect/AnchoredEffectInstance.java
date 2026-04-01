package com.anthony.magicgame.spell.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Mutable world-persistent anchored spell effect instance.
 */
public final class AnchoredEffectInstance {
    public static final Codec<AnchoredEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(AnchoredEffectInstance::id),
            Codec.STRING.fieldOf("kind").xmap(AnchoredEffectKind::valueOf, AnchoredEffectKind::name).forGetter(AnchoredEffectInstance::kind),
            Codec.STRING.fieldOf("owner_id").forGetter(AnchoredEffectInstance::ownerId),
            Codec.STRING.fieldOf("spell_id").forGetter(AnchoredEffectInstance::spellId),
            Codec.STRING.fieldOf("dimension_id").forGetter(AnchoredEffectInstance::dimensionId),
            Codec.INT.fieldOf("x").forGetter(AnchoredEffectInstance::x),
            Codec.INT.fieldOf("y").forGetter(AnchoredEffectInstance::y),
            Codec.INT.fieldOf("z").forGetter(AnchoredEffectInstance::z),
            Codec.INT.fieldOf("radius").forGetter(AnchoredEffectInstance::radius),
            Codec.INT.fieldOf("remaining_ticks").forGetter(AnchoredEffectInstance::remainingTicks),
            Codec.STRING.listOf().optionalFieldOf("players_inside", List.of()).forGetter(AnchoredEffectInstance::playersInside)
    ).apply(instance, AnchoredEffectInstance::new));

    private final String id;
    private final AnchoredEffectKind kind;
    private final String ownerId;
    private final String spellId;
    private final String dimensionId;
    private final int x;
    private final int y;
    private final int z;
    private final int radius;
    private int remainingTicks;
    private final Set<String> playersInside;

    public AnchoredEffectInstance(
            String id,
            AnchoredEffectKind kind,
            String ownerId,
            String spellId,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius,
            int remainingTicks,
            List<String> playersInside
    ) {
        this.id = id;
        this.kind = kind;
        this.ownerId = ownerId;
        this.spellId = spellId;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.remainingTicks = remainingTicks;
        this.playersInside = new HashSet<>(playersInside);
    }

    public static AnchoredEffectInstance create(
            AnchoredEffectKind kind,
            UUID ownerId,
            String spellId,
            String dimensionId,
            int x,
            int y,
            int z,
            int radius,
            int remainingTicks
    ) {
        return new AnchoredEffectInstance(
                UUID.randomUUID().toString(),
                kind,
                ownerId.toString(),
                spellId,
                dimensionId,
                x,
                y,
                z,
                radius,
                remainingTicks,
                List.of()
        );
    }

    public String id() {
        return id;
    }

    public AnchoredEffectKind kind() {
        return kind;
    }

    public String ownerId() {
        return ownerId;
    }

    public String spellId() {
        return spellId;
    }

    public String dimensionId() {
        return dimensionId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int radius() {
        return radius;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public List<String> playersInside() {
        return List.copyOf(playersInside);
    }

    public int remainingSeconds() {
        return Math.max(0, remainingTicks / 20);
    }

    public boolean ownerMatches(UUID playerId) {
        return ownerId.equals(playerId.toString());
    }

    public boolean matchesDimension(String dimension) {
        return dimensionId.equals(dimension);
    }

    public boolean contains(double px, double py, double pz) {
        double dx = px - (x + 0.5D);
        double dy = py - (y + 0.5D);
        double dz = pz - (z + 0.5D);
        return (dx * dx) + (dy * dy) + (dz * dz) <= (double) radius * (double) radius;
    }

    public void tickSecond() {
        remainingTicks = Math.max(0, remainingTicks - 20);
    }

    public boolean isExpired() {
        return remainingTicks <= 0;
    }

    public Set<String> updateOccupants(Collection<String> currentPlayers) {
        Set<String> current = new HashSet<>(currentPlayers);
        Set<String> entrants = new HashSet<>(current);
        entrants.removeAll(playersInside);
        playersInside.clear();
        playersInside.addAll(current);
        return entrants;
    }
}