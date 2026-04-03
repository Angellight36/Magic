package com.angellight.magicgame.spell.effect;

record SpellTravelProfile(
        double range,
        float directDamage,
        float splashDamage,
        double splashRadius,
        double knockback,
        double verticalLift,
        int igniteTicks
) {
}
