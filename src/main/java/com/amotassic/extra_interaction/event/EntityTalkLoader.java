package com.amotassic.extra_interaction.event;

import com.amotassic.extra_interaction.ExtraInteraction;
import com.amotassic.extra_interaction.interaction.EntityTalkData;
import com.amotassic.extra_interaction.network.SendTalkData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ExtraInteraction.MOD_ID)
public class EntityTalkLoader {

    @SubscribeEvent
    public static void loadTalkData(AddServerReloadListenersEvent event) {
        event.addListener(ExtraInteraction.id("entity_talk"), new EntityTalkData());
    }

    @SubscribeEvent
    public static void initializeChatBoxScreen(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        //发包到客户端
        if (player != null) sendTalkData(player);
        else event.getPlayerList().getPlayers().forEach(EntityTalkLoader::sendTalkData);
    }

    static void sendTalkData(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SendTalkData(EntityTalkData.entityTalkData));
    }
}
