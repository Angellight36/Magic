package com.angellight.magicgame.spell.pattern;

/**
 * Small pure rules for local lock interaction decisions so keyed and denied interaction behavior stays testable.
 */
public final class LockingPatternInteractionRules {
    private LockingPatternInteractionRules() {
    }

    public static boolean shouldBlockClientInteraction(String requiredSignature, String presentedSignature) {
        if (requiredSignature == null) {
            return true;
        }
        return !LockKeying.matches(requiredSignature, presentedSignature);
    }
}
