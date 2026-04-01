package com.anthony.magicgame.spell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Performs the first pass of semantic interpretation for prototype spell chains.
 */
public final class SpellInterpreter {
    private SpellInterpreter() {
    }

    public static InterpretedSpell interpret(SpellChain spell) {
        EnumSet<MagicDomain> domains = EnumSet.noneOf(MagicDomain.class);
        List<String> warnings = new ArrayList<>();
        boolean hasAnchor = false;
        boolean hasBoundary = false;
        boolean hasField = false;
        boolean hasLife = false;
        boolean hasLifePattern = false;
        boolean hasLockingPattern = false;
        boolean hasShape = false;
        boolean hasDirect = false;
        boolean hasRelease = false;
        boolean hasSeparate = false;
        boolean hasEarth = false;
        boolean hasSurface = false;
        boolean hasPath = false;

        for (GlyphDefinition glyph : spell.glyphs()) {
            domains.addAll(glyph.domainHints());
            switch (glyph.id()) {
                case "anchor" -> hasAnchor = true;
                case "boundary" -> hasBoundary = true;
                case "field" -> hasField = true;
                case "life" -> hasLife = true;
                case "life_pattern" -> hasLifePattern = true;
                case "locking_pattern" -> hasLockingPattern = true;
                case "shape" -> hasShape = true;
                case "direct" -> hasDirect = true;
                case "release" -> hasRelease = true;
                case "separate" -> hasSeparate = true;
                case "earth" -> hasEarth = true;
                case "surface" -> hasSurface = true;
                case "path" -> hasPath = true;
                default -> {
                    // No extra flags for this glyph in the current prototype.
                }
            }
        }

        if (!spell.isPrototypeCastable()) {
            warnings.add("Spell chains need at least one principle and one operation.");
        }
        if (!spell.categoriesPresent().contains(GlyphCategory.REFERENCE) && !spell.categoriesPresent().contains(GlyphCategory.CONSTRAINT)) {
            warnings.add("Spell may be difficult to aim because it lacks references and spatial constraints.");
        }
        if (hasRelease && !hasSeparate) {
            warnings.add("Traveling effects without separation logic are prone to self-harm.");
        }

        SpellIntent intent;
        if (hasAnchor && hasBoundary && hasField) {
            intent = SpellIntent.BOUNDARY_WARD;
        } else if (hasLife && hasLifePattern) {
            intent = SpellIntent.HEALING_EFFECT;
        } else if (hasLockingPattern && (hasSeparate || spell.glyphs().stream().anyMatch(glyph -> glyph.id().equals("disrupt") || glyph.id().equals("terminate")))) {
            intent = SpellIntent.PATTERN_INTERACTION;
        } else if (hasShape && hasDirect && hasRelease) {
            intent = SpellIntent.TRAVELING_EFFECT;
        } else if (hasEarth && (hasSurface || hasPath)) {
            intent = SpellIntent.CONSTRUCTION_EFFECT;
        } else {
            intent = SpellIntent.UNKNOWN_UNSTABLE;
            warnings.add("Interpreter could not confidently classify this spell yet.");
        }

        if (domains.isEmpty()) {
            domains = EnumSet.of(MagicDomain.INFORMATION);
        }
        return new InterpretedSpell(spell, intent, Set.copyOf(domains), warnings);
    }
}