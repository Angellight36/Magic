package com.anthony.magicgame;

import com.anthony.magicgame.command.MagicCommand;
import com.anthony.magicgame.command.MagicStatusCommand;
import com.anthony.magicgame.debug.LockDebugTicker;
import com.anthony.magicgame.item.MagicItems;
import com.anthony.magicgame.mana.ManaRegenerationService;
import com.anthony.magicgame.network.MagicNetworking;
import com.anthony.magicgame.spell.effect.AnchoredEffectTicker;
import com.anthony.magicgame.spell.pattern.BlockPatternTagTicker;
import com.anthony.magicgame.spell.pattern.LockingPatternInteractionGuard;
import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import com.anthony.magicgame.spell.registry.PrototypeSpellRegistry;
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
