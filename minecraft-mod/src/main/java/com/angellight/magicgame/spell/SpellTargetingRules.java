package com.angellight.magicgame.spell;

/**
 * Scored targeting rules used by the command-driven prototype runtime.
 *
 * <p>These rules intentionally expose weighted ambiguity rather than collapsing under-specified
 * spells into a single hardcoded recipient. Runtime code can then pick among valid candidates
 * using weighted randomness.</p>
 */
public final class SpellTargetingRules {
    private SpellTargetingRules() {
    }

    /**
     * Scores possible recipients for restoration-like spells.
     *
     * <p>Self and the current look target are both treated as viable candidates unless the chain
     * strongly biases one over the other. This makes under-specified healing behavior feel magical
     * and uncertain instead of silently pretending the spell was explicit.</p>
     *
     * @param spell source spell chain
     * @param hasLookTarget whether a valid looked-at living target exists right now
     * @return candidate weights for recipient selection
     */
    public static RestorationTargetWeights scoreRestorationTargets(SpellChain spell, boolean hasLookTarget) {
        return SpellFlowRules.restorationTargetWeights(SpellFlowRules.scoreRecipients(spell), hasLookTarget);
    }

    /**
     * Resolves a recipient using a caller-provided weighted roll.
     *
     * @param weights previously scored target weights
     * @param roll zero-based roll in the range {@code [0, weights.totalWeight())}
     * @return selected recipient bucket
     */
    public static RestorationTargetChoice chooseRestorationTarget(RestorationTargetWeights weights, int roll) {
        int totalWeight = weights.totalWeight();
        if (totalWeight <= 0) {
            return RestorationTargetChoice.NONE;
        }
        if (roll < 0 || roll >= totalWeight) {
            throw new IllegalArgumentException("Roll must be within the total target weight.");
        }
        if (roll < weights.selfWeight()) {
            return RestorationTargetChoice.SELF;
        }
        if (roll < weights.selfWeight() + weights.lookTargetWeight()) {
            return RestorationTargetChoice.LOOK_TARGET;
        }
        return RestorationTargetChoice.NONE;
    }
    /**
     * Weighted recipient candidates for restoration-like spells.
     *
     * @param selfWeight weight assigned to the caster as recipient
     * @param lookTargetWeight weight assigned to the currently looked-at living target
     */
    public record RestorationTargetWeights(int selfWeight, int lookTargetWeight) {
        public RestorationTargetWeights {
            if (selfWeight < 0 || lookTargetWeight < 0) {
                throw new IllegalArgumentException("Target weights must be non-negative.");
            }
        }

        public int totalWeight() {
            return selfWeight + lookTargetWeight;
        }
    }

    /**
     * Outcomes for the current prototype restoration targeting policy.
     */
    public enum RestorationTargetChoice {
        SELF,
        LOOK_TARGET,
        NONE
    }
}
