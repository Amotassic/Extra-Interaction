package com.amotassic.extra_interaction.api;

import com.amotassic.extra_interaction.interaction.Interaction;
import com.amotassic.extra_interaction.interaction.Interactions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface InteractionRegister {

    void init();

    default Interaction withEntity(BiPredicate<Player, Entity> condition, BiConsumer<Player, Entity> action) {
        return Interaction.withEntity(condition, action);
    }

    default Interaction withBlock(BiPredicate<Player, BlockPos> condition, BiConsumer<Player, BlockPos> action) {
        return Interaction.withBlock(condition, action);
    }

    default void addForBlock(String id, String name, Interaction interaction) {
        Interactions.addForBlock(id, name, interaction);
    }

    default void addForBlocks(Predicate<Block> filter, String name, Interaction interaction) {
        Interactions.addForBlocks(filter, name, interaction);
    }

    default void addForEntity(String id, String name, Interaction interaction) {
        Interactions.addForEntity(id, name, interaction);
    }

    default void addForEntities(Predicate<EntityType<?>> filter, String name, Interaction interaction) {
        Interactions.addForEntities(filter, name, interaction);
    }
}
