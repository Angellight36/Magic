package com.angellight.magicgame.spell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Performs a scored semantic interpretation pass for prototype spell chains.
 *
 * <p>The interpreter deliberately uses multiple overlapping signals instead of one hardcoded
 * "pattern match = outcome" rule. That keeps mixed-purpose chains explainable and tunable while
 * still allowing effects such as sacrificial healing to express both restoration and self-cost.</p>
 */
public final class SpellInterpreter {
    private SpellInterpreter() {
    }

    public static InterpretedSpell interpret(SpellChain spell) {
        List<String> warnings = new ArrayList<>();
        SemanticSignals signals = new SemanticSignals();

        for (GlyphDefinition glyph : spell.glyphs()) {
            signals.observe(glyph);
        }

        if (!spell.isPrototypeCastable()) {
            warnings.add("Spell chains need at least one principle and one operation.");
        }
        if (signals.referenceCount == 0 && signals.constraintCount == 0 && !signals.has("anchor")) {
            warnings.add("Spell may be difficult to aim because it lacks references and spatial constraints.");
        }
        if (signals.has("release") && !signals.has("separate")) {
            warnings.add("Traveling effects without separation logic are prone to self-harm.");
        }
        if (signals.has("transfer") && !signals.has("seen_target") && !signals.has("chosen_point")) {
            warnings.add("Transfer chains are unstable without a clear recipient or destination.");
        }
        if (signals.has("anchor") && !signals.has("boundary") && !signals.has("field")) {
            warnings.add("Anchored chains without boundary or field logic may disperse unpredictably.");
        }
        if (signals.has("restore") && !signals.has("life")) {
            warnings.add("Restore logic is present without explicit life-domain support.");
        }

        finalizeTraits(signals);
        Map<SpellIntent, Integer> intentScores = scoreIntents(signals);
        Map<SpellRecipient, Integer> recipientScores = SpellFlowRules.scoreRecipients(spell);
        Map<SpellSource, Integer> sourceScores = SpellFlowRules.scoreSources(spell);
        RankedIntent rankedIntent = chooseIntent(intentScores);

        SpellIntent intent = rankedIntent.intent();
        if (rankedIntent.topScore() < 10) {
            intent = SpellIntent.UNKNOWN_UNSTABLE;
            warnings.add("Interpreter could not confidently classify this spell yet.");
        } else if (rankedIntent.margin() <= 1) {
            warnings.add("Interpreter sees this chain as highly ambiguous.");
        }

        boostDomains(signals, intent);
        if (signals.domainScores.isEmpty()) {
            signals.domainScores.put(MagicDomain.INFORMATION, 1);
        }

        return new InterpretedSpell(
                spell,
                intent,
                Set.copyOf(signals.domainScores.keySet()),
                signals.domainScores,
                signals.traits,
                recipientScores,
                sourceScores,
                intentScores,
                rankedIntent.margin(),
                warnings
        );
    }

    private static void finalizeTraits(SemanticSignals signals) {
        if (signals.has("life") || signals.has("refine") || signals.has("strengthen") || signals.has("restore")) {
            signals.traits.add(SpellTrait.RESTORATIVE);
        }
        if (signals.has("life_pattern") || signals.has("locking_pattern") || signals.has("perception")
                || signals.has("order") || signals.has("binding")) {
            signals.traits.add(SpellTrait.PATTERN_SENSITIVE);
        }
        if (signals.has("transfer") && signals.has("life")) {
            signals.traits.add(SpellTrait.VITALITY_TRANSFER);
        }
    }

    private static Map<SpellIntent, Integer> scoreIntents(SemanticSignals signals) {
        Map<SpellIntent, Integer> scores = new HashMap<>();
        scores.put(SpellIntent.TRAVELING_EFFECT, travelingScore(signals));
        scores.put(SpellIntent.BOUNDARY_WARD, boundaryWardScore(signals));
        scores.put(SpellIntent.PATTERN_INTERACTION, patternInteractionScore(signals));
        scores.put(SpellIntent.RESTORATION_EFFECT, restorationScore(signals));
        scores.put(SpellIntent.VITALITY_TRANSFER, vitalityTransferScore(signals));
        scores.put(SpellIntent.CONSTRUCTION_EFFECT, constructionScore(signals));
        scores.put(SpellIntent.UNKNOWN_UNSTABLE, 0);
        return scores;
    }

