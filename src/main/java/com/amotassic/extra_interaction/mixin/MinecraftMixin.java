package com.amotassic.extra_interaction.mixin;

import com.amotassic.extra_interaction.client.InteractionHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 9))
    private boolean handleKeybinds(KeyMapping instance, Operation<Boolean> original) {
        boolean bl = original.call(instance);
        if (instance.getKey().getValue() == InteractionHandler.interactKey.getKey().getValue() && InteractionHandler.hasOptions()) {
            bl = false;
        }
        return bl;
    }
}
