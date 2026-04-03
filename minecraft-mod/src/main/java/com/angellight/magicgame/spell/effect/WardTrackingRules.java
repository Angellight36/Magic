package com.angellight.magicgame.spell.effect;

/**
 * Pure occupant filtering rules for ward triggers so the alert-ward prototype does not react to every nearby entity.
 */
public final class WardTrackingRules {
    private WardTrackingRules() {
    }

    public static boolean shouldTrackOccupant(WardOccupantCandidate candidate) {
        if (candidate.removed()) {
            return false;
        }
        if (!candidate.living()) {
            return false;
        }
        if (candidate.spectatorPlayer()) {
            return false;
        }
        return !candidate.owner();
    }

    /**
     * Minimal entity state needed to decide whether a ward should track an occupant.
     */
    public record WardOccupantCandidate(
            boolean removed,
            boolean living,
            boolean spectatorPlayer,
            boolean owner
    ) {
    }
}
