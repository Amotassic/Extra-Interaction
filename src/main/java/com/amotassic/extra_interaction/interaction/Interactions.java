package com.amotassic.extra_interaction.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class Interactions {
    static final Map<String, Map<String, Interaction>> BLOCK_INTERACTIONS = new HashMap<>();
    static final Map<String, Map<String, Interaction>> ENTITY_INTERACTIONS = new HashMap<>();
    static final Map<String, Interaction> EMPTY_MAP = Map.of();

    public static void addForBlock(String id, String name, Interaction interaction) {
        if (name.isEmpty()) return;
        BLOCK_INTERACTIONS.computeIfAbsent(id, _ -> new LinkedHashMap<>()).put(name, interaction);
    }

    public static void addForBlocks(Predicate<Block> filter, String name, Interaction interaction) {
        BuiltInRegistries.BLOCK.stream().filter(filter).forEach(block -> addForBlock(getBlockId(block), name, interaction));
    }

    public static void addForEntity(String id, String name, Interaction interaction) {
        if (name.isEmpty()) return;
        ENTITY_INTERACTIONS.computeIfAbsent(id, _ -> new LinkedHashMap<>()).put(name, interaction);
    }

    public static void addForEntities(Predicate<EntityType<?>> filter, String name, Interaction interaction) {
        BuiltInRegistries.ENTITY_TYPE.stream().filter(filter).forEach(type -> addForEntity(getEntityId(type), name, interaction));
    }

    static void getInteractions(Player player, BlockPos pos, List<InteractionRec> list, boolean showAll) {
        var id = getBlockId(player.level().getBlockState(pos));
        BLOCK_INTERACTIONS.getOrDefault(id, EMPTY_MAP).forEach((name, interaction) -> {
            boolean canUse = interaction.test(player, pos);
            if (canUse || showAll) list.add(new InteractionRec(pos, name, interaction, canUse));
        });
    }
    static void getInteractions(Player player, Entity entity, List<InteractionRec> list, boolean showAll) {
        var id = getEntityId(entity);
        ENTITY_INTERACTIONS.getOrDefault(id, EMPTY_MAP).forEach((name, interaction) -> {
            boolean canUse = interaction.test(player, entity);
            if (canUse || showAll) list.add(new InteractionRec(entity, name, interaction, canUse));
        });
    }
    public static void getInteractions(Player player, Object o, List<InteractionRec> list, boolean showAll) {
        if (o instanceof BlockPos pos) getInteractions(player, pos, list, showAll);
        else if (o instanceof Entity entity) getInteractions(player, entity, list, showAll);
    }

    static void applyAction(Player player, BlockPos pos, String name) {
        BLOCK_INTERACTIONS.getOrDefault(getBlockId(player.level().getBlockState(pos)), EMPTY_MAP).getOrDefault(name, Interaction.EMPTY).accept(player, pos);
    }
    static void applyAction(Player player, Entity entity, String name) {
        ENTITY_INTERACTIONS.getOrDefault(getEntityId(entity), EMPTY_MAP).getOrDefault(name, Interaction.EMPTY).accept(player, entity);
    }
    public static void applyAction(Player player, Object blockPosOrEntity, String name) {
        if (blockPosOrEntity instanceof BlockPos pos) { applyAction(player, pos, name); return; }
        Entity entity = null;
        if (blockPosOrEntity instanceof Entity e) entity = e;
        else if (blockPosOrEntity instanceof Integer id) entity = player.level().getEntity(id);
        if (entity != null) applyAction(player, entity, name);
    }

    public static String getBlockId(Block block) {return BuiltInRegistries.BLOCK.getKey(block).toString();}
    public static String getBlockId(BlockState state) {return getBlockId(state.getBlock());}

    public static String getEntityId(EntityType<?> type) {return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();}
    public static String getEntityId(Entity entity) {return getEntityId(entity.getType());}
}
