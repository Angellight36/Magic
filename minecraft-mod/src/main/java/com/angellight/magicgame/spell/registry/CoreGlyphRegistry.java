package com.angellight.magicgame.spell.registry;

import com.angellight.magicgame.spell.GlyphCategory;
import com.angellight.magicgame.spell.GlyphDefinition;
import com.angellight.magicgame.spell.MagicDomain;
import com.angellight.magicgame.spell.SpellChain;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds the first prototype glyph slice derived from the design notes.
 */
public final class CoreGlyphRegistry {
    private static final List<GlyphDefinition> GLYPHS = List.of(
            glyph("fire", "Fire", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.DAMAGE), 4, 2),
            glyph("earth", "Earth", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.STRUCTURE), 4, 3),
            glyph("force", "Force", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.DAMAGE, MagicDomain.SPATIAL), 4, 2),
            glyph("binding", "Binding", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.PATTERN), 3, 3),
            glyph("order", "Order", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.PATTERN, MagicDomain.INFORMATION), 3, 4),
            glyph("life", "Life", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.LIFE), 4, 3),
            glyph("space", "Space", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.SPATIAL), 3, 3),
            glyph("perception", "Perception", GlyphCategory.PRINCIPLE, Set.of(MagicDomain.INFORMATION, MagicDomain.PATTERN), 3, 4),
            glyph("gather", "Gather", GlyphCategory.OPERATION, Set.of(MagicDomain.DAMAGE, MagicDomain.STRUCTURE), 2, 1),
            glyph("shape", "Shape", GlyphCategory.OPERATION, Set.of(MagicDomain.STRUCTURE, MagicDomain.DAMAGE), 2, 2),
            glyph("bind", "Bind", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN), 2, 2),
            glyph("release", "Release", GlyphCategory.OPERATION, Set.of(MagicDomain.DAMAGE), 2, 1),
            glyph("direct", "Direct", GlyphCategory.OPERATION, Set.of(MagicDomain.SPATIAL), 2, 2),
            glyph("anchor", "Anchor", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.STRUCTURE), 2, 3),
            glyph("sustain", "Sustain", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.LIFE), 2, 3),
            glyph("stabilize", "Stabilize", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.INFORMATION), 2, 4),
            glyph("disrupt", "Disrupt", GlyphCategory.OPERATION, Set.of(MagicDomain.DAMAGE, MagicDomain.PATTERN), 2, 2),
            glyph("separate", "Separate", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.SPATIAL), 2, 2),
            glyph("refine", "Refine", GlyphCategory.OPERATION, Set.of(MagicDomain.LIFE, MagicDomain.INFORMATION), 2, 3),
            glyph("restore", "Restore", GlyphCategory.OPERATION, Set.of(MagicDomain.LIFE, MagicDomain.INFORMATION), 2, 3),
            glyph("transfer", "Transfer", GlyphCategory.OPERATION, Set.of(MagicDomain.LIFE, MagicDomain.INFORMATION), 2, 3),
            glyph("strengthen", "Strengthen", GlyphCategory.OPERATION, Set.of(MagicDomain.STRUCTURE, MagicDomain.LIFE), 2, 3),
            glyph("attune", "Attune", GlyphCategory.OPERATION, Set.of(MagicDomain.INFORMATION, MagicDomain.PATTERN), 2, 3),
            glyph("terminate", "Terminate", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN), 2, 2),
            glyph("persist", "Persist", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.SPATIAL), 2, 4),
            glyph("gentle", "Gentle", GlyphCategory.OPERATION, Set.of(MagicDomain.INFORMATION, MagicDomain.PATTERN), 1, 3),
            glyph("unravel", "Unravel", GlyphCategory.OPERATION, Set.of(MagicDomain.PATTERN, MagicDomain.INFORMATION), 2, 3),
            glyph("self", "Self", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.SPATIAL), 1, 1),
            glyph("forward", "Forward", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.SPATIAL), 1, 1),
            glyph("boundary", "Boundary", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.SPATIAL, MagicDomain.PATTERN), 1, 2),
            glyph("field", "Field", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.SPATIAL, MagicDomain.PATTERN), 1, 2),
            glyph("path", "Path", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.SPATIAL), 1, 1),
            glyph("surface", "Surface", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.STRUCTURE), 1, 1),
            glyph("raise", "Raise", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.STRUCTURE, MagicDomain.SPATIAL), 1, 2),
            glyph("carve", "Carve", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.STRUCTURE, MagicDomain.DAMAGE), 1, 2),
            glyph("on_impact", "On Impact", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.DAMAGE, MagicDomain.SPATIAL), 1, 2),
            glyph("on_entry", "On Entry", GlyphCategory.CONSTRAINT, Set.of(MagicDomain.PATTERN, MagicDomain.INFORMATION), 1, 2),
            glyph("chosen_point", "Chosen Point", GlyphCategory.REFERENCE, Set.of(MagicDomain.SPATIAL), 1, 1),
            glyph("caster", "Caster", GlyphCategory.REFERENCE, Set.of(MagicDomain.INFORMATION, MagicDomain.SPATIAL), 1, 1),
            glyph("seen_target", "Seen Target", GlyphCategory.REFERENCE, Set.of(MagicDomain.INFORMATION, MagicDomain.SPATIAL), 1, 2),
            glyph("locking_pattern", "Locking Pattern", GlyphCategory.REFERENCE, Set.of(MagicDomain.PATTERN), 1, 2),
            glyph("life_pattern", "Life Pattern", GlyphCategory.REFERENCE, Set.of(MagicDomain.LIFE, MagicDomain.INFORMATION), 1, 2)
    );

    private static final Map<String, GlyphDefinition> BY_ID = GLYPHS.stream()
            .collect(Collectors.toUnmodifiableMap(GlyphDefinition::id, Function.identity()));

    private CoreGlyphRegistry() {
    }

    public static List<GlyphDefinition> all() {
        return GLYPHS;
    }

    public static Optional<GlyphDefinition> find(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static int size() {
        return GLYPHS.size();
    }

    public static SpellChain chain(String... glyphIds) {
        return new SpellChain(Arrays.stream(glyphIds)
                .map(CoreGlyphRegistry::require)
                .toList());
    }

    private static GlyphDefinition require(String id) {
        GlyphDefinition glyph = BY_ID.get(id);
        if (glyph == null) {
            throw new IllegalArgumentException("Unknown glyph id: " + id);
        }
        return glyph;
    }

    private static GlyphDefinition glyph(
            String id,
            String displayName,
            GlyphCategory category,
            Set<MagicDomain> domainHints,
            int manaWeight,
            int stabilityWeight
    ) {
        return new GlyphDefinition(id, displayName, category, domainHints, manaWeight, stabilityWeight);
    }
}
