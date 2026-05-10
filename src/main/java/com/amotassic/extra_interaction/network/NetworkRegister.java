package com.amotassic.extra_interaction.network;

import com.amotassic.extra_interaction.ExtraInteraction;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = ExtraInteraction.MOD_ID)
public class NetworkRegister {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(ExtraInteraction.MOD_ID);
        //c2s
        registrar.playToServer(SendInteraction.TYPE, SendInteraction.CODEC, (packet, context) -> SendInteraction.handle((ServerPlayer) context.player(), packet));
    }
}