    private static int travelingScore(SemanticSignals signals) {
        int score = signals.count("fire") * 5
                + signals.count("force") * 4
                + signals.count("gather") * 2
                + signals.count("shape") * 2
                + signals.count("direct") * 3
                + signals.count("forward") * 2
                + signals.count("release") * 4
                + signals.count("on_impact") * 2;
        if (signals.has("shape") && signals.has("direct") && signals.has("release")) {
            score += 8;
        }
        if (signals.has("anchor")) {
            score -= 3;
        }
        return score;
    }

    private static int boundaryWardScore(SemanticSignals signals) {
        int score = signals.count("anchor") * 5
                + signals.count("boundary") * 4
                + signals.count("field") * 4
                + signals.count("persist") * 3
                + signals.count("sustain") * 2
                + signals.count("bind") * 2
                + signals.count("attune") * 2
                + signals.count("on_entry") * 2;
        if (signals.has("anchor") && signals.has("boundary") && signals.has("field")) {
            score += 10;
        }
        return score;
    }

    private static int patternInteractionScore(SemanticSignals signals) {
        int score = signals.count("perception") * 2
                + signals.count("order") * 3
                + signals.count("binding") * 2
                + signals.count("locking_pattern") * 5
                + signals.count("separate") * 3
                + signals.count("disrupt") * 3
                + signals.count("terminate") * 3
                + signals.count("unravel") * 3
                + signals.count("gentle") * 1;
        if (signals.has("locking_pattern") && (signals.has("separate") || signals.has("disrupt")
                || signals.has("terminate") || signals.has("unravel"))) {
            score += 8;
        }
        return score;
    }

    private static int restorationScore(SemanticSignals signals) {
        int score = signals.count("life") * 4
                + signals.count("life_pattern") * 4
                + signals.count("restore") * 2
                + signals.count("refine") * 2
                + signals.count("strengthen") * 2
                + signals.count("stabilize") * 2
                + signals.count("gentle") * 1;
        if (signals.has("life") && (signals.has("life_pattern") || signals.has("refine") || signals.has("strengthen") || signals.has("restore"))) {
            score += 8;
        }
        if (signals.has("transfer")) {
            score -= 6;
        }
        if ((signals.has("self") || signals.has("caster")) && signals.has("transfer")) {
            score -= 4;
        }
        return score;
    }

    private static int vitalityTransferScore(SemanticSignals signals) {
        int score = signals.count("life") * 3
                + signals.count("life_pattern") * 4
                + signals.count("transfer") * 8
                + signals.count("restore") * 1
                + signals.count("refine") * 2
                + signals.count("self") * 4
                + signals.count("caster") * 4
                + signals.count("seen_target") * 5
                + signals.targetedReferenceCount * 1
                + signals.count("stabilize") * 1
                + signals.count("direct") * 2;
        if (signals.has("transfer") && signals.has("life")) {
            score += 7;
        }
        if ((signals.has("self") || signals.has("caster")) && signals.has("seen_target")) {
            score += 10;
        }
        if ((signals.has("self") || signals.has("caster")) && signals.has("life_pattern") && signals.has("transfer")) {
            score += 6;
        }
        return score;
    }

    private static int constructionScore(SemanticSignals signals) {
        int score = signals.count("earth") * 5
                + signals.count("shape") * 3
                + signals.count("path") * 3
                + signals.count("surface") * 3
                + signals.count("raise") * 4
                + signals.count("carve") * 3
                + signals.count("chosen_point") * 2
                + signals.count("anchor") * 1
                + signals.count("strengthen") * 2;
        if (signals.has("earth") && (signals.has("path") || signals.has("surface") || signals.has("raise") || signals.has("carve"))) {
            score += 8;
        }
        return score;
    }

