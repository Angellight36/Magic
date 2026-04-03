package com.angellight.magicgame;

import com.angellight.magicgame.command.MagicCommand;
import com.angellight.magicgame.command.MagicStatusCommand;
import com.angellight.magicgame.debug.LockDebugTicker;
import com.angellight.magicgame.item.MagicItems;
import com.angellight.magicgame.mana.ManaRegenerationService;
import com.angellight.magicgame.network.MagicNetworking;
import com.angellight.magicgame.spell.effect.AnchoredEffectTicker;
import com.angellight.magicgame.spell.pattern.BlockPatternTagTicker;
import com.angellight.magicgame.spell.pattern.LockingPatternInteractionGuard;
import com.angellight.magicgame.spell.registry.CoreGlyphRegistry;
import com.angellight.magicgame.spell.registry.PrototypeSpellRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for the playable Magic prototype mod.
 */
public final class MagicGameMod implements ModInitializer {
    public static final String MOD_ID = "magicgame";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MagicItems.register();
        MagicNetworking.register();
        ManaRegenerationService.register();
        AnchoredEffectTicker.register();
        BlockPatternTagTicker.register();
        LockDebugTicker.register();
        LockingPatternInteractionGuard.register();
        MagicCommand.register();
        MagicStatusCommand.register();

        LOGGER.info("Loading Magic prototype on Fabric.");
        LOGGER.info(
                "Bootstrapped {} prototype glyphs and {} prototype spells for the first playable slice.",
                CoreGlyphRegistry.size(),
                PrototypeSpellRegistry.size()
        );
    }
}

