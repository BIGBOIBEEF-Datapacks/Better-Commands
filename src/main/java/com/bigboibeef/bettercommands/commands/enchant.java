package com.bigboibeef.bettercommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

public class enchant {
    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            entityName -> Text.stringifiedTranslatable("commands.enchant.failed.entity", entityName)
    );
    private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType(
            entityName -> Text.stringifiedTranslatable("commands.enchant.failed.itemless", entityName)
    );
    private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType(
            itemName -> Text.stringifiedTranslatable("commands.enchant.failed.incompatible", itemName)
    );
    private static final Dynamic2CommandExceptionType FAILED_LEVEL_EXCEPTION = new Dynamic2CommandExceptionType(
            (level, maxLevel) -> Text.stringifiedTranslatable("commands.enchant.failed.level", level, maxLevel)
    );
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.enchant.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                CommandManager.literal("enchant")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(
                                CommandManager.argument("targets", EntityArgumentType.entities())
                                        .then(
                                                CommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                                                        .executes(
                                                                context -> execute(
                                                                        context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment"), 1
                                                                )
                                                        )
                                                        .then(
                                                                CommandManager.argument("level", IntegerArgumentType.integer(0))
                                                                        .executes(
                                                                                context -> execute(
                                                                                        context.getSource(),
                                                                                        EntityArgumentType.getEntities(context, "targets"),
                                                                                        RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment"),
                                                                                        IntegerArgumentType.getInteger(context, "level")
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment, int level) throws CommandSyntaxException {
        Enchantment enchantment2 = enchantment.value();
        if (level > enchantment2.getMaxLevel()) {
            throw FAILED_LEVEL_EXCEPTION.create(level, enchantment2.getMaxLevel());
        }

        int i = 0;

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack itemStack = livingEntity.getMainHandStack();
                if (!itemStack.isEmpty()) {
                    ItemEnchantmentsComponent enchantments = itemStack.getEnchantments();
                    ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

                    boolean modified = false;

                    for (RegistryEntry<Enchantment> ench : enchantments.getEnchantments()) {
                        if (ench.equals(enchantment)) {
                            if (level > 0) {
                                builder.set(ench, level);
                                modified = true;
                            } else {
                                modified = true;
                            }
                        } else {
                            builder.set(ench, enchantments.getLevel(ench));
                        }
                    }

                    if (level > 0 && !enchantments.getEnchantments().contains(enchantment)) {
                        builder.set(enchantment, level);
                        modified = true;
                    }

                    if (modified) {
                        ItemEnchantmentsComponent newEnchantments = builder.build();
                        itemStack.set(DataComponentTypes.ENCHANTMENTS, newEnchantments);
                        i++;
                    }

                } else if (targets.size() == 1) {
                    throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                }
            } else if (targets.size() == 1) {
                throw FAILED_ENTITY_EXCEPTION.create(entity.getName().getString());
            }
        }

        if (i == 0) {
            throw FAILED_EXCEPTION.create();
        } else {
            if (targets.size() == 1) {
                source.sendFeedback(
                        () -> Text.translatable("commands.enchant.success.single", Enchantment.getName(enchantment, level), targets.iterator().next().getDisplayName()),
                        true
                );
            } else {
                source.sendFeedback(() -> Text.translatable("commands.enchant.success.multiple", Enchantment.getName(enchantment, level), targets.size()), true);
            }
            return i;
        }
    }

}