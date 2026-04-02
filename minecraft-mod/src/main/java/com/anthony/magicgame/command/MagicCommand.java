package com.anthony.magicgame.command;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.anthony.magicgame.mana.ManaProfile;
import com.anthony.magicgame.mana.PlayerManaManager;
import com.anthony.magicgame.network.MagicNetworking;
import com.anthony.magicgame.spell.GlyphCategory;
import com.anthony.magicgame.spell.GlyphDefinition;
import com.anthony.magicgame.spell.PrototypeSpellDefinition;
import com.anthony.magicgame.spell.SpellChain;
import com.anthony.magicgame.spell.SpellChainParser;
import com.anthony.magicgame.spell.ConstructionPlacementRules;
import com.anthony.magicgame.spell.SpellFlowRules;
import com.anthony.magicgame.spell.SpellIntent;
import com.anthony.magicgame.spell.SpellRecipient;
import com.anthony.magicgame.spell.SpellResolutionPlan;
import com.anthony.magicgame.spell.SpellResolver;
import com.anthony.magicgame.spell.SpellSource;
import com.anthony.magicgame.spell.SpellTargetingRules;
import com.anthony.magicgame.spell.PatternInteractionRules;
import com.anthony.magicgame.spell.pattern.LockedBlockManager;
import com.anthony.magicgame.spell.pattern.LockingPatternBlocks;
import com.anthony.magicgame.spell.effect.AnchoredEffectInstance;
import com.anthony.magicgame.spell.effect.AnchoredEffectKind;
import com.anthony.magicgame.spell.effect.AnchoredEffectManager;
import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import com.anthony.magicgame.spell.registry.PrototypeSpellRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
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
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Registers the current command-driven prototype interface for testing mana and spell flow.
 */
public final class MagicCommand {
    private static final int DEFAULT_ANCHOR_RADIUS = 6;
    private static final int DEFAULT_ANCHOR_DURATION_SECONDS = 180;
    private static final String CUSTOM_GLYPHS_ARGUMENT = "glyphs";

