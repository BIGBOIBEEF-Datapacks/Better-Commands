package com.bigboibeef.bettercommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Formatting;

import java.util.Collection;
public class damage {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("damage").requires((source) -> source.hasPermissionLevel(2))).then(CommandManager.argument("target", EntityArgumentType.entities()).then(((RequiredArgumentBuilder) CommandManager.argument("amount", FloatArgumentType.floatArg(0.0F)).executes((context) -> execute((ServerCommandSource) context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "amount"), ((ServerCommandSource) context.getSource()).getWorld().getDamageSources().generic())).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) CommandManager.argument("damageType", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.DAMAGE_TYPE)).executes((context) -> execute((ServerCommandSource) context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE))))).then(CommandManager.literal("at").then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes((context) -> execute((ServerCommandSource) context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), Vec3ArgumentType.getVec3(context, "location"))))))).then(CommandManager.literal("by").then(((RequiredArgumentBuilder) CommandManager.argument("entity", EntityArgumentType.entity()).executes((context) -> execute((ServerCommandSource) context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity"))))).then(CommandManager.literal("from").then(CommandManager.argument("cause", EntityArgumentType.entity()).executes((context) -> execute((ServerCommandSource) context.getSource(), EntityArgumentType.getEntities(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity"), EntityArgumentType.getEntity(context, "cause")))))))))))));
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, float amount, DamageSource damageSource) throws CommandSyntaxException {
        int successCount = 0;
        ServerPlayerEntity sourcePlayer = source.getPlayer();

        for (Entity target : targets) {
            if (target instanceof ServerPlayerEntity targetPlayer && sourcePlayer != null) {
                float newHealth = targetPlayer.getHealth() - amount;
                targetPlayer.setAttacker(sourcePlayer);
                targetPlayer.setHealth(Math.max(newHealth, 0.1F));
                if (targetPlayer.getHealth() == 0.1F) {
                    targetPlayer.damage(damageSource, Math.max(newHealth, 0.1F));
                    if (!targetPlayer.isDead()) {
                        targetPlayer.kill();
                    }

                    for (ServerPlayerEntity player : sourcePlayer.getServer().getPlayerManager().getPlayerList()) {
                        player.sendMessage(Text.literal(targetPlayer.getName().getLiteralString() + " was killed by " + sourcePlayer.getName().getLiteralString() + " using their mob ability.").styled(style -> style.withColor(Formatting.WHITE)));
                    }
                }
            } else {
                target.damage(damageSource, amount);
            }

            source.sendFeedback(() -> Text.translatable("commands.damage.success", amount, target.getDisplayName()), true);
            successCount++;
        }

        return successCount;
    }
}