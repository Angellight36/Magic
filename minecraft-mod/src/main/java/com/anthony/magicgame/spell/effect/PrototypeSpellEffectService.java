package com.anthony.magicgame.spell.effect;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.anthony.magicgame.spell.ConstructionPlacementRules;
import com.anthony.magicgame.spell.PatternInteractionRules;
import com.anthony.magicgame.spell.SpellChain;
import com.anthony.magicgame.spell.SpellFlowRules;
import com.anthony.magicgame.spell.SpellResolutionPlan;
import com.anthony.magicgame.spell.SpellTargetingRules;
import com.anthony.magicgame.spell.pattern.LockedBlockManager;
import com.anthony.magicgame.spell.pattern.LockingPatternBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Executes the currently implemented prototype spell effects without relying on borrowed vanilla mechanics.
 */
public final class PrototypeSpellEffectService {
    private PrototypeSpellEffectService() {
    }

    public static void cast(ServerPlayer player, ServerLevel level, SpellChain spell, SpellResolutionPlan plan) {
        switch (plan.intent()) {
            case TRAVELING_EFFECT -> runTravelingEffect(player, level, spell);
            case RESTORATION_EFFECT -> runRestorationEffect(player, level, spell);
            case VITALITY_TRANSFER -> runVitalityTransfer(player, level, spell);
            case PATTERN_INTERACTION -> runPatternInteraction(player, level, spell);
            case CONSTRUCTION_EFFECT -> runConstructionEffect(player, level, spell);
            case BOUNDARY_WARD, UNKNOWN_UNSTABLE -> {
                // Anchored and unstable prototype effects do not cast directly here yet.
            }
        }
    }

    private static void runTravelingEffect(ServerPlayer player, ServerLevel level, SpellChain spell) {
        MagicDebugSettings debugSettings = MagicDebugSettings.get(player.level().getServer());
        boolean containsFire = hasGlyph(spell, "fire");
        boolean containsForce = hasGlyph(spell, "force");

        if (containsFire) {
            castFireTravelingEffect(player, level, spell, debugSettings);
            return;
        }

        if (containsForce) {
            castForceTravelingEffect(player, level, spell);
        }
    }

