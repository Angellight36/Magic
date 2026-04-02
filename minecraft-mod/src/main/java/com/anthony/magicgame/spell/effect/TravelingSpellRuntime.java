package com.anthony.magicgame.spell.effect;

import com.anthony.magicgame.spell.SpellChain;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Shared runtime helpers for traveling prototype spells so fire and force effects use owned collision and tuning rules.
 */
public final class TravelingSpellRuntime {
    private TravelingSpellRuntime() {
    }

    static SpellTravelProfile fireProfile(SpellChain spell) {
        float directDamage = 6.0F;
        float splashDamage = 3.0F;
        double splashRadius = 2.0D;
        int igniteTicks = 80;

        if (hasGlyph(spell, "gather")) {
            directDamage += 1.0F;
            splashDamage += 0.5F;
        }
        if (hasGlyph(spell, "shape")) {
            splashRadius += 0.4D;
        }
        if (hasGlyph(spell, "refine")) {
            directDamage += 1.0F;
        }
        if (hasGlyph(spell, "release")) {
            igniteTicks += 20;
        }

        return new SpellTravelProfile(18.0D, directDamage, splashDamage, splashRadius, 0.0D, 0.0D, igniteTicks);
    }

    static SpellTravelProfile forceProfile(SpellChain spell) {
        float directDamage = hasGlyph(spell, "gather") ? 5.0F : 4.0F;
        double knockback = hasGlyph(spell, "shape") ? 1.8D : 1.4D;
        double verticalLift = hasGlyph(spell, "raise") ? 0.45D : 0.35D;
        double range = hasGlyph(spell, "forward") ? 14.0D : 10.0D;

        return new SpellTravelProfile(range, directDamage, 0.0F, 0.0D, knockback, verticalLift, 0);
    }

    static TravelingSpellImpact traceImpact(ServerPlayer player, ServerLevel level, double maxDistance) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 fallbackImpact = origin.add(look.scale(maxDistance));

        BlockHitResult blockHit = pickTargetedBlock(player, maxDistance);
        double blockDistance = blockHit == null ? Double.MAX_VALUE : origin.distanceTo(blockHit.getLocation());

        LivingEntity entityHit = findLivingTargetInLook(player, level, maxDistance);
        double entityDistance = entityHit == null
                ? Double.MAX_VALUE
                : origin.distanceTo(entityHit.getBoundingBox().getCenter());

        if (entityHit != null && entityDistance <= blockDistance + 0.2D) {
            return new TravelingSpellImpact(entityHit.getBoundingBox().getCenter(), entityHit, null);
        }
        if (blockHit != null) {
            return new TravelingSpellImpact(blockHit.getLocation(), null, blockHit.getBlockPos());
        }
        return new TravelingSpellImpact(fallbackImpact, null, BlockPos.containing(fallbackImpact));
    }

    private static BlockHitResult pickTargetedBlock(ServerPlayer player, double distance) {
        HitResult hit = player.pick(distance, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    private static LivingEntity findLivingTargetInLook(ServerPlayer player, ServerLevel level, double maxDistance) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity bestTarget = null;
        double bestAlong = maxDistance + 1.0D;
        double bestOffset = Double.MAX_VALUE;

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().expandTowards(look.scale(maxDistance)).inflate(1.5D),
                candidate -> candidate.isAlive() && candidate != player
        )) {
            Vec3 center = entity.getBoundingBox().getCenter();
            Vec3 toEntity = center.subtract(origin);
            double along = toEntity.dot(look);
            if (along < 0.0D || along > maxDistance) {
                continue;
            }

            Vec3 closestPoint = origin.add(look.scale(along));
            double offset = center.distanceTo(closestPoint);
            double allowedOffset = Math.max(1.0D, entity.getBbWidth());
            if (offset > allowedOffset) {
                continue;
            }

            if (along < bestAlong || (Math.abs(along - bestAlong) < 0.25D && offset < bestOffset)) {
                bestTarget = entity;
                bestAlong = along;
                bestOffset = offset;
            }
        }

        return bestTarget;
    }

    private static boolean hasGlyph(SpellChain spell, String glyphId) {
        return spell.glyphs().stream().anyMatch(glyph -> glyph.id().equals(glyphId));
    }
}
