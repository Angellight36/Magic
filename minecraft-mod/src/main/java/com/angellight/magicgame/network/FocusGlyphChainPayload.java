package com.angellight.magicgame.network;

import com.angellight.magicgame.MagicGameMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

/**
 * Carries the currently written glyph chain for a held focus so the server can persist it on the item stack.
 *
 * @param hand hand containing the focus being edited
 * @param glyphIds ordered glyph ids stored on that focus
 */
public record FocusGlyphChainPayload(InteractionHand hand, List<String> glyphIds) implements CustomPacketPayload {
    public static final Identifier PAYLOAD_ID =
            Identifier.fromNamespaceAndPath(MagicGameMod.MOD_ID, "focus_glyph_chain");
    public static final Type<FocusGlyphChainPayload> ID = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, FocusGlyphChainPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, payload -> payload.hand().name(),
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), payload -> new ArrayList<>(payload.glyphIds()),
            (handName, glyphIds) -> new FocusGlyphChainPayload(InteractionHand.valueOf(handName), glyphIds)
    );

    public FocusGlyphChainPayload {
        glyphIds = glyphIds.stream()
                .filter(glyphId -> glyphId != null && !glyphId.isBlank())
                .toList();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