    private static void castFireTravelingEffect(
            ServerPlayer player,
            ServerLevel level,
            SpellChain spell,
            MagicDebugSettings debugSettings
    ) {
        SpellTravelProfile profile = TravelingSpellRuntime.fireProfile(spell);
        TravelingSpellImpact impact = TravelingSpellRuntime.traceImpact(player, level, profile.range());
        Vec3 launchOrigin = player.getEyePosition().add(player.getLookAngle().normalize().scale(0.65D));

        spawnCoreFireTravelParticles(level, launchOrigin, impact.impactPosition());
        if (debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_TRAIL_PARTICLES)) {
            spawnOptionalFireTrailParticles(level, launchOrigin, impact.impactPosition());
        }
        if (debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_LAUNCH_SOUND)) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.9F
            );
        }

        int directHits = 0;
        if (impact.entityHit() != null) {
            LivingEntity target = impact.entityHit();
            target.hurt(level.damageSources().magic(), profile.directDamage());
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), profile.igniteTicks()));
            directHits = 1;
        }

        int splashHits = 0;
        List<LivingEntity> splashTargets = level.getEntitiesOfClass(
                LivingEntity.class,
                impact.bounds(profile.splashRadius()),
                entity -> entity.isAlive()
                        && entity != player
                        && entity != impact.entityHit()
                        && entity.distanceToSqr(
                                impact.impactPosition().x,
                                impact.impactPosition().y,
                                impact.impactPosition().z
                        ) <= profile.splashRadius() * profile.splashRadius()
        );
        for (LivingEntity target : splashTargets) {
            target.hurt(level.damageSources().magic(), profile.splashDamage());
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), Math.max(20, profile.igniteTicks() / 2)));
            splashHits++;
        }

        spawnFireImpactParticles(level, impact.impactPosition(), profile.splashRadius());
        level.playSound(
                null,
                impact.impactPosition().x,
                impact.impactPosition().y,
                impact.impactPosition().z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                0.7F,
                1.2F
        );

        if (debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_VISUALS)) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Fire spell impacted at "
                            + formatPosition(impact.impactPosition())
                            + " | direct hits " + directHits
                            + " | splash hits " + splashHits + "."
            ));
        }
    }

    private static void castForceTravelingEffect(ServerPlayer player, ServerLevel level, SpellChain spell) {
        SpellTravelProfile profile = TravelingSpellRuntime.forceProfile(spell);
        TravelingSpellImpact impact = TravelingSpellRuntime.traceImpact(player, level, profile.range());
        Vec3 look = player.getLookAngle().normalize();

        if (impact.entityHit() != null) {
            LivingEntity target = impact.entityHit();
            target.hurt(level.damageSources().magic(), profile.directDamage());
            target.push(look.x * profile.knockback(), profile.verticalLift(), look.z * profile.knockback());
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    target.getX(),
                    target.getY(0.5D),
                    target.getZ(),
                    12,
                    0.3D,
                    0.4D,
                    0.3D,
                    0.03D
            );
            level.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(),
                    target.getY(0.5D),
                    target.getZ(),
                    8,
                    0.25D,
                    0.3D,
                    0.25D,
                    0.02D
            );
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.9F);
            player.sendSystemMessage(Component.literal(
                    "[debug] Force bolt struck " + target.getName().getString() + "."
            ));
            return;
        }

        spawnForcePulseParticles(level, impact.impactPosition());
        player.sendSystemMessage(Component.literal(
                "[debug] Force bolt discharged without a living target."
        ));
    }

    private static void runRestorationEffect(ServerPlayer player, ServerLevel level, SpellChain spell) {
        LivingEntity target = resolveRestorationTarget(player, level, spell);
        if (target == null) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Restoration spell found no clear living target."
            ));
            return;
        }

        float healAmount = 4.0F;
        if (hasGlyph(spell, "restore")) {
            healAmount += 1.0F;
        }
        if (hasGlyph(spell, "strengthen")) {
            healAmount += 1.0F;
        }
        if (hasGlyph(spell, "refine")) {
            healAmount += 1.0F;
        }

        target.heal(healAmount);
        if (hasGlyph(spell, "sustain") || hasGlyph(spell, "persist")) {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        }
        level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(0.9D), target.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.02D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY(0.7D), target.getZ(), 8, 0.2D, 0.25D, 0.2D, 0.02D);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.sendSystemMessage(Component.literal(
                "[debug] Restored " + target.getName().getString() + " for " + healAmount + " health."
        ));
    }

    private static void runVitalityTransfer(ServerPlayer player, ServerLevel level, SpellChain spell) {
        LivingEntity target = findLivingTargetInLook(player, level, 10.0D);
        if (target == null) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Vitality transfer needs a seen living target."
            ));
            return;
        }

        float selfCost = hasGlyph(spell, "self") || hasGlyph(spell, "caster") ? 4.0F : 2.0F;
        if (player.getHealth() <= selfCost + 1.0F) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Not enough health to sustain the vitality transfer safely."
            ));
            return;
        }

        player.hurtServer(level, level.damageSources().magic(), selfCost);
        float healAmount = selfCost + 3.0F;
        if (hasGlyph(spell, "refine")) {
            healAmount += 1.0F;
        }
        target.heal(healAmount);
        if (hasGlyph(spell, "strengthen")) {
            target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
        }

        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY(0.7D), player.getZ(), 6, 0.2D, 0.25D, 0.2D, 0.02D);
        level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY(0.9D), target.getZ(), 10, 0.25D, 0.35D, 0.25D, 0.03D);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.7F, 0.8F);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.sendSystemMessage(Component.literal(
                "[debug] Converted " + selfCost + " of your health into " + healAmount + " healing for " + target.getName().getString() + "."
        ));
    }

    private static void runPatternInteraction(ServerPlayer player, ServerLevel level, SpellChain spell) {
        BlockHitResult hit = pickTargetedBlock(player, 6.0D);
        if (hit == null) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Pattern interaction needs a targeted block."
            ));
            return;
        }

        BlockPos pos = LockingPatternBlocks.canonicalize(level, hit.getBlockPos());
        if (!LockingPatternBlocks.isLockable(level, pos)) {
            player.sendSystemMessage(Component.literal(
                    "[debug] Targeted block has no stateful or storable pattern this lock logic can currently bind."
            ));
            return;
        }

        LockedBlockManager lockManager = LockedBlockManager.get(level.getServer());
        PatternInteractionRules.PatternInteractionMode mode = PatternInteractionRules.classifyMode(spell);
        switch (mode) {
            case LOCK -> {
                boolean changed = lockManager.lock(level, pos);
                level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 12, 0.25D, 0.3D, 0.25D, 0.02D);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 0.75F);
                player.sendSystemMessage(Component.literal(
                        changed
                                ? "[debug] Bound a locking pattern onto the target."
                                : "[debug] The target is already held by a locking pattern."
                ));
            }
            case UNLOCK -> {
                boolean changed = lockManager.unlock(level, pos);
                if (!changed) {
                    player.sendSystemMessage(Component.literal(
                            "[debug] No active locking pattern was found on the target."
                    ));
                    return;
                }
                level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 12, 0.25D, 0.3D, 0.25D, 0.02D);
                level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, hasGlyph(spell, "gentle") ? 1.15F : 0.95F);
                player.sendSystemMessage(Component.literal(
                        "[debug] Reduced or removed the target's locking pattern."
                ));
            }
            case DISRUPT -> {
                boolean changed = lockManager.unlock(level, pos);
                level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 14, 0.25D, 0.3D, 0.25D, 0.04D);
                level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.9F, 0.75F);
                player.sendSystemMessage(Component.literal(
                        changed
                                ? "[debug] Disrupted the target's locking pattern."
                                : "[debug] The pattern discharge scattered across the target harmlessly."
                ));
            }
            case NONE -> player.sendSystemMessage(Component.literal(
                    "[debug] Pattern interaction found no clear locking behavior to resolve."
            ));
        }
    }

    private static void runConstructionEffect(ServerPlayer player, ServerLevel level, SpellChain spell) {
        if (hasGlyph(spell, "raise")) {
            int placed = placeRaisedStoneWall(player, level);
            player.sendSystemMessage(Component.literal(
                    placed > 0
                            ? "[debug] Raised a temporary stone wall footprint with " + placed + " block(s)."
                            : "[debug] Could not find enough open space to raise the wall."
            ));
            return;
        }

        int placed = placeStonePath(player, level);
        player.sendSystemMessage(Component.literal(
                placed > 0
                        ? "[debug] Shaped a stone path with " + placed + " block(s)."
                        : "[debug] Could not find enough open ground to shape the path."
        ));
    }

    private static void spawnCoreFireTravelParticles(ServerLevel level, Vec3 origin, Vec3 impactPosition) {
        Vec3 delta = impactPosition.subtract(origin);
        double distance = Math.max(0.5D, delta.length());
        int steps = Math.max(6, (int) Math.ceil(distance * 4.0D));
        for (int step = 1; step <= steps; step++) {
            double progress = step / (double) steps;
            Vec3 point = origin.add(delta.scale(progress));
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 2, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }

    private static void spawnOptionalFireTrailParticles(ServerLevel level, Vec3 origin, Vec3 impactPosition) {
        Vec3 delta = impactPosition.subtract(origin);
        double distance = Math.max(0.5D, delta.length());
        int steps = Math.max(4, (int) Math.ceil(distance * 2.0D));
        for (int step = 1; step <= steps; step++) {
            double progress = step / (double) steps;
            Vec3 point = origin.add(delta.scale(progress));
            level.sendParticles(ParticleTypes.SMOKE, point.x, point.y, point.z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
        }
    }

    private static void spawnFireImpactParticles(ServerLevel level, Vec3 impactPosition, double radius) {
        level.sendParticles(ParticleTypes.FLAME, impactPosition.x, impactPosition.y, impactPosition.z, 16, radius * 0.2D, 0.15D, radius * 0.2D, 0.02D);
        level.sendParticles(ParticleTypes.SMOKE, impactPosition.x, impactPosition.y, impactPosition.z, 10, radius * 0.15D, 0.12D, radius * 0.15D, 0.01D);
        level.sendParticles(ParticleTypes.LAVA, impactPosition.x, impactPosition.y, impactPosition.z, 4, 0.08D, 0.08D, 0.08D, 0.0D);
    }

    private static void spawnForcePulseParticles(ServerLevel level, Vec3 impactPosition) {
        level.sendParticles(ParticleTypes.CLOUD, impactPosition.x, impactPosition.y, impactPosition.z, 6, 0.12D, 0.12D, 0.12D, 0.01D);
        level.sendParticles(ParticleTypes.CRIT, impactPosition.x, impactPosition.y, impactPosition.z, 5, 0.1D, 0.1D, 0.1D, 0.01D);
    }

    private static LivingEntity resolveRestorationTarget(ServerPlayer player, ServerLevel level, SpellChain spell) {
        LivingEntity lookTarget = findLivingTargetInLook(player, level, 10.0D);
        SpellTargetingRules.RestorationTargetWeights weights = SpellFlowRules.restorationTargetWeights(
                SpellFlowRules.scoreRecipients(spell),
                lookTarget != null
        );
        if (weights.totalWeight() <= 0) {
            return null;
        }
        SpellTargetingRules.RestorationTargetChoice choice = SpellTargetingRules.chooseRestorationTarget(
                weights,
                player.getRandom().nextInt(weights.totalWeight())
        );
        return switch (choice) {
            case SELF -> player;
            case LOOK_TARGET -> lookTarget;
            case NONE -> null;
        };
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
                entity -> entity.isAlive() && entity != player
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

    private static BlockHitResult pickTargetedBlock(ServerPlayer player, double distance) {
        HitResult hit = player.pick(distance, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    private static int placeStonePath(ServerPlayer player, ServerLevel level) {
        BlockPos start = findConstructionStart(player, level);
        Direction forward = player.getDirection();
        int placed = 0;
        for (int step = 0; step < 4; step++) {
            BlockPos cursor = start.relative(forward, step);
            if (tryPlacePathBlock(level, cursor, Blocks.STONE_BRICKS.defaultBlockState())) {
                placed++;
            }
        }
        if (placed > 0) {
            level.sendParticles(ParticleTypes.CRIT, start.getX() + 0.5D, start.getY() + 0.2D, start.getZ() + 0.5D, 12, 0.4D, 0.1D, 0.4D, 0.02D);
            level.playSound(null, start, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.95F);
        }
        return placed;
    }

    private static int placeRaisedStoneWall(ServerPlayer player, ServerLevel level) {
        BlockPos start = findConstructionStart(player, level);
        Direction side = player.getDirection().getClockWise();
        int placed = 0;
        for (int width = -1; width <= 1; width++) {
            for (int height = 0; height < 2; height++) {
                BlockPos cursor = start.relative(side, width).above(height);
                if (tryPlaceConstructBlock(level, cursor, Blocks.COBBLESTONE.defaultBlockState())) {
                    placed++;
                }
            }
        }
        if (placed > 0) {
            level.sendParticles(ParticleTypes.CLOUD, start.getX() + 0.5D, start.getY() + 0.8D, start.getZ() + 0.5D, 14, 0.6D, 0.5D, 0.6D, 0.02D);
            level.playSound(null, start, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        return placed;
    }

    private static BlockPos findConstructionStart(ServerPlayer player, ServerLevel level) {
        BlockHitResult hit = pickTargetedBlock(player, 8.0D);
        if (hit != null) {
            BlockPos pos = hit.getBlockPos();
            BlockState hitState = level.getBlockState(pos);
            if (ConstructionPlacementRules.shouldReplaceTargetedSurface(hitState, level.getBlockEntity(pos) != null)) {
                return pos;
            }
            return hitState.canBeReplaced() ? pos : pos.above();
        }
        return player.blockPosition().relative(player.getDirection());
    }

    private static boolean tryPlacePathBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState existing = level.getBlockState(pos);
        if (!ConstructionPlacementRules.canPlacePathSegment(existing, level.getBlockEntity(pos) != null, level.getBlockState(pos.below()))) {
            return false;
        }
        return level.setBlock(pos, state, 3);
    }

    private static boolean tryPlaceConstructBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.canBeReplaced()) {
            return false;
        }
        if (!level.getBlockState(pos.below()).blocksMotion()) {
            return false;
        }
        return level.setBlock(pos, state, 3);
    }

    private static boolean hasGlyph(SpellChain spell, String glyphId) {
        return spell.glyphs().stream().anyMatch(glyph -> glyph.id().equals(glyphId));
    }

    private static String formatPosition(Vec3 position) {
        return Math.round(position.x * 10.0D) / 10.0D + ", "
                + Math.round(position.y * 10.0D) / 10.0D + ", "
                + Math.round(position.z * 10.0D) / 10.0D;
    }
}
