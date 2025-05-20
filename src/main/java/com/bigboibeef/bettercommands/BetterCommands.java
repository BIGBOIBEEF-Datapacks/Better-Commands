package com.bigboibeef.bettercommands;

import com.bigboibeef.bettercommands.commands.damage;
import com.bigboibeef.bettercommands.commands.enchant;
import com.bigboibeef.bettercommands.commands.summon;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterCommands implements ModInitializer {
	public static final String MOD_ID = "better-commands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegister.registerCommands();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.getRoot().getChildren().removeIf(node ->
					node instanceof LiteralCommandNode<?> literal && (
							literal.getLiteral().equals("damage") ||
									literal.getLiteral().equals("enchant") ||
									literal.getLiteral().equals("summon")
					)
			);


			damage.register(dispatcher, registryAccess);
			enchant.register(dispatcher, registryAccess);
			summon.register(dispatcher, registryAccess);
		});
	}
}
