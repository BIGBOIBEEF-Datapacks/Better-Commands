package com.bigboibeef.bettercommands.commands;

import com.bigboibeef.bettercommands.CommandRegister;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.bigboibeef.bettercommands.BetterCommands.LOGGER;

public class summon {

    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType FAILED_UUID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("commands.summon.invalidPosition")
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                CommandManager.literal("summon")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("entity", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE))
                                        .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes(
                                                context -> execute(
                                                        context.getSource(),
                                                        RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                        context.getSource().getPosition(),
                                                        new NbtCompound(),
                                                        true
                                                )
                                        )
                                        .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(
                                                                context -> execute(
                                                                        context.getSource(),
                                                                        RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                        Vec3ArgumentType.getVec3(context, "pos"),
                                                                        new NbtCompound(),
                                                                        true
                                                                )
                                                        )
                                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                                                        .executes(
                                                                                context -> summunMultiple(
                                                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                                                        context.getSource(),
                                                                                        RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                        Vec3ArgumentType.getVec3(context, "pos"),
                                                                                        new NbtCompound(),
                                                                                        false
                                                                                )
                                                                        )



                                                                        //NBT STARTS HERE :) (incomplete)
                                                                        /*
                                                                        Tags	List of Strings	"Tags": ["example", "mob_tag"]
                                                                        Team	String	"Team": "red"
                                                                        UUID	IntArray (4 ints)	"UUID": [I;123456789,987654321,111213141,151617181]






                                                                        ActiveEffects Format (List of Compounds)
                                                                        Id: Byte — potion effect ID (or use id as a String in modern Minecraft)

                                                                            Amplifier: Byte — effect strength (0 = level 1)

                                                                            Duration: Int — ticks (20 ticks = 1 second)

                                                                            Ambient: Byte — 1b if from a beacon or similar

                                                                            ShowParticles: Byte — 1b if particles should be visible

                                                                            ShowIcon: Byte — 1b if HUD icon is shown (optional)

                                                                         EXAMPLES:
                                                                         "ActiveEffects": [
                                                                          {
                                                                            "Id": 1b,
                                                                            "Amplifier": 1b,
                                                                            "Duration": 600,
                                                                            "Ambient": 0b,
                                                                            "ShowParticles": 1b
                                                                          }
                                                                        ]

                                                                        "ActiveEffects": [
                                                                          {
                                                                            "id": "minecraft:speed",

                                                                            "amplifier": 1b,
                                                                            "duration": 600
                                                                          }
                                                                        ]
                                                                         */
                                                                        .then(CommandManager.argument("name", StringArgumentType.string())
                                                                                        .executes(
                                                                                                context -> {
                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));

                                                                                                    return summunMultiple(
                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                            context.getSource(),
                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                            nbt,
                                                                                                            false
                                                                                                    );
                                                                                                }
                                                                                        )
                                                                                        .then(CommandManager.argument("custom name visible", BoolArgumentType.bool())
                                                                                                        .executes(
                                                                                                                context -> {
                                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));
                                                                                                                    nbt.putBoolean("CustomNameVisible", BoolArgumentType.getBool(context, "custom name visible"));

                                                                                                                    return summunMultiple(
                                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                                            context.getSource(),
                                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                                            nbt,
                                                                                                                            false
                                                                                                                    );
                                                                                                                }
                                                                                                        )
                                                                                                        .then(CommandManager.argument("silent", BoolArgumentType.bool())
                                                                                                                        .executes(
                                                                                                                                context -> {
                                                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));
                                                                                                                                    nbt.putBoolean("CustomNameVisible", BoolArgumentType.getBool(context, "custom name visible"));
                                                                                                                                    nbt.putBoolean("Silent", BoolArgumentType.getBool(context, "silent"));

                                                                                                                                    return summunMultiple(
                                                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                                                            context.getSource(),
                                                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                                                            nbt,
                                                                                                                                            false
                                                                                                                                    );
                                                                                                                                }
                                                                                                                        )
                                                                                                                        .then(CommandManager.argument("invulnerable", BoolArgumentType.bool())
                                                                                                                                        .executes(
                                                                                                                                                context -> {
                                                                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));
                                                                                                                                                    nbt.putBoolean("CustomNameVisible", BoolArgumentType.getBool(context, "custom name visible"));
                                                                                                                                                    nbt.putBoolean("Silent", BoolArgumentType.getBool(context, "silent"));
                                                                                                                                                    nbt.putBoolean("Invulnerable", BoolArgumentType.getBool(context, "invulnerable"));

                                                                                                                                                    return summunMultiple(
                                                                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                                                                            context.getSource(),
                                                                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                                                                            nbt,
                                                                                                                                                            false
                                                                                                                                                    );
                                                                                                                                                }
                                                                                                                                        )
                                                                                                                                        .then(CommandManager.argument("no gravity", BoolArgumentType.bool())
                                                                                                                                                        .executes(
                                                                                                                                                                context -> {
                                                                                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));
                                                                                                                                                                    nbt.putBoolean("CustomNameVisible", BoolArgumentType.getBool(context, "custom name visible"));
                                                                                                                                                                    nbt.putBoolean("Silent", BoolArgumentType.getBool(context, "silent"));
                                                                                                                                                                    nbt.putBoolean("Invulnerable", BoolArgumentType.getBool(context, "invulnerable"));
                                                                                                                                                                    nbt.putBoolean("NoGravity", BoolArgumentType.getBool(context, "no gravity"));

                                                                                                                                                                    return summunMultiple(
                                                                                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                                                                                            context.getSource(),
                                                                                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                                                                                            nbt,
                                                                                                                                                                            false
                                                                                                                                                                    );
                                                                                                                                                                }
                                                                                                                                                        )
                                                                                                                                                        .then(CommandManager.argument("glowing", BoolArgumentType.bool())
                                                                                                                                                                        .executes(
                                                                                                                                                                                context -> {
                                                                                                                                                                                    NbtCompound nbt = new NbtCompound();
                                                                                                                                                                                    nbt.putString("CustomName", Text.Serialization.toJsonString(Text.literal(StringArgumentType.getString(context, "name")), registryAccess));
                                                                                                                                                                                    nbt.putBoolean("CustomNameVisible", BoolArgumentType.getBool(context, "custom name visible"));
                                                                                                                                                                                    nbt.putBoolean("Silent", BoolArgumentType.getBool(context, "silent"));
                                                                                                                                                                                    nbt.putBoolean("Invulnerable", BoolArgumentType.getBool(context, "invulnerable"));
                                                                                                                                                                                    nbt.putBoolean("NoGravity", BoolArgumentType.getBool(context, "no gravity"));
                                                                                                                                                                                    nbt.putBoolean("Glowing", BoolArgumentType.getBool(context, "glowing"));

                                                                                                                                                                                    return summunMultiple(
                                                                                                                                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                                                                                                                                            context.getSource(),
                                                                                                                                                                                            RegistryEntryReferenceArgumentType.getSummonableEntityType(context, "entity"),
                                                                                                                                                                                            Vec3ArgumentType.getVec3(context, "pos"),
                                                                                                                                                                                            nbt,
                                                                                                                                                                                            false
                                                                                                                                                                                    );
                                                                                                                                                                                }
                                                                                                                                                                        )
                                                                                                                                                        )
                                                                                                                                        )
                                                                                                                        )
                                                                                                        )
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    public static Entity summon(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (!World.isValid(blockPos)) {
            throw INVALID_POSITION_EXCEPTION.create();
        } else {
            NbtCompound nbtCompound = nbt.copy();
            nbtCompound.putString("id", entityType.registryKey().getValue().toString());
            ServerWorld serverWorld = source.getWorld();
            Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, serverWorld, entityx -> {
                entityx.refreshPositionAndAngles(pos.x, pos.y, pos.z, entityx.getYaw(), entityx.getPitch());
                return entityx;
            });
            if (entity == null) {
                throw FAILED_EXCEPTION.create();
            } else {
                if (initialize && entity instanceof MobEntity) {
                    ((MobEntity)entity).initialize(source.getWorld(), source.getWorld().getLocalDifficulty(entity.getBlockPos()), SpawnReason.COMMAND, null);
                }

                if (!serverWorld.spawnNewEntityAndPassengers(entity)) {
                    throw FAILED_UUID_EXCEPTION.create();
                } else {
                    return entity;
                }
            }
        }
    }

    private static int execute(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        Entity entity = summon(source, entityType, pos, nbt, initialize);
        source.sendFeedback(() -> Text.translatable("commands.summon.success", entity.getDisplayName()), true);
        return 1;
    }

    private static int summunMultiple (int amount, ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        int count = 0;
        for (int i = 0; i < amount; i++) {
                execute(source, entityType, pos, nbt, initialize);
                count++;
        }
        return count;
    }
}
