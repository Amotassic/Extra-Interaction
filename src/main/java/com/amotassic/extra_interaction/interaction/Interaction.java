package com.amotassic.extra_interaction.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public record Interaction(BiPredicate<Player, Object> condition, BiConsumer<Player, Object> action) {
    public static final Interaction EMPTY = new Interaction((_, _) -> true, (_, _) -> {});

    public static Interaction withEntity(BiPredicate<Player, Entity> condition, BiConsumer<Player, Entity> action) {
        return new Interaction(
                (player, o) -> o instanceof Entity entity && condition.test(player, entity),
                (player, o) -> {if (o instanceof Entity entity) action.accept(player, entity);}
        );
    }

    public static Interaction withBlock(BiPredicate<Player, BlockPos> condition, BiConsumer<Player, BlockPos> action) {
        return new Interaction(
                (player, o) -> o instanceof BlockPos pos && condition.test(player, pos),
                (player, o) -> {if (o instanceof BlockPos pos) action.accept(player, pos);}
        );
    }

    public boolean test(Player player, Object blockPosOrEntity) {return condition.test(player, blockPosOrEntity);}

    public void accept(Player player, Object blockPosOrEntity) {action.accept(player, blockPosOrEntity);}
}
