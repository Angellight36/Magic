package com.angellight.magicgame.spell.effect;

import com.angellight.magicgame.spell.SpellChain;
import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the owned traveling spell tuning so fire and force casts do not silently fall back to vanilla behavior.
 */
class TravelingSpellRuntimeTest {
    @Test
    void fireProfileScalesDamageAndSplashFromItsShapingGlyphs() {
        SpellChain fireball = CoreGlyphRegistry.chain(
                "gather",
                "fire",
                "shape",
                "separate",
                "forward",
                "direct",
                "stabilize",
                "on_impact",
                "release"
        );

        SpellTravelProfile profile = TravelingSpellRuntime.fireProfile(fireball);

        assertTrue(profile.directDamage() >= 7.0F);
        assertTrue(profile.splashDamage() >= 3.5F);
        assertTrue(profile.splashRadius() > 2.0D);
        assertTrue(profile.igniteTicks() >= 100);
    }

    @Test
    void forceProfileKeepsASeparateKnockbackDrivenIdentity() {
        SpellChain forceBolt = CoreGlyphRegistry.chain(
                "gather",
                "force",
                "shape",
                "separate",
                "forward",
                "direct",
                "on_impact",
                "release"
        );

        SpellTravelProfile profile = TravelingSpellRuntime.forceProfile(forceBolt);

        assertTrue(profile.directDamage() >= 5.0F);
        assertTrue(profile.knockback() >= 1.8D);
        assertTrue(profile.verticalLift() >= 0.35D);
        assertTrue(profile.range() >= 14.0D);
    }
}
