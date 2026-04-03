package com.angellight.magicgame.spell.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the narrowed occupant filter used by alert wards.
 */
class WardTrackingRulesTest {
    @Test
    void tracksLivingNonOwnerOccupants() {
        assertTrue(WardTrackingRules.shouldTrackOccupant(new WardTrackingRules.WardOccupantCandidate(
                false,
                true,
                false,
                false
        )));
    }

    @Test
    void ignoresRemovedNonLivingOwnerAndSpectatorOccupants() {
        assertFalse(WardTrackingRules.shouldTrackOccupant(new WardTrackingRules.WardOccupantCandidate(
                true,
                true,
                false,
                false
        )));
        assertFalse(WardTrackingRules.shouldTrackOccupant(new WardTrackingRules.WardOccupantCandidate(
                false,
                false,
                false,
                false
        )));
        assertFalse(WardTrackingRules.shouldTrackOccupant(new WardTrackingRules.WardOccupantCandidate(
                false,
                true,
                false,
                true
        )));
        assertFalse(WardTrackingRules.shouldTrackOccupant(new WardTrackingRules.WardOccupantCandidate(
                false,
                true,
                true,
                false
        )));
    }
}
