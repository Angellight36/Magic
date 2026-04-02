package com.anthony.magicgame.spell;

import com.anthony.magicgame.mana.ManaProfile;
import com.anthony.magicgame.mana.PlayerManaManager;
import com.anthony.magicgame.network.MagicNetworking;
import com.anthony.magicgame.spell.effect.AnchoredEffectInstance;
import com.anthony.magicgame.spell.effect.AnchoredEffectKind;
import com.anthony.magicgame.spell.effect.AnchoredEffectManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Owns command-facing spell casting and anchoring preconditions so the command layer does less runtime bookkeeping.
 */
public final class PrototypeSpellCastingService {
    private PrototypeSpellCastingService() {
    }

    public static ManaSpendResult spendManaForCast(
            MinecraftServer server,
            ServerPlayer player,
            String label,
            int manaCost
    ) {
        PlayerManaManager manaManager = PlayerManaManager.get(server);
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        if (!mana.trySpend(manaCost)) {
            return ManaSpendResult.failure(
                    "Not enough mana for " + label + ". Need " + manaCost
                            + ", have " + mana.currentMana() + "."
            );
        }

        manaManager.setDirty();
        MagicNetworking.syncMana(player, mana);
        return ManaSpendResult.success(mana);
    }

    public static AnchorResult anchorSpell(
            MinecraftServer server,
            ServerPlayer player,
            String spellId,
            int radius,
            int durationSeconds,
            SpellResolutionPlan plan
    ) {
        if (!isAnchorablePrototype(spellId)) {
            return AnchorResult.failure("Only alert_ward can be anchored in the current prototype.");
        }

        ManaSpendResult spendResult = spendManaForCast(server, player, spellId, plan.manaCost());
        if (!spendResult.success()) {
            return AnchorResult.failure(spendResult.failureMessage());
        }

        ServerLevel level = (ServerLevel) player.level();
        AnchoredEffectInstance effect = AnchoredEffectInstance.create(
                AnchoredEffectKind.ALERT_WARD,
                player.getUUID(),
                spellId,
                level.dimension().identifier().toString(),
                player.blockPosition().getX(),
                player.blockPosition().getY(),
                player.blockPosition().getZ(),
                radius,
                durationSeconds * 20
        );

        AnchoredEffectManager effectManager = AnchoredEffectManager.get(server);
        effectManager.addEffect(effect);
        return AnchorResult.success(effect, spendResult.mana());
    }

    public static boolean isAnchorablePrototype(String spellId) {
        return "alert_ward".equals(spellId);
    }

    /**
     * Result of a mana-spending precondition check.
     */
    public record ManaSpendResult(boolean success, ManaProfile mana, String failureMessage) {
        public static ManaSpendResult success(ManaProfile mana) {
            return new ManaSpendResult(true, mana, null);
        }

        public static ManaSpendResult failure(String failureMessage) {
            return new ManaSpendResult(false, null, failureMessage);
        }
    }

    /**
     * Result of preparing and storing an anchored prototype spell.
     */
    public record AnchorResult(boolean success, AnchoredEffectInstance effect, ManaProfile mana, String failureMessage) {
        public static AnchorResult success(AnchoredEffectInstance effect, ManaProfile mana) {
            return new AnchorResult(true, effect, mana, null);
        }

        public static AnchorResult failure(String failureMessage) {
            return new AnchorResult(false, null, null, failureMessage);
        }
    }
}
