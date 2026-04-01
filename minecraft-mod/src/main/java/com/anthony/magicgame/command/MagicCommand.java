package com.anthony.magicgame.command;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.anthony.magicgame.mana.ManaProfile;
import com.anthony.magicgame.mana.PlayerManaManager;
import com.anthony.magicgame.spell.PrototypeSpellDefinition;
import com.anthony.magicgame.spell.SpellResolutionPlan;
import com.anthony.magicgame.spell.SpellResolver;
import com.anthony.magicgame.spell.effect.AnchoredEffectInstance;
import com.anthony.magicgame.spell.effect.AnchoredEffectKind;
import com.anthony.magicgame.spell.effect.AnchoredEffectManager;
import com.anthony.magicgame.spell.registry.PrototypeSpellRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

/**
 * Registers the current command-driven prototype interface for testing mana and spell flow.
 */
public final class MagicCommand {
    private static final int DEFAULT_ANCHOR_RADIUS = 6;
    private static final int DEFAULT_ANCHOR_DURATION_SECONDS = 180;

    private MagicCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("magic")
                        .executes(MagicCommand::showStatus)
                        .then(Commands.literal("status").executes(MagicCommand::showStatus))
                        .then(Commands.literal("cast")
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestSpells(builder))
                                        .executes(MagicCommand::castSpell)))
                        .then(Commands.literal("spells").executes(MagicCommand::listSpells))
                        .then(Commands.literal("debug")
                                .executes(MagicCommand::showDebugSettings)
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(MagicCommand::setDebugEnabled))
                                .then(Commands.literal("feature")
                                        .then(Commands.argument("feature", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestDebugFeatures(builder))
                                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                        .executes(MagicCommand::setDebugFeatureEnabled)))))
                        .then(Commands.literal("anchor")
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestAnchorableSpells(builder))
                                        .executes(MagicCommand::anchorSpell)
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 32))
                                                .executes(MagicCommand::anchorSpell)
                                                .then(Commands.argument("duration_seconds", IntegerArgumentType.integer(10, 3600))
                                                        .executes(MagicCommand::anchorSpell)))))
                        .then(Commands.literal("anchors")
                                .executes(MagicCommand::listAnchors)
                                .then(Commands.literal("clear").executes(MagicCommand::clearAnchors)))
                        .then(Commands.literal("mana")
                                .then(Commands.literal("refill")
                                        .executes(MagicCommand::refillMana))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(MagicCommand::setMana))))
        ));
    }

    public static int showStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        AnchoredEffectManager effectManager = AnchoredEffectManager.get(context.getSource().getServer());

        context.getSource().sendSuccess(() -> Component.literal(
                "Mana " + mana.currentMana() + "/" + mana.maxMana()
                        + " | Prototype spells: " + PrototypeSpellRegistry.size()
                        + " | Anchors: " + effectManager.effectsForOwner(player.getUUID()).size()
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listSpells(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
                "Prototype spells: " + String.join(", ", PrototypeSpellRegistry.ids())
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int castSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        String spellId = StringArgumentType.getString(context, "spell").toLowerCase(Locale.ROOT);
        PrototypeSpellDefinition definition = PrototypeSpellRegistry.require(spellId);
        SpellResolutionPlan plan = SpellResolver.resolve(definition);
        MagicDebugSettings debugSettings = MagicDebugSettings.get(context.getSource().getServer());
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());

        if (!mana.trySpend(plan.manaCost())) {
            context.getSource().sendFailure(Component.literal(
                    "Not enough mana for " + definition.displayName() + ". Need " + plan.manaCost()
                            + ", have " + mana.currentMana() + "."
            ));
            return 0;
        }

        manaManager.setDirty();
        context.getSource().sendSuccess(() -> Component.literal(
                definition.displayName() + " resolved as " + plan.intent()
                        + " using " + plan.primaryDomain()
                        + ". Cost " + plan.manaCost()
                        + ", stability " + plan.stabilityScore()
                        + ". Mana now " + mana.currentMana() + "/" + mana.maxMana() + "."
        ), false);

        if (!plan.warnings().isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "Warnings: " + String.join(" | ", plan.warnings())
            ), false);
        }
        if (spellId.equals("fireball") && debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_VISUALS)) {
            spawnDebugFireball(player, context.getSource().getLevel());
            context.getSource().sendSuccess(() -> Component.literal(
                    "[debug] Spawned vanilla LargeFireball placeholder for fireball testing."
            ), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int anchorSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        String spellId = StringArgumentType.getString(context, "spell").toLowerCase(Locale.ROOT);
        if (!spellId.equals("alert_ward")) {
            context.getSource().sendFailure(Component.literal(
                    "Only alert_ward can be anchored in the current prototype."
            ));
            return 0;
        }
        AnchoredEffectKind kind = AnchoredEffectKind.ALERT_WARD;

        int radius = readOptionalInt(context, "radius", DEFAULT_ANCHOR_RADIUS);
        int durationSeconds = readOptionalInt(context, "duration_seconds", DEFAULT_ANCHOR_DURATION_SECONDS);
        SpellResolutionPlan plan = SpellResolver.resolve(PrototypeSpellRegistry.require(spellId));
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        if (!mana.trySpend(plan.manaCost())) {
            context.getSource().sendFailure(Component.literal(
                    "Not enough mana to anchor " + spellId + ". Need " + plan.manaCost()
                            + ", have " + mana.currentMana() + "."
            ));
            return 0;
        }

        ServerLevel level = context.getSource().getLevel();
        AnchoredEffectInstance effect = AnchoredEffectInstance.create(
                kind,
                player.getUUID(),
                spellId,
                level.dimension().identifier().toString(),
                player.blockPosition().getX(),
                player.blockPosition().getY(),
                player.blockPosition().getZ(),
                radius,
                durationSeconds * 20
        );

        AnchoredEffectManager effectManager = AnchoredEffectManager.get(context.getSource().getServer());
        effectManager.addEffect(effect);
        manaManager.setDirty();
        context.getSource().sendSuccess(() -> Component.literal(
                "Anchored " + spellId + " at " + formatPosition(effect)
                        + " with radius " + radius
                        + " for " + durationSeconds + " seconds. Mana now " + mana.currentMana() + "/" + mana.maxMana() + "."
        ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listAnchors(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        List<AnchoredEffectInstance> anchors = AnchoredEffectManager.get(context.getSource().getServer()).effectsForOwner(player.getUUID());
        if (anchors.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("You have no anchored spell effects."), false);
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSuccess(() -> Component.literal("Anchored effects: " + anchors.size()), false);
        for (AnchoredEffectInstance effect : anchors) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "- " + effect.kind() + " [" + effect.spellId() + "] at " + formatPosition(effect)
                            + " radius " + effect.radius()
                            + " remaining " + effect.remainingSeconds() + "s"
            ), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearAnchors(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        AnchoredEffectManager effectManager = AnchoredEffectManager.get(context.getSource().getServer());
        int removed = effectManager.clearOwnerEffects(player.getUUID());
        context.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " anchored effect(s)."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int refillMana(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        mana.restoreToFull();
        manaManager.setDirty();
        context.getSource().sendSuccess(() -> Component.literal("Mana refilled to " + mana.maxMana() + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setMana(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        int amount = IntegerArgumentType.getInteger(context, "amount");
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        mana.setCurrentMana(amount);
        manaManager.setDirty();
        context.getSource().sendSuccess(() -> Component.literal(
                "Mana set to " + mana.currentMana() + "/" + mana.maxMana() + "."
        ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int showDebugSettings(CommandContext<CommandSourceStack> context) {
        MagicDebugSettings settings = MagicDebugSettings.get(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal(
                "Debug global: " + settings.enabled()
        ), false);
        for (MagicDebugFeature feature : MagicDebugFeature.values()) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "- " + feature.id() + ": configured=" + settings.isFeatureConfiguredEnabled(feature)
                            + ", active=" + settings.isFeatureActive(feature)
            ), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setDebugEnabled(CommandContext<CommandSourceStack> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        MagicDebugSettings settings = MagicDebugSettings.get(context.getSource().getServer());
        settings.setEnabled(enabled);
        context.getSource().sendSuccess(() -> Component.literal("Debug global set to " + enabled + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setDebugFeatureEnabled(CommandContext<CommandSourceStack> context) {
        String featureId = StringArgumentType.getString(context, "feature").toLowerCase(Locale.ROOT);
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        MagicDebugFeature feature;
        try {
            feature = MagicDebugFeature.require(featureId);
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }

        MagicDebugSettings settings = MagicDebugSettings.get(context.getSource().getServer());
        settings.setFeatureEnabled(feature, enabled);
        context.getSource().sendSuccess(() -> Component.literal(
                "Debug feature " + feature.id() + " set to " + enabled + "."
        ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestSpells(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(PrototypeSpellRegistry.ids(), builder);
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestAnchorableSpells(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(List.of("alert_ward"), builder);
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestDebugFeatures(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(MagicDebugFeature.ids(), builder);
    }

    private static ServerPlayer requirePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static int readOptionalInt(CommandContext<CommandSourceStack> context, String name, int fallback) {
        try {
            return IntegerArgumentType.getInteger(context, name);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private static String formatPosition(AnchoredEffectInstance effect) {
        return effect.x() + ", " + effect.y() + ", " + effect.z();
    }

    private static void spawnDebugFireball(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        LargeFireball fireball = new LargeFireball(level, player, look.scale(0.1D), 0);
        fireball.setPos(player.getX() + look.x * 2.0D, player.getEyeY() - 0.15D, player.getZ() + look.z * 2.0D);
        level.addFreshEntity(fireball);
    }
}
