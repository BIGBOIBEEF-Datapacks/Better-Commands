package com.bigboibeef.bettercommands;

import com.bigboibeef.bettercommands.commands.damage;
import com.bigboibeef.bettercommands.commands.enchant;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegister {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            damage.register(dispatcher, registryAccess);
            enchant.register(dispatcher, registryAccess);
        });
    }
}