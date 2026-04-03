package com.angellight.magicgame.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Mutable mana state for a single player in the current prototype.
 */
public final class ManaProfile {
    public static final int DEFAULT_MAX_MANA = 100;
    public static final int DEFAULT_REGEN_PER_SECOND = 2;
    public static final Codec<ManaProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("current_mana").forGetter(ManaProfile::currentMana),
            Codec.INT.optionalFieldOf("max_mana", DEFAULT_MAX_MANA).forGetter(ManaProfile::maxMana),
            Codec.INT.optionalFieldOf("regen_per_second", DEFAULT_REGEN_PER_SECOND).forGetter(ManaProfile::regenPerSecond)
    ).apply(instance, ManaProfile::new));

    private int currentMana;
    private final int maxMana;
    private final int regenPerSecond;

    public ManaProfile(int currentMana, int maxMana, int regenPerSecond) {
        if (maxMana <= 0) {
            throw new IllegalArgumentException("Max mana must be positive.");
        }
        if (regenPerSecond < 0) {
            throw new IllegalArgumentException("Mana regeneration must not be negative.");
        }
        this.maxMana = maxMana;
        this.regenPerSecond = regenPerSecond;
        this.currentMana = Math.max(0, Math.min(currentMana, maxMana));
    }

    public static ManaProfile createDefault() {
        return new ManaProfile(DEFAULT_MAX_MANA, DEFAULT_MAX_MANA, DEFAULT_REGEN_PER_SECOND);
    }

    public int currentMana() {
        return currentMana;
    }

    public int maxMana() {
        return maxMana;
    }

    public int regenPerSecond() {
        return regenPerSecond;
    }

    public boolean trySpend(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Mana spend must not be negative.");
        }
        if (currentMana < amount) {
            return false;
        }
        currentMana -= amount;
        return true;
    }

    public void regenerate() {
        restore(regenPerSecond);
    }

    public void restore(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Mana restore must not be negative.");
        }
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    public void restoreToFull() {
        currentMana = maxMana;
    }

    public void setCurrentMana(int amount) {
        currentMana = Math.max(0, Math.min(amount, maxMana));
    }
}