    private static RankedIntent chooseIntent(Map<SpellIntent, Integer> scores) {
        List<Map.Entry<SpellIntent, Integer>> ranked = scores.entrySet().stream()
                .filter(entry -> entry.getKey() != SpellIntent.UNKNOWN_UNSTABLE)
                .sorted(Map.Entry.<SpellIntent, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().name()))
                .toList();
        Map.Entry<SpellIntent, Integer> top = ranked.getFirst();
        int secondScore = ranked.size() > 1 ? ranked.get(1).getValue() : 0;
        return new RankedIntent(top.getKey(), top.getValue(), Math.max(0, top.getValue() - secondScore));
    }

    private static void boostDomains(SemanticSignals signals, SpellIntent intent) {
        switch (intent) {
            case TRAVELING_EFFECT -> {
                signals.addDomain(MagicDomain.DAMAGE, 8);
                signals.addDomain(MagicDomain.SPATIAL, 5);
            }
            case BOUNDARY_WARD -> {
                signals.addDomain(MagicDomain.PATTERN, 8);
                signals.addDomain(MagicDomain.SPATIAL, 4);
            }
            case PATTERN_INTERACTION -> {
                signals.addDomain(MagicDomain.PATTERN, 8);
                signals.addDomain(MagicDomain.INFORMATION, 4);
            }
            case RESTORATION_EFFECT -> {
                signals.addDomain(MagicDomain.LIFE, 8);
                signals.addDomain(MagicDomain.INFORMATION, 3);
            }
            case VITALITY_TRANSFER -> {
                signals.addDomain(MagicDomain.LIFE, 7);
                signals.addDomain(MagicDomain.INFORMATION, 4);
            }
            case CONSTRUCTION_EFFECT -> {
                signals.addDomain(MagicDomain.STRUCTURE, 8);
                signals.addDomain(MagicDomain.SPATIAL, 4);
            }
            case UNKNOWN_UNSTABLE -> signals.addDomain(MagicDomain.INFORMATION, 1);
        }

        if (signals.traits.contains(SpellTrait.RESTORATIVE)) {
            signals.addDomain(MagicDomain.LIFE, 2);
        }
        if (signals.traits.contains(SpellTrait.PATTERN_SENSITIVE)) {
            signals.addDomain(MagicDomain.PATTERN, 2);
            signals.addDomain(MagicDomain.INFORMATION, 2);
        }
        if (signals.traits.contains(SpellTrait.STRUCTURAL_SHAPING)) {
            signals.addDomain(MagicDomain.STRUCTURE, 2);
        }
        if (signals.traits.contains(SpellTrait.TRAVELING_DELIVERY)) {
            signals.addDomain(MagicDomain.SPATIAL, 2);
        }
        if (signals.traits.contains(SpellTrait.VITALITY_TRANSFER)) {
            signals.addDomain(MagicDomain.LIFE, 2);
        }
    }

    private record RankedIntent(SpellIntent intent, int topScore, int margin) {
    }

    private static final class SemanticSignals {
        private final Map<String, Integer> glyphCounts = new HashMap<>();
        private final EnumSet<SpellTrait> traits = EnumSet.noneOf(SpellTrait.class);
        private final Map<MagicDomain, Integer> domainScores = new HashMap<>();
        private int constraintCount;
        private int referenceCount;
        private int targetedReferenceCount;

        private void observe(GlyphDefinition glyph) {
            glyphCounts.merge(glyph.id(), 1, Integer::sum);
            glyph.domainHints().forEach(domain -> addDomain(domain, 2));
            if (glyph.category() == GlyphCategory.CONSTRAINT) {
                constraintCount++;
            }
            if (glyph.category() == GlyphCategory.REFERENCE) {
                referenceCount++;
            }
            switch (glyph.id()) {
                case "anchor" -> traits.add(SpellTrait.ANCHORED);
                case "boundary" -> traits.add(SpellTrait.BOUNDED_AREA);
                case "field" -> traits.add(SpellTrait.FIELD_EFFECT);
                case "direct", "forward", "release", "on_impact" -> traits.add(SpellTrait.TRAVELING_DELIVERY);
                case "self", "caster" -> traits.add(SpellTrait.SELF_REFERENCE);
                case "seen_target", "chosen_point", "life_pattern", "locking_pattern" -> {
                    traits.add(SpellTrait.TARGETED_REFERENCE);
                    targetedReferenceCount++;
                }
                case "persist", "sustain", "stabilize" -> traits.add(SpellTrait.PERSISTENT);
                case "attune" -> traits.add(SpellTrait.ATTUNED_OWNER);
                case "disrupt", "terminate", "unravel", "separate" -> traits.add(SpellTrait.DISRUPTIVE);
                case "earth", "shape", "path", "surface", "raise", "carve" -> traits.add(SpellTrait.STRUCTURAL_SHAPING);
                default -> {
                    // No additional traits for this glyph in the current prototype.
                }
            }
        }

        private boolean has(String glyphId) {
            return glyphCounts.containsKey(glyphId);
        }

        private int count(String glyphId) {
            return glyphCounts.getOrDefault(glyphId, 0);
        }

        private void addDomain(MagicDomain domain, int score) {
            domainScores.merge(domain, score, Integer::sum);
        }
    }
}
