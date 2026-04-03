package com.angellight.magicgame.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the default behavior of the debug toggle system.
 */
class MagicDebugSettingsTest {
    @Test
    void featuresNeedGlobalDebugEvenWhenConfiguredOn() {
        MagicDebugSettings settings = new MagicDebugSettings();

        assertFalse(settings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES));
        settings.setFeatureEnabled(MagicDebugFeature.WARD_MESSAGES, true);
        assertFalse(settings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES));
        settings.setEnabled(true);
        assertTrue(settings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES));
    }

    @Test
    void featureFlagsCanDisableSingleDebugChannel() {
        MagicDebugSettings settings = new MagicDebugSettings();
        settings.setEnabled(true);
        settings.setFeatureEnabled(MagicDebugFeature.FIREBALL_VISUALS, false);

        assertFalse(settings.isFeatureActive(MagicDebugFeature.FIREBALL_VISUALS));
        assertTrue(settings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES));
    }
}
