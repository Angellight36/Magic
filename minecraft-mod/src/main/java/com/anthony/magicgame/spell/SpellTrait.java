package com.anthony.magicgame.spell;

/**
 * Interpretable semantic traits that can coexist within a single spell chain.
 *
 * <p>These traits are intentionally lower-level than {@link SpellIntent}. They let downstream systems
 * reason about mixed-purpose chains such as sacrificial healing, where the spell is simultaneously
 * restorative, transfer-oriented, and self-sourcing.</p>
 */
public enum SpellTrait {
    ANCHORED,
    BOUNDED_AREA,
    FIELD_EFFECT,
    TRAVELING_DELIVERY,
    RESTORATIVE,
    VITALITY_TRANSFER,
    PATTERN_SENSITIVE,
    DISRUPTIVE,
    STRUCTURAL_SHAPING,
    SELF_REFERENCE,
    TARGETED_REFERENCE,
    PERSISTENT,
    ATTUNED_OWNER
}
