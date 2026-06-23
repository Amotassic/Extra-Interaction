package com.amotassic.extra_interaction.interaction;

import com.amotassic.extra_interaction.ExtraInteraction;
import com.amotassic.extra_interaction.SimplePlaceholder;
import com.google.gson.*;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntityTalkData extends SimplePreparableReloadListener<Map<Identifier, String>> {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<Identifier, String> entityTalkData = new HashMap<>();

    @Override
    protected Map<Identifier, String> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, String> map = new HashMap<>();
        FileToIdConverter jsonConverter = FileToIdConverter.json("extra_interaction/entity_talk");
        for (var entry : jsonConverter.listMatchingResources(manager).entrySet()) {
            var resourceLocation = entry.getKey();
            var rl = jsonConverter.fileToId(resourceLocation);

            try (var resource = entry.getValue().openAsReader()) {
                var json = resource.lines().collect(Collectors.joining("\n"));
                map.put(rl, json);
            } catch (IOException e) {
                ExtraInteraction.LOGGER.error("Error loading data from {}: {}", rl, e.getMessage());
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, String> preparations, ResourceManager manager, ProfilerFiller profiler) {
        entityTalkData.clear();
        entityTalkData.putAll(preparations);
        loadEntityTalk(preparations);
    }

    public static void loadEntityTalk(Map<Identifier, String> data) {
        Interactions.ENTITY_INTERACTIONS.values().forEach(map -> map.remove("<name>"));
        data.forEach((identifier, s) -> {
            try {
                JsonObject json = GSON.fromJson(s, JsonObject.class);
                JsonArray talks = json.get("talks").getAsJsonArray();
                for (var e : talks) {
                    var ids = getFieldAsStrings(e, "id");
                    var names = getFieldAsStrings(e, "name");
                    var commands = getFieldAsStrings(e, "commands");
                    for (String id : ids) {
                        for (String name : names) {
                            var namesMap = entityTalks.getOrDefault(id, new HashMap<>());
                            namesMap.put(name, commands);
                            entityTalks.put(id, namesMap);
                        }
                    }
                }
            } catch (Exception e) {
                ExtraInteraction.LOGGER.error("Parsing error loading entity talk {}: {}", identifier, e.getMessage());
            }
        });
        entityTalks.keySet().forEach(id -> Interactions.addForEntity(id, "<name>", talk_interaction));
    }

    static List<String> getFieldAsStrings(JsonElement element, String fieldName) {
        var ids = new ArrayList<String>();
        if (element instanceof JsonObject object) {
            JsonElement id = object.get(fieldName);
            if (id.isJsonPrimitive()) ids.add(id.getAsString());
            else if (id instanceof JsonArray array) for (var a : array) ids.add(a.getAsString());
        }
        return ids;
    }

    static final Map<String, Map<String, List<String>>> entityTalks = new HashMap<>();
    static final Interaction talk_interaction = Interaction.withEntity(
            (_, entity) -> {
                var id = Interactions.getEntityId(entity);
                var name = entity.getName().getString();
                return entityTalks.getOrDefault(id, Map.of()).containsKey(name);
            },
            (player, entity) -> {
                if (player.level().isClientSide()) return;
                var id = Interactions.getEntityId(entity);
                var name = entity.getName().getString();
                var commands = entityTalks.getOrDefault(id, Map.of()).getOrDefault(name, List.of());
                if (!commands.isEmpty()) {
                    var s = commands.get(new Random().nextInt(commands.size()));
                    var command = SimplePlaceholder.resolve(player, entity, s);
                    executeCommands((ServerPlayer) player, command);
                }
            }
    ).icon(ExtraInteraction.id("textures/talk.png")).tips("跟特定名称的实体交互可以触发特殊剧情（");

    public static void executeCommands(ServerPlayer player, String value) {
        var commands = value.split(";");
        for (var command : commands) {
            command = command.trim();
            if (command.startsWith("/")) command = command.substring(1);
            if (!command.isBlank()) executeCommand(player.level().getServer(), player, command);
        }
    }

    public static void executeCommand(MinecraftServer server, @Nullable Entity entity, String command) {
        // 创建命令源，并赋予2级权限，且禁止输出
        CommandSourceStack commandSource;
        if (entity != null) {
            if (entity instanceof ServerPlayer player) {
                commandSource = player.createCommandSourceStack();
            } else commandSource = entity.createCommandSourceStackForNameResolution((ServerLevel) entity.level());
        } else commandSource = server.createCommandSourceStack();
        commandSource = commandSource.withPermission(LevelBasedPermissionSet.GAMEMASTER).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(commandSource, command);
    }
}
