package com.amotassic.extra_interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimplePlaceholder {
    static final Pattern holderPattern = Pattern.compile("<((?![<>]).)*>");
    static final HashMap<String, BiFunction<Player, Object, String>> resolvers = new HashMap<>();

    public static void addResolver(String name, BiFunction<Player, Object, String> function) {resolvers.put(name, function);}

    static {
        addResolver("name", (p, o) -> {
            if (o instanceof Entity entity) return entity.getName().getString();
            if (o instanceof BlockPos pos) return Component.translatable(p.level().getBlockState(pos).getBlock().getDescriptionId()).getString();
            return null;
        });
        addResolver("uuid", (_, o) -> o instanceof Entity entity ? entity.getUUID().toString() : null);
        addResolver("pos", (_, o) -> {
            if (o instanceof Entity entity) return entity.blockPosition().toShortString().replace(",", "");
            if (o instanceof BlockPos pos) return pos.toShortString().replace(",", "");
            return null;
        });

        addResolver("player.name", (p, _) -> resolvers.get("name").apply(p, p));
        addResolver("player.uuid", (p, _) -> resolvers.get("uuid").apply(p, p));
        addResolver("player.pos", (p, _) -> resolvers.get("pos").apply(p, p));
    }

    public static String resolve(Player player, Object target, String input) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = holderPattern.matcher(input);
        int lastIndex = 0;
        while (matcher.find()) {
            sb.append(input, lastIndex, matcher.start()); // 追加匹配前的文本
            lastIndex = matcher.end();
            String group = matcher.group();
            String placeholder = group.substring(1, group.length() - 1); // 去掉尖括号
            String parsed;
            var resolver = resolvers.get(placeholder);
            if (resolver == null) parsed = null; else parsed = resolver.apply(player, target);
            sb.append(parsed == null ? group : parsed);
        }
        sb.append(input.substring(lastIndex)); // 追加剩余文本
        return sb.toString();
    }
}
