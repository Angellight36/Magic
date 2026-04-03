package com.angellight.magicgame.spell.effect;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the pure data behavior of anchored spell effects before full world tests exist.
 */
class AnchoredEffectInstanceTest {
    @Test
    void occupantTrackingOnlyReturnsNewEntrants() {
        AnchoredEffectInstance effect = AnchoredEffectInstance.create(
                AnchoredEffectKind.ALERT_WARD,
                UUID.randomUUID(),
                "alert_ward",
                "minecraft:overworld",
                0,
                64,
                0,
                6,
                200
        );

        Set<String> firstEntrants = effect.updateOccupants(List.of("a", "b"));
        Set<String> secondEntrants = effect.updateOccupants(List.of("b", "c"));

        assertEquals(Set.of("a", "b"), firstEntrants);
        assertEquals(Set.of("c"), secondEntrants);
    }

    @Test
    void tickingEventuallyExpiresTheEffect() {
        AnchoredEffectInstance effect = AnchoredEffectInstance.create(
                AnchoredEffectKind.ALERT_WARD,
                UUID.randomUUID(),
                "alert_ward",
                "minecraft:overworld",
                0,
                64,
                0,
                6,
                20
        );

        effect.tickSecond();

        assertTrue(effect.isExpired());
        assertEquals(0, effect.remainingTicks());
    }
}