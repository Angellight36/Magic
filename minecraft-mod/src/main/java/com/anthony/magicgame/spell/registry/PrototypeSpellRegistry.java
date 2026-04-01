package com.anthony.magicgame.spell.registry;

import com.anthony.magicgame.spell.PrototypeSpellDefinition;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of command-driven prototype spells sourced from the current design documents.
 */
public final class PrototypeSpellRegistry {
    private static final List<PrototypeSpellDefinition> SPELLS = List.of(
            new PrototypeSpellDefinition(
                    "fireball",
                    "Fireball",
                    "Contained fire released on impact.",
                    List.of("gather", "fire", "shape", "separate", "forward", "direct", "stabilize", "on_impact", "release")
            ),
            new PrototypeSpellDefinition(
                    "healing_touch",
                    "Healing Touch",
                    "A first-pass healing chain for living targets.",
                    List.of("perception", "life", "life_pattern", "refine", "strengthen", "stabilize")
            ),
            new PrototypeSpellDefinition(
                    "unlock",
                    "Unlock",
                    "Pattern-focused separation of a locking structure.",
                    List.of("perception", "order", "binding", "locking_pattern", "separate")
            ),
            new PrototypeSpellDefinition(
                    "alert_ward",
                    "Alert Ward",
                    "Anchored boundary field that watches for entry.",
                    List.of("anchor", "boundary", "field", "perception", "on_entry", "attune", "caster", "bind")
            ),
            new PrototypeSpellDefinition(
                    "stone_path",
                    "Stone Path",
                    "Simple construction chain for shaping grounded structure.",
                    List.of("earth", "shape", "path", "surface", "anchor", "strengthen", "stabilize")
            )
    );

    private static final Map<String, PrototypeSpellDefinition> BY_ID = SPELLS.stream()
            .collect(Collectors.toUnmodifiableMap(PrototypeSpellDefinition::id, Function.identity()));

    private PrototypeSpellRegistry() {
    }

    public static List<String> ids() {
        return SPELLS.stream()
                .map(PrototypeSpellDefinition::id)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public static PrototypeSpellDefinition require(String id) {
        PrototypeSpellDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown prototype spell: " + id);
        }
        return definition;
    }

    public static int size() {
        return SPELLS.size();
    }
}