package com.anthony.magicgame.spell.effect;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stores world-persistent anchored spell effects and provides tick/update helpers.
 */
public final class AnchoredEffectManager extends SavedData {
    private static final String DATA_ID = "magicgame_anchored_effects";
    private static final Codec<AnchoredEffectManager> CODEC = AnchoredEffectInstance.CODEC.listOf()
            .xmap(AnchoredEffectManager::fromSerialized, AnchoredEffectManager::toSerialized);
    private static final SavedDataType<AnchoredEffectManager> TYPE = new SavedDataType<>(
            DATA_ID,
            AnchoredEffectManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final List<AnchoredEffectInstance> effects = new ArrayList<>();

    public static AnchoredEffectManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void addEffect(AnchoredEffectInstance effect) {
        effects.add(effect);
        setDirty();
    }

    public List<AnchoredEffectInstance> effectsForOwner(UUID playerId) {
        return effects.stream().filter(effect -> effect.ownerMatches(playerId)).toList();
    }

    public int clearOwnerEffects(UUID playerId) {
        int before = effects.size();
        effects.removeIf(effect -> effect.ownerMatches(playerId));
        int removed = before - effects.size();
        if (removed > 0) {
            setDirty();
        }
        return removed;
    }

    public void tick(MinecraftServer server) {
        boolean changed = false;
        Iterator<AnchoredEffectInstance> iterator = effects.iterator();
        while (iterator.hasNext()) {
            AnchoredEffectInstance effect = iterator.next();
            changed |= triggerEffect(server, effect);
            effect.tickSecond();
            if (effect.isExpired()) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    private boolean triggerEffect(MinecraftServer server, AnchoredEffectInstance effect) {
        return switch (effect.kind()) {
            case ALERT_WARD -> tickAlertWard(server, effect);
        };
    }

    private boolean tickAlertWard(MinecraftServer server, AnchoredEffectInstance effect) {
        MagicDebugSettings debugSettings = MagicDebugSettings.get(server);
        if (debugSettings.isFeatureActive(MagicDebugFeature.WARD_BOUNDARY_PARTICLES)) {
            renderWardBoundary(server, effect);
        }

        List<String> occupants = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String playerDimension = player.level().dimension().identifier().toString();
            if (!effect.matchesDimension(playerDimension) || effect.ownerMatches(player.getUUID())) {
                continue;
            }
            if (effect.contains(player.getX(), player.getY(), player.getZ())) {
                occupants.add(player.getUUID().toString());
            }
        }

        boolean changed = false;
        for (String entrantId : effect.updateOccupants(occupants)) {
            UUID uuid = UUID.fromString(entrantId);
            ServerPlayer entrant = server.getPlayerList().getPlayer(uuid);
            ServerPlayer owner = server.getPlayerList().getPlayer(UUID.fromString(effect.ownerId()));
            if (owner != null && debugSettings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES)) {
                owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[debug] Ward Activated @" + effect.x() + ", " + effect.y() + ", " + effect.z()
                                + " by @" + (entrant == null ? entrantId : entrant.getName().getString())
                ));
            }
            if (entrant != null && debugSettings.isFeatureActive(MagicDebugFeature.WARD_MESSAGES)) {
                entrant.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[debug] You triggered a ward @" + effect.x() + ", " + effect.y() + ", " + effect.z() + "."
                ));
            }
            changed = true;
        }
        return changed;
    }

    private static AnchoredEffectManager fromSerialized(List<AnchoredEffectInstance> effects) {
        AnchoredEffectManager manager = new AnchoredEffectManager();
        manager.effects.addAll(effects);
        return manager;
    }

    private List<AnchoredEffectInstance> toSerialized() {
        return List.copyOf(effects);
    }

    private void renderWardBoundary(MinecraftServer server, AnchoredEffectInstance effect) {
        for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
            if (!level.dimension().identifier().toString().equals(effect.dimensionId())) {
                continue;
            }

            double centerX = effect.x() + 0.5D;
            double centerY = effect.y() + 0.2D;
            double centerZ = effect.z() + 0.5D;
            int points = Math.max(12, effect.radius() * 4);
            for (int index = 0; index < points; index++) {
                double angle = (Math.PI * 2.0D * index) / points;
                double px = centerX + Math.cos(angle) * effect.radius();
                double pz = centerZ + Math.sin(angle) * effect.radius();
                level.sendParticles(ParticleTypes.END_ROD, px, centerY, pz, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
            level.sendParticles(ParticleTypes.ENCHANT, centerX, centerY + 0.4D, centerZ, 4, 0.2D, 0.2D, 0.2D, 0.0D);
            return;
        }
    }
}
