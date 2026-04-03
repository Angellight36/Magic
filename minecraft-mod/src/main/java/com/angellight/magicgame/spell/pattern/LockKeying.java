package com.angellight.magicgame.spell.pattern;

/**
 * Shared helpers for keyed locks and linked-key presentation.
 */
public final class LockKeying {
    private static final int DISPLAY_SIGNATURE_LENGTH = 8;

    private LockKeying() {
    }

    /**
     * Returns whether the presented key signature can unlock the stored signature.
     *
     * @param lockSignature signature stored on the lock
     * @param keySignature signature presented by the key
     * @return true when the lock is keyed and the key matches exactly
     */
    public static boolean matches(String lockSignature, String keySignature) {
        return lockSignature != null && lockSignature.equals(keySignature);
    }

    /**
     * Shortens a full key signature for user-facing text.
     *
     * @param signature full stored signature
     * @return abbreviated display-safe signature
     */
    public static String displaySignature(String signature) {
        if (signature == null || signature.isBlank()) {
            return "UNBOUND";
        }
        return signature.length() <= DISPLAY_SIGNATURE_LENGTH
                ? signature.toUpperCase(java.util.Locale.ROOT)
                : signature.substring(0, DISPLAY_SIGNATURE_LENGTH).toUpperCase(java.util.Locale.ROOT);
    }
}
