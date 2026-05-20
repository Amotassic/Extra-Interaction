package com.amotassic.extra_interaction;

import com.amotassic.extra_interaction.api.InteractionAutoRegister;
import com.amotassic.extra_interaction.api.InteractionRegister;
import com.amotassic.extra_interaction.client.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.lang.annotation.ElementType;

@Mod(ExtraInteraction.MOD_ID)
public class ExtraInteraction {
    public static final String MOD_ID = "extra_interaction";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExtraInteraction(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        LOGGER.info("Ciallo～(∠·ω< )⌒★");
        register();
        if (dist.isClient()) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CONFIG_SPEC, MOD_ID + "_client.toml");
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    private static void register() {
        var annotationType = org.objectweb.asm.Type.getType(InteractionAutoRegister.class);
        for (var data : ModList.get().getAllScanData()) {
            for (var annotation : data.getAnnotations()) {
                if (annotationType.equals(annotation.annotationType()) && annotation.targetType() == ElementType.TYPE) {
                    String name = annotation.memberName();
                    try {
                        Class<?> forName = Class.forName(name);
                        if (forName.getConstructor().newInstance() instanceof InteractionRegister register) {
                            register.init();
                            LOGGER.info("Successfully registered interactions in class: {}", name);
                        }
                    } catch (Throwable e) {
                        LOGGER.error("Failed to register interactions for class: {}", name, e);
                    }
                }
            }
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
