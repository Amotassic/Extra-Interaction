package com.amotassic.extra_interaction;

import com.amotassic.extra_interaction.api.InteractionAutoRegister;
import com.amotassic.extra_interaction.api.InteractionRegister;
import com.zhenshiz.chatbox.utils.chatbox.ChatBoxCommandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

@InteractionAutoRegister
public class TeaconExample implements InteractionRegister {

    static void interactBlock(Player player, BlockPos pos) {
        player.level().getBlockState(pos).useWithoutItem(player.level(), player, new BlockHitResult(player.position(), Direction.UP, pos, false));
    }

    static boolean isDoorOpen(Player player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        return state.getBlock() instanceof DoorBlock door && door.isOpen(state);
    }

    public void init() {
/*        var open_door = withBlock((player, pos) -> !isDoorOpen(player, pos), TeaconExample::interactBlock);
        var close_door = withBlock(TeaconExample::isDoorOpen, TeaconExample::interactBlock);
        Predicate<Block> doorsCanOpenByHand = block -> block instanceof DoorBlock door && door.type().canOpenByHand();
        addForBlocks(doorsCanOpenByHand, "开门", open_door);
        addForBlocks(doorsCanOpenByHand, "关门", close_door);*/

        String[] names = {"amotassic", "zhenshiz", "zi_min"};
        var words = new HashMap<String, List<String>>();
        words.put("zi_min", List.of("Hello大家好，欢迎来到籽岷和他朋友们的Minecraft游戏世界！", "！？强强？！"));
        var talk_interaction = withEntity(
                (player, entity) -> {
                    String string = entity.getName().getString().toLowerCase();
                    for (String name : names) if (name.equals(string)) return true;
                    return false;
                },
                (player, entity) -> {
                    if (player.level().isClientSide()) return;
                    var name = entity.getName().getString().toLowerCase();
                    switch (name) {
                        case "amotassic", "zhenshiz" -> {
                            boolean cbLoaded = ExtraInteraction.isModLoaded("chatbox");
                            if (cbLoaded) ChatBoxCommandUtil.serverSkipDialogues((ServerPlayer) player, Identifier.fromNamespaceAndPath("example", "teacon_intro"), "start", 0, List.of(entity));
                        }
                        default -> {
                            var messages = words.get(name);
                            if (messages != null) player.sendSystemMessage(Component.literal(messages.get(new Random().nextInt(messages.size()))));
                        }
                    }
                }
        ).icon(ExtraInteraction.id("textures/talk.png")).tips("跟特定名称的玩家交互可以触发特殊剧情（未完成）");
        addForEntity("minecraft:mannequin", "对话", talk_interaction);
        addForEntity("minecraft:player", "对话", talk_interaction);

        addForEntity("minecraft:villager", "打劫", withEntity(
                (_, entity) -> entity.isAlive(),
                (player, entity) -> {
                    if (player.level().isClientSide()) return;
                    Villager villager = (Villager) entity;
                    if (villager.isBaby()) {
                        player.sendSystemMessage(Component.literal("你连小村民都要打劫？你怎么这么坏！"));
                        return;
                    }
                    villager.invulnerableTime = 0;
                    villager.hurtServer((ServerLevel) player.level(), player.damageSources().genericKill(), 5);
                    ItemStack stack = new ItemStack(Items.EMERALD);
                    Vec3 pos = villager.position();
                    ItemEntity itemEntity = new ItemEntity(player.level(), pos.x, pos.y, pos.z, stack);
                    player.level().addFreshEntity(itemEntity);
                }
        ).icon(Identifier.parse("textures/item/diamond_sword.png")));
        addForEntity("minecraft:villager", "送花", withEntity(
                (player, _) -> player.getMainHandItem().is(ItemTags.FLOWERS),
                (player, entity) -> {
                    if (player.level().isClientSide()) return;
                    Villager villager = (Villager) entity;
                    player.getMainHandItem().shrink(1);
                    villager.getGossips().add(player.getUUID(), GossipType.MAJOR_POSITIVE, 1);
                    villager.getGossips().add(player.getUUID(), GossipType.MINOR_NEGATIVE, -25);
                }
        ).icon(Identifier.parse("textures/block/poppy.png")));
/*        addForEntity("minecraft:villager", "测试1", Interaction.EMPTY);
        addForEntity("minecraft:villager", "测试2", Interaction.EMPTY);
        addForEntity("minecraft:villager", "测试3", Interaction.EMPTY);
        addForEntity("minecraft:villager", "测试4", Interaction.EMPTY);
        addForEntity("minecraft:villager", "测试5", Interaction.EMPTY);*/
    }
}
