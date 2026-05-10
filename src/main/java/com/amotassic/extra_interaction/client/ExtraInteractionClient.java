package com.amotassic.extra_interaction.client;

import com.amotassic.extra_interaction.ExtraInteraction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = ExtraInteraction.MOD_ID, value = Dist.CLIENT)
public class ExtraInteractionClient {

    @SubscribeEvent
    public static void registerKey(RegisterKeyMappingsEvent event) {
        event.register(InteractionHandler.interactKey);
    }

    @SubscribeEvent
    public static void hud(RenderGuiEvent.Post event) {
        InteractionHandler.onHudRender(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        InteractionHandler.onKey(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (InteractionHandler.onMouseScroll(event.getScrollDeltaX(), event.getScrollDeltaY(), event.isLeftDown(), event.isMiddleDown(), event.isRightDown(), event.getMouseX(), event.getMouseY())) event.setCanceled(true);
    }
}
