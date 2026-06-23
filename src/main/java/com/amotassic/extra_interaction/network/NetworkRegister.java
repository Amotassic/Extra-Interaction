package com.amotassic.extra_interaction.network;

import com.amotassic.extra_interaction.ExtraInteraction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = ExtraInteraction.MOD_ID)
public class NetworkRegister {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(ExtraInteraction.MOD_ID);
        //c2s
        registrar.playToServer(SendInteraction.TYPE, SendInteraction.CODEC, (packet, context) -> SendInteraction.handle(context.player(), packet));
        //s2c
        registrar.playToClient(SendTalkData.TYPE, SendTalkData.CODEC, ((packet, _) -> SendTalkData.handle(packet)));
    }
}
