package com.angellight.magicgame.debug;

import com.angellight.magicgame.spell.pattern.LockedBlockManager;
import com.angellight.magicgame.spell.pattern.PatternTaggedBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Renders a simple targeted particle view so we can see lock state while tuning entryway logic.
 */
public final class LockDebugTicker {
    private static final DustParticleOptions LOCKED_PARTICLE = new DustParticleOptions(0xFF2626, 1.0F);
    private static final DustParticleOptions UNLOCKED_PARTICLE = new DustParticleOptions(0x33FF33, 1.0F);

    private LockDebugTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MagicDebugSettings settings = MagicDebugSettings.get(server);
            if (!settings.isFeatureActive(MagicDebugFeature.LOCK_STATE_PARTICLES)) {
                return;
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                renderTargetLockState(player);
            }
        });
    }

    private static void renderTargetLockState(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || level.getGameTime() % 6L != 0L) {
            return;
        }

        HitResult hit = player.pick(8.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = PatternTaggedBlocks.canonicalize(level, blockHit.getBlockPos());
        if (!PatternTaggedBlocks.supportsPhysicalLock(level, pos)) {
            return;
        }

        boolean locked = LockedBlockManager.get(level.getServer()).isLocked(level, pos);
        DustParticleOptions particle = locked ? LOCKED_PARTICLE : UNLOCKED_PARTICLE;
        level.sendParticles(particle, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 1, 0.02D, 0.02D, 0.02D, 0.0D);
    }
}
