package com.angellight.magicgame.command;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

/**
 * Keeps the original bootstrap alias available while the fuller command tree lives under {@code /magic}.
 */
public final class MagicStatusCommand {
    private MagicStatusCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("magicstatus")
                        .executes(MagicCommand::showStatus)
        ));
    }
}