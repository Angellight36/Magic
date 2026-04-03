package com.angellight.magicgame.network;

import com.angellight.magicgame.MagicGameMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Sends short-lived spell status text to the client so the prototype can show recent cast/analyze feedback on the HUD.
 *
 * @param title compact headline for the recent spell event
 * @param detail additional details about the spell result
 * @param displayTicks how long the feedback should remain visible
 */
public record SpellFeedbackPayload(String title, String detail, int displayTicks) implements CustomPacketPayload {
    public static final Identifier PAYLOAD_ID =
            Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "spell_feedback");
    public static final Type<SpellFeedbackPayload> ID = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellFeedbackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellFeedbackPayload::title,
            ByteBufCodecs.STRING_UTF8, SpellFeedbackPayload::detail,
            ByteBufCodecs.INT, SpellFeedbackPayload::displayTicks,
            SpellFeedbackPayload::new
    );

    public SpellFeedbackPayload {
        title = title == null ? "" : title;
        detail = detail == null ? "" : detail;
        if (displayTicks < 0) {
            throw new IllegalArgumentException("displayTicks must not be negative.");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
