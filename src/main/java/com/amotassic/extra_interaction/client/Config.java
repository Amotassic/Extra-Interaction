package com.amotassic.extra_interaction.client;

import com.amotassic.extra_interaction.ExtraInteraction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec CONFIG_SPEC;
    static final Identifier defaultTexture = ExtraInteraction.id("textures/option.png");
    public static final ModConfigSpec.ConfigValue<String> optionTexture;
    public static final ModConfigSpec.DoubleValue xPosPercent;
    public static final ModConfigSpec.DoubleValue yPosPercent;
    public static final ModConfigSpec.IntValue optionWidth;
    public static final ModConfigSpec.IntValue optionHeight;
    public static final ModConfigSpec.IntValue showCount;

    static {
        var builder = new ModConfigSpec.Builder();
        optionTexture = builder.define("optionTexture", "extra_interaction:textures/option.png");
        xPosPercent = builder.comment(" 单位：屏幕宽度百分比", " unit: percentage of screen width")
                .defineInRange("xPosPercent", 70d, 0, 100);
        yPosPercent = builder.comment(" 单位：屏幕高度百分比", " unit: percentage of screen height")
                .defineInRange("yPosPercent", 50d, 0, 100);
        optionWidth = builder.comment(" 单位：像素", " unit: pixels")
                .defineInRange("optionWidth", 120, 30, 999);
        optionHeight = builder.comment(" 单位：像素", " unit: pixels")
                .defineInRange("optionHeight", 20, 13, 99);
        showCount = builder.comment(" 如果同时出现的选项数量较多", " used when many options appear simultaneously")
                .defineInRange("showCount", 3, 1, 12);
        CONFIG_SPEC = builder.build();
    }

    public static Identifier getOptionTexture() {
        String string = optionTexture.get();
        Identifier texture = null;
        try {
            if (!string.isBlank()) texture = Identifier.parse(string);
        } catch (Exception ignored) {
        }
        return texture == null ? defaultTexture : texture;
    }
    public static double getXPos() {return xPosPercent.get() / 100;}
    public static double getYPos() {return yPosPercent.get() / 100;}
}
