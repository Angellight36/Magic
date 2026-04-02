package com.anthony.magicgame.command;

import com.anthony.magicgame.debug.MagicDebugFeature;
import com.anthony.magicgame.debug.MagicDebugSettings;
import com.anthony.magicgame.mana.ManaProfile;
import com.anthony.magicgame.mana.PlayerManaManager;
import com.anthony.magicgame.network.MagicNetworking;
import com.anthony.magicgame.spell.GlyphCategory;
import com.anthony.magicgame.spell.GlyphDefinition;
import com.anthony.magicgame.spell.PrototypeSpellDefinition;
import com.anthony.magicgame.spell.PrototypeSpellCastingService;
import com.anthony.magicgame.spell.SpellChain;
import com.anthony.magicgame.spell.SpellChainParser;
import com.anthony.magicgame.spell.SpellIntent;
import com.anthony.magicgame.spell.SpellResolutionPlan;
import com.anthony.magicgame.spell.SpellResolver;
import com.anthony.magicgame.spell.effect.AnchoredEffectInstance;
import com.anthony.magicgame.spell.effect.AnchoredEffectManager;
import com.anthony.magicgame.spell.effect.PrototypeSpellEffectService;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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
            int radius = readOptionalInt(context, "radius", DEFAULT_ANCHOR_RADIUS);
            int durationSeconds = readOptionalInt(context, "duration_seconds", DEFAULT_ANCHOR_DURATION_SECONDS);
            SpellResolutionPlan plan = SpellResolver.resolve(PrototypeSpellRegistry.require(spellId));
            PrototypeSpellCastingService.AnchorResult result = PrototypeSpellCastingService.anchorSpell(
                    context.getSource().getServer(),
                    player,
                    spellId,
                    radius,
                    durationSeconds,
                    plan
            );
            if (!result.success()) {
                context.getSource().sendFailure(Component.literal(result.failureMessage()));
                return 0;
            }

            AnchoredEffectInstance effect = result.effect();
            context.getSource().sendSuccess(() -> Component.literal(
                    "Anchored " + spellId + " at " + formatPosition(effect)
                            + " with radius " + radius
                            + " for " + durationSeconds + " seconds. Mana now "
                            + result.mana().currentMana() + "/" + result.mana().maxMana() + "."
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
        return SharedSuggestionProvider.suggest(
                PrototypeSpellRegistry.ids().stream().filter(PrototypeSpellCastingService::isAnchorablePrototype),
                builder
        );
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
        PrototypeSpellCastingService.ManaSpendResult spendResult = PrototypeSpellCastingService.spendManaForCast(
                context.getSource().getServer(),
                player,
                label,
                plan.manaCost()
        );
        if (!spendResult.success()) {
            context.getSource().sendFailure(Component.literal(spendResult.failureMessage()));
            return 0;
        }

        showCastResult(context.getSource(), label, plan, spendResult.mana());
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
        PrototypeSpellEffectService.cast(player, level, spell, plan);
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
