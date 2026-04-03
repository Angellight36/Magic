package com.angellight.magicgame.spell;

/**
 * Broad internal intent buckets used to route prototype spells before deeper domain execution exists.
 *
 * <p>Unlike {@link SpellTrait}, intent is the best-fit dominant outcome rather than a full description
 * of everything a chain is trying to do.</p>
 */
public enum SpellIntent {
    TRAVELING_EFFECT,
    BOUNDARY_WARD,
    PATTERN_INTERACTION,
    RESTORATION_EFFECT,
    VITALITY_TRANSFER,
    CONSTRUCTION_EFFECT,
    UNKNOWN_UNSTABLE
}
