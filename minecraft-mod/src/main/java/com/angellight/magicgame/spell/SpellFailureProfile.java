package com.angellight.magicgame.spell;

/**
 * First-pass profile describing the most likely failure shape for a spell.
 *
 * @param failureType broad failure class
 * @param severity rough 1-5 severity estimate
 * @param domain domain most likely involved in the failure
 * @param description short explanation of why this failure is likely
 * @param gameplayOutcome expected gameplay-facing result if the failure manifests
 */
public record SpellFailureProfile(
        SpellFailureType failureType,
        int severity,
        MagicDomain domain,
        String description,
        String gameplayOutcome
) {
    public SpellFailureProfile {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("Failure severity must be between 1 and 5.");
        }
    }
}
