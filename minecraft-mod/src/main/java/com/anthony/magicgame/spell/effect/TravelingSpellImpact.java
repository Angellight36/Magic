package com.anthony.magicgame.spell.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

record TravelingSpellImpact(Vec3 impactPosition, LivingEntity entityHit, BlockPos blockPos) {
    AABB bounds(double radius) {
        return new AABB(
                impactPosition.x - radius,
                impactPosition.y - radius,
                impactPosition.z - radius,
                impactPosition.x + radius,
                impactPosition.y + radius,
                impactPosition.z + radius
        );
    }
}