    private MagicCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("magic")
                        .executes(MagicCommand::showStatus)
                        .then(Commands.literal("status").executes(MagicCommand::showStatus))
                        .then(Commands.literal("glyphs").executes(MagicCommand::listGlyphs))
                        .then(Commands.literal("analyze")
                                .then(Commands.literal("chain")
                                        .then(Commands.argument(CUSTOM_GLYPHS_ARGUMENT, StringArgumentType.greedyString())
                                                .executes(MagicCommand::analyzeCustomChain)))
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestSpells(builder))
                                        .executes(MagicCommand::analyzeSpell)))
                        .then(Commands.literal("cast")
                                .then(Commands.literal("chain")
                                        .then(Commands.argument(CUSTOM_GLYPHS_ARGUMENT, StringArgumentType.greedyString())
                                                .executes(MagicCommand::castCustomChain)))
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

    private static int listGlyphs(CommandContext<CommandSourceStack> context) {
        Map<GlyphCategory, List<GlyphDefinition>> glyphsByCategory = CoreGlyphRegistry.all().stream()
                .sorted(Comparator.comparing(GlyphDefinition::id))
                .collect(java.util.stream.Collectors.groupingBy(
                        GlyphDefinition::category,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        context.getSource().sendSuccess(() -> Component.literal("Prototype glyphs: " + CoreGlyphRegistry.size()), false);
        for (Map.Entry<GlyphCategory, List<GlyphDefinition>> entry : glyphsByCategory.entrySet()) {
            String glyphs = entry.getValue().stream()
                    .map(GlyphDefinition::id)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("(none)");
            context.getSource().sendSuccess(() -> Component.literal(
                    "- " + entry.getKey() + ": " + glyphs
            ), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int analyzeSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            String spellId = StringArgumentType.getString(context, "spell").toLowerCase(Locale.ROOT);
            PrototypeSpellDefinition definition = PrototypeSpellRegistry.require(spellId);
            SpellChain spell = SpellChainParser.parse(String.join(" ", definition.glyphIds()));
            SpellResolutionPlan plan = SpellResolver.resolve(spell);
            showPlan(context.getSource(), definition.displayName(), spell, plan);
            maybeSendSpellFeedback(context.getSource().getServer(), requirePlayer(context), "Analyzed " + definition.displayName(), summarizePlan(plan));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int analyzeCustomChain(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        try {
            SpellChain spell = parseCustomChain(context);
            SpellResolutionPlan plan = SpellResolver.resolve(spell);
            showPlan(context.getSource(), "Custom Chain", spell, plan);
            maybeSendSpellFeedback(context.getSource().getServer(), player, "Analyzed Custom Chain", summarizePlan(plan));
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int castSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        try {
            String spellId = StringArgumentType.getString(context, "spell").toLowerCase(Locale.ROOT);
            PrototypeSpellDefinition definition = PrototypeSpellRegistry.require(spellId);
            SpellChain spell = SpellChainParser.parse(String.join(" ", definition.glyphIds()));
            SpellResolutionPlan plan = SpellResolver.resolve(spell);
            return castResolvedSpell(context, player, definition.displayName(), spell, plan);
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int castCustomChain(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        try {
            SpellChain spell = parseCustomChain(context);
            SpellResolutionPlan plan = SpellResolver.resolve(spell);
            return castResolvedSpell(context, player, "Custom Chain", spell, plan);
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int anchorSpell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = requirePlayer(context);
        try {
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
            MagicNetworking.syncMana(player, mana);
            context.getSource().sendSuccess(() -> Component.literal(
                    "Anchored " + spellId + " at " + formatPosition(effect)
                            + " with radius " + radius
                            + " for " + durationSeconds + " seconds. Mana now " + mana.currentMana() + "/" + mana.maxMana() + "."
            ), true);
            maybeSendSpellFeedback(
                    context.getSource().getServer(),
                    player,
                    "Anchored Alert Ward",
                    "Radius " + radius + " | Duration " + durationSeconds + "s"
            );
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
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
        MagicNetworking.syncMana(player, mana);
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
        MagicNetworking.syncMana(player, mana);
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
        var server = context.getSource().getServer();
        MagicDebugSettings settings = MagicDebugSettings.get(server);
        settings.setEnabled(enabled);
        MagicNetworking.syncManaForAll(server);
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

        var server = context.getSource().getServer();
        MagicDebugSettings settings = MagicDebugSettings.get(server);
        settings.setFeatureEnabled(feature, enabled);
        MagicNetworking.syncManaForAll(server);
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

    private static SpellChain parseCustomChain(CommandContext<CommandSourceStack> context) {
        return SpellChainParser.parse(StringArgumentType.getString(context, CUSTOM_GLYPHS_ARGUMENT));
    }

    private static int castResolvedSpell(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            String label,
            SpellChain spell,
            SpellResolutionPlan plan
    ) {
        PlayerManaManager manaManager = PlayerManaManager.get(context.getSource().getServer());
        ManaProfile mana = manaManager.getOrCreate(player.getUUID());
        if (!mana.trySpend(plan.manaCost())) {
            context.getSource().sendFailure(Component.literal(
                    "Not enough mana for " + label + ". Need " + plan.manaCost()
                            + ", have " + mana.currentMana() + "."
            ));
            return 0;
        }

        manaManager.setDirty();
        MagicNetworking.syncMana(player, mana);
        showCastResult(context.getSource(), label, plan, mana);
        showWarnings(context.getSource(), plan);
        runPrototypeEffects(player, context.getSource().getLevel(), spell, plan);
        maybeSendSpellFeedback(context.getSource().getServer(), player, "Cast " + label, summarizePlan(plan));
        return Command.SINGLE_SUCCESS;
    }

    private static void showPlan(CommandSourceStack source, String label, SpellChain spell, SpellResolutionPlan plan) {
        source.sendSuccess(() -> Component.literal(
                label + " -> " + summarizePlan(plan)
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Glyphs: " + spell.glyphs().stream().map(GlyphDefinition::id).reduce((left, right) -> left + " -> " + right).orElse("(none)")
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Traits: " + plan.interpretedSpell().traits().stream()
                        .map(Enum::name)
                        .sorted()
                        .reduce((left, right) -> left + ", " + right)
                        .orElse("(none)")
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Intent scores: " + formatScoreMap(plan.interpretedSpell().intentScores())
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Domain scores: " + formatScoreMap(plan.interpretedSpell().domainScores())
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Recipient scores: " + formatScoreMap(plan.interpretedSpell().recipientScores())
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Source scores: " + formatScoreMap(plan.interpretedSpell().sourceScores())
        ), false);
        if (plan.likelyFailureProfile() != null) {
            source.sendSuccess(() -> Component.literal(
                    "Likely failure: " + formatFailureProfile(plan.likelyFailureProfile())
            ), false);
        }
        showWarnings(source, plan);
    }

    private static void showCastResult(CommandSourceStack source, String label, SpellResolutionPlan plan, ManaProfile mana) {
        source.sendSuccess(() -> Component.literal(
                label + " resolved as " + plan.intent()
                        + " using " + plan.primaryDomain()
                        + ". Cost " + plan.manaCost()
                        + ", stability " + plan.stabilityScore()
                        + ", confidence " + plan.confidenceScore()
                        + ". Mana now " + mana.currentMana() + "/" + mana.maxMana() + "."
        ), false);
    }

    private static void showWarnings(CommandSourceStack source, SpellResolutionPlan plan) {
        if (!plan.warnings().isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "Warnings: " + String.join(" | ", plan.warnings())
            ), false);
        }
    }

    private static void maybeSendSpellFeedback(net.minecraft.server.MinecraftServer server, ServerPlayer player, String title, String detail) {
        MagicDebugSettings settings = MagicDebugSettings.get(server);
        MagicNetworking.sendSpellFeedback(
                player,
                title,
                settings.isFeatureActive(MagicDebugFeature.SPELL_FEEDBACK_TEXT) ? detail : ""
        );
    }

    private static String summarizePlan(SpellResolutionPlan plan) {
        return plan.intent() + " | " + plan.primaryDomain()
                + " | Cost " + plan.manaCost()
                + " | Stability " + plan.stabilityScore()
                + " | Confidence " + plan.confidenceScore();
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

    private static void runPrototypeEffects(
            ServerPlayer player,
            ServerLevel level,
            SpellChain spell,
            SpellResolutionPlan plan
    ) {
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

        if (containsFire && debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_VISUALS)) {
            spawnDebugFireball(player, level);
            if (debugSettings.isFeatureActive(MagicDebugFeature.FIREBALL_TRAIL_PARTICLES)) {
                spawnFireballTrailParticles(player, level);
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
            player.sendSystemMessage(Component.literal(
                    "[debug] Spawned vanilla LargeFireball placeholder for fireball testing."
            ));
            return;
        }

        if (containsForce) {
            LivingEntity target = findLivingTargetInLook(player, level, 10.0D);
            if (target != null) {
                Vec3 look = player.getLookAngle().normalize();
                target.hurt(level.damageSources().magic(), 4.0F);
                target.push(look.x * 1.4D, 0.35D, look.z * 1.4D);
                level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(0.5D), target.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.03D);
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.02D);
                level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.9F);
                player.sendSystemMessage(Component.literal(
                        "[debug] Force bolt struck " + target.getName().getString() + "."
                ));
            } else {
                spawnForcePulseParticles(player, level);
                player.sendSystemMessage(Component.literal(
                        "[debug] Force bolt discharged without a living target."
                ));
            }
        }
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

    private static void spawnDebugFireball(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        LargeFireball fireball = new LargeFireball(level, player, look.scale(0.1D), 0);
        fireball.setPos(player.getX() + look.x * 2.0D, player.getEyeY() - 0.15D, player.getZ() + look.z * 2.0D);
        level.addFreshEntity(fireball);
    }

    private static void spawnFireballTrailParticles(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        for (int step = 1; step <= 6; step++) {
            double distance = step * 0.8D;
            double x = player.getX() + look.x * distance;
            double y = player.getEyeY() - 0.1D + look.y * distance;
            double z = player.getZ() + look.z * distance;
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.05D, 0.05D, 0.05D, 0.0D);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
        }
    }

    private static void spawnForcePulseParticles(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        for (int step = 1; step <= 5; step++) {
            double distance = step * 1.0D;
            double x = player.getX() + look.x * distance;
            double y = player.getEyeY() - 0.05D + look.y * distance;
            double z = player.getZ() + look.z * distance;
            level.sendParticles(ParticleTypes.CLOUD, x, y, z, 3, 0.08D, 0.08D, 0.08D, 0.01D);
        }
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

    private static String formatScoreMap(Map<?, Integer> scores) {
        return scores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((left, right) -> {
                    int byValue = Integer.compare(right.getValue(), left.getValue());
                    if (byValue != 0) {
                        return byValue;
                    }
                    return left.getKey().toString().compareTo(right.getKey().toString());
                })
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("(none)");
    }

    private static String formatFailureProfile(com.anthony.magicgame.spell.SpellFailureProfile failureProfile) {
        return failureProfile.failureType()
                + " (severity " + failureProfile.severity()
                + ", domain " + failureProfile.domain()
                + ") | " + failureProfile.description()
                + " | " + failureProfile.gameplayOutcome();
    }
}
