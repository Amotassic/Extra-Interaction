package com.amotassic.extra_interaction.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class Interaction {
    public static final Interaction EMPTY = withAny((_, _) -> true, (_, _) -> {});
    public final List<String> tips = new ArrayList<>();
    public @Nullable Identifier icon;
    public final BiPredicate<Player, Object> condition;
    public final BiConsumer<Player, Object> action;

    Interaction(BiPredicate<Player, Object> condition, BiConsumer<Player, Object> action) {
        this.condition = condition;
        this.action = action;
    }

    public static Interaction withAny(BiPredicate<Player, Object> condition, BiConsumer<Player, Object> action) {
        return new Interaction(condition, action);
    }

    public static Interaction withEntity(BiPredicate<Player, Entity> condition, BiConsumer<Player, Entity> action) {
        return withAny(
                (player, o) -> o instanceof Entity entity && condition.test(player, entity),
                (player, o) -> {if (o instanceof Entity entity) action.accept(player, entity);}
        );
    }

    public static Interaction withBlock(BiPredicate<Player, BlockPos> condition, BiConsumer<Player, BlockPos> action) {
        return withAny(
                (player, o) -> o instanceof BlockPos pos && condition.test(player, pos),
                (player, o) -> {if (o instanceof BlockPos pos) action.accept(player, pos);}
        );
    }

    public Interaction copy() {return withAny(condition, action).tips(tips).icon(icon);}

    public Interaction tips(@NonNull List<String> tips) {
        this.tips.addAll(tips);
        return this;
    }
    public Interaction tips(@NonNull String... tips) {return tips(Arrays.stream(tips).toList());}

    public String getTip() {
        if (tips.isEmpty()) return "extra_interaction.default_tip" + new Random().nextInt(3);
        return tips.get(new Random().nextInt(tips.size()));
    }

    public Interaction icon(@Nullable Identifier icon) {
        this.icon = icon;
        return this;
    }

    public boolean test(Player player, Object blockPosOrEntity) {return condition.test(player, blockPosOrEntity);}

    public void accept(Player player, Object blockPosOrEntity) {
        if (test(player, blockPosOrEntity)) action.accept(player, blockPosOrEntity);
    }
}